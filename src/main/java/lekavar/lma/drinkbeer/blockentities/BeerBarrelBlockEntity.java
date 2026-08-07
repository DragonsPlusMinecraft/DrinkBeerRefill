package lekavar.lma.drinkbeer.blockentities;

import lekavar.lma.drinkbeer.gui.BeerBarrelMenu;
import lekavar.lma.drinkbeer.recipes.BrewingRecipe;
import lekavar.lma.drinkbeer.recipes.IBrewingInventory;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.RecipeRegistry;
import lekavar.lma.drinkbeer.utils.ContainerNbtHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BeerBarrelBlockEntity extends BlockEntity implements MenuProvider {
    public static final int STATUS_WAITING = 0;
    public static final int STATUS_BREWING = 1;
    public static final int STATUS_READY = 2;

    private static final int INGREDIENT_SLOT_COUNT = 4;
    private static final int CUP_SLOT = 4;
    private static final int OUTPUT_SLOT = 5;

    private final BrewingInventory brewingInventory = new BrewingInventory(this);
    private final IItemHandler combinedItemHandler = new BarrelItemHandler(this, HandlerMode.COMBINED);
    private final IItemHandler ingredientItemHandler = new BarrelItemHandler(this, HandlerMode.INGREDIENT_INPUT);
    private final IItemHandler cupItemHandler = new BarrelItemHandler(this, HandlerMode.CUP_INPUT);
    private final IItemHandler outputItemHandler = new BarrelItemHandler(this, HandlerMode.OUTPUT);
    private LazyOptional<IItemHandler> combinedCapability = LazyOptional.of(() -> combinedItemHandler);
    private LazyOptional<IItemHandler> ingredientCapability = LazyOptional.of(() -> ingredientItemHandler);
    private LazyOptional<IItemHandler> cupCapability = LazyOptional.of(() -> cupItemHandler);
    private LazyOptional<IItemHandler> outputCapability = LazyOptional.of(() -> outputItemHandler);

    private int remainingBrewTime;
    private int statusCode = STATUS_WAITING;

    public final ContainerData syncData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> remainingBrewTime;
                case 1 -> statusCode;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> remainingBrewTime = Math.max(0, value);
                case 1 -> statusCode = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public BeerBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.BEER_BARREL_TILEENTITY.get(), pos, state);
    }

    public void tickServer() {
        if (level == null || level.isClientSide()) {
            return;
        }

        switch (statusCode) {
            case STATUS_WAITING -> tryStartBrewing();
            case STATUS_BREWING -> tickBrewing();
            case STATUS_READY -> {
                if (brewingInventory.getItem(OUTPUT_SLOT).isEmpty()) {
                    statusCode = STATUS_WAITING;
                    remainingBrewTime = 0;
                    updateBE();
                }
            }
            default -> {
                statusCode = STATUS_WAITING;
                remainingBrewTime = 0;
                updateBE();
            }
        }
    }

    private void tryStartBrewing() {
        // Old versions could save a pre-generated result while the barrel was still marked as waiting.
        // Treat it as ready instead of permanently locking that result in the output slot.
        if (!brewingInventory.getItem(OUTPUT_SLOT).isEmpty()) {
            statusCode = STATUS_READY;
            remainingBrewTime = 0;
            updateBE();
            return;
        }

        BrewingRecipe recipe = findRecipe();
        if (recipe == null || !recipe.isCupQualified(brewingInventory)) {
            return;
        }

        remainingBrewTime = Math.max(1, recipe.getBrewingTime());
        statusCode = STATUS_BREWING;
        updateBE();
    }

    private void tickBrewing() {
        if (remainingBrewTime > 0) {
            remainingBrewTime--;
        }

        if (remainingBrewTime > 0) {
            setChanged();
            return;
        }

        remainingBrewTime = 0;

        // Compatibility with barrels saved by the old implementation, which created the output before
        // starting the countdown and consumed the ingredients immediately.
        if (!brewingInventory.getItem(OUTPUT_SLOT).isEmpty()) {
            statusCode = STATUS_READY;
            updateBE();
            return;
        }

        BrewingRecipe recipe = findRecipe();
        if (recipe == null || !recipe.isCupQualified(brewingInventory)) {
            statusCode = STATUS_WAITING;
            updateBE();
            return;
        }

        completeBrewing(recipe);
    }

    @Nullable
    private BrewingRecipe findRecipe() {
        if (level == null) {
            return null;
        }
        return level.getRecipeManager()
                .getRecipeFor(RecipeRegistry.RECIPE_TYPE_BREWING.get(), brewingInventory, level)
                .orElse(null);
    }

    private void completeBrewing(BrewingRecipe recipe) {
        ItemStack result = recipe.assemble(brewingInventory, level.registryAccess());
        if (result.isEmpty()) {
            statusCode = STATUS_WAITING;
            updateBE();
            return;
        }

        for (int slot = 0; slot < INGREDIENT_SLOT_COUNT; slot++) {
            ItemStack consumed = brewingInventory.removeItem(slot, 1);
            if (shouldReturnBucket(consumed)) {
                ItemStack bucket = Items.BUCKET.getDefaultInstance();
                if (brewingInventory.getItem(slot).isEmpty()) {
                    brewingInventory.setItem(slot, bucket);
                } else {
                    Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D,
                            worldPosition.getZ() + 0.5D, bucket);
                }
            }
        }

        brewingInventory.removeItem(CUP_SLOT, recipe.getRequiredCupCount());
        brewingInventory.setItem(OUTPUT_SLOT, result);
        statusCode = STATUS_READY;
        updateBE();
    }

    private boolean shouldReturnBucket(ItemStack item) {
        return item.getItem() instanceof BucketItem || item.getItem() instanceof MilkBucketItem;
    }

    public BrewingInventory getBrewingInventory() {
        return brewingInventory;
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return combinedItemHandler;
        }
        if (side == Direction.UP) {
            return ingredientItemHandler;
        }
        if (side == Direction.DOWN) {
            return outputItemHandler;
        }
        return cupItemHandler;
    }

    public boolean canModifyInputs() {
        return statusCode != STATUS_BREWING;
    }

    public boolean isOutputReady() {
        return statusCode == STATUS_READY;
    }

    private boolean canPlaceInSlot(int slot, ItemStack stack) {
        if (!canModifyInputs() || stack.isEmpty()) {
            return false;
        }
        if (slot >= 0 && slot < INGREDIENT_SLOT_COUNT) {
            return isValidIngredient(stack);
        }
        if (slot == CUP_SLOT) {
            return isValidCup(stack);
        }
        return false;
    }

    private boolean isValidIngredient(ItemStack stack) {
        if (level == null) {
            return !stack.is(ItemRegistry.EMPTY_BEER_MUG.get());
        }
        return level.getRecipeManager().getAllRecipesFor(RecipeRegistry.RECIPE_TYPE_BREWING.get()).stream()
                .flatMap(recipe -> recipe.getIngredients().stream())
                .anyMatch(ingredient -> ingredient.test(stack));
    }

    private boolean isValidCup(ItemStack stack) {
        if (level == null) {
            return stack.is(ItemRegistry.EMPTY_BEER_MUG.get());
        }
        return level.getRecipeManager().getAllRecipesFor(RecipeRegistry.RECIPE_TYPE_BREWING.get()).stream()
                .map(BrewingRecipe::getBeerCup)
                .anyMatch(cup -> ItemStack.isSameItemSameTags(cup, stack));
    }

    public void updateBE() {
        if (level == null) {
            setChanged();
            return;
        }
        BlockState blockState = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_CLIENTS);
        setChanged();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerNbtHelper.saveAllItems(tag, brewingInventory);
        tag.putInt("RemainingBrewTime", remainingBrewTime);
        tag.putInt("statusCode", statusCode);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        int rawRemainingBrewTime = tag.getInt("RemainingBrewTime");
        int rawStatusCode = tag.getInt("statusCode");
        remainingBrewTime = Math.max(0, rawRemainingBrewTime);
        statusCode = rawStatusCode;
        if (statusCode < STATUS_WAITING || statusCode > STATUS_READY) {
            statusCode = STATUS_WAITING;
        }
        loadInventory(tag);
        boolean legacyNumericTags = (tag.contains("RemainingBrewTime", Tag.TAG_ANY_NUMERIC)
                && !tag.contains("RemainingBrewTime", Tag.TAG_INT))
                || (tag.contains("statusCode", Tag.TAG_ANY_NUMERIC) && !tag.contains("statusCode", Tag.TAG_INT));
        if (legacyNumericTags || rawRemainingBrewTime != remainingBrewTime || rawStatusCode != statusCode) {
            setChanged();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.drinkbeer.beer_barrel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new BeerBarrelMenu(id, brewingInventory, syncData, inventory, this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) {
            handleUpdateTag(packet.getTag());
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        ContainerNbtHelper.saveAllItems(tag, brewingInventory);
        tag.putInt("RemainingBrewTime", remainingBrewTime);
        tag.putInt("statusCode", statusCode);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        remainingBrewTime = Math.max(0, tag.getInt("RemainingBrewTime"));
        statusCode = tag.getInt("statusCode");
        if (statusCode < STATUS_WAITING || statusCode > STATUS_READY) {
            statusCode = STATUS_WAITING;
        }
        loadInventory(tag);
    }

    private void loadInventory(CompoundTag tag) {
        brewingInventory.clearContent();
        if (tag.contains("Items", Tag.TAG_LIST)) {
            ContainerNbtHelper.loadAllItems(tag, brewingInventory);
            return;
        }
        if (tag.contains("inv", Tag.TAG_LIST)) {
            ListTag legacyItems = tag.getList("inv", Tag.TAG_COMPOUND);
            for (int slot = 0; slot < Math.min(brewingInventory.getContainerSize(), legacyItems.size()); slot++) {
                brewingInventory.setItem(slot, ItemStack.of(legacyItems.getCompound(slot)));
            }
            setChanged();
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        combinedCapability.invalidate();
        ingredientCapability.invalidate();
        cupCapability.invalidate();
        outputCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        combinedCapability = LazyOptional.of(() -> combinedItemHandler);
        ingredientCapability = LazyOptional.of(() -> ingredientItemHandler);
        cupCapability = LazyOptional.of(() -> cupItemHandler);
        outputCapability = LazyOptional.of(() -> outputItemHandler);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return combinedCapability.cast();
            }
            if (side == Direction.UP) {
                return ingredientCapability.cast();
            }
            if (side == Direction.DOWN) {
                return outputCapability.cast();
            }
            return cupCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    public static class BrewingInventory extends SimpleContainer implements IBrewingInventory {
        private final BeerBarrelBlockEntity blockEntity;

        public BrewingInventory(BeerBarrelBlockEntity blockEntity) {
            super(6);
            this.blockEntity = blockEntity;
        }

        @NotNull
        @Override
        public List<ItemStack> getIngredients() {
            List<ItemStack> ingredients = new ArrayList<>();
            for (int slot = 0; slot < INGREDIENT_SLOT_COUNT; slot++) {
                if (!getItem(slot).isEmpty()) {
                    ingredients.add(getItem(slot).copy());
                }
            }
            return ingredients;
        }

        @NotNull
        @Override
        public ItemStack getCup() {
            return getItem(CUP_SLOT).copy();
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return blockEntity.canPlaceInSlot(slot, stack);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            blockEntity.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return blockEntity.level != null
                    && blockEntity.level.getBlockEntity(blockEntity.worldPosition) == blockEntity
                    && player.distanceToSqr(blockEntity.worldPosition.getX() + 0.5D, blockEntity.worldPosition.getY() + 0.5D,
                    blockEntity.worldPosition.getZ() + 0.5D) <= 64.0D;
        }

    }

    private enum HandlerMode {
        COMBINED,
        INGREDIENT_INPUT,
        CUP_INPUT,
        OUTPUT
    }

    private static class BarrelItemHandler implements IItemHandler {
        private static final int[] COMBINED_SLOTS = {0, 1, 2, 3, 4, 5};
        private static final int[] INGREDIENT_SLOTS = {0, 1, 2, 3};
        private static final int[] CUP_SLOTS = {4};
        private static final int[] OUTPUT_SLOTS = {0, 1, 2, 3, 5};

        private final BeerBarrelBlockEntity blockEntity;
        private final int[] slots;
        private final boolean canInsert;

        private BarrelItemHandler(BeerBarrelBlockEntity blockEntity, HandlerMode mode) {
            this.blockEntity = blockEntity;
            this.slots = switch (mode) {
                case COMBINED -> COMBINED_SLOTS;
                case INGREDIENT_INPUT -> INGREDIENT_SLOTS;
                case CUP_INPUT -> CUP_SLOTS;
                case OUTPUT -> OUTPUT_SLOTS;
            };
            this.canInsert = mode != HandlerMode.OUTPUT;
        }

        @Override
        public int getSlots() {
            return slots.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int inventorySlot = resolveSlot(slot);
            return inventorySlot < 0 ? ItemStack.EMPTY : blockEntity.brewingInventory.getItem(inventorySlot).copy();
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int inventorySlot = resolveSlot(slot);
            if (!canInsert || inventorySlot < 0 || !blockEntity.canPlaceInSlot(inventorySlot, stack)) {
                return stack;
            }

            ItemStack existing = blockEntity.brewingInventory.getItem(inventorySlot);
            if (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, stack)) {
                return stack;
            }

            int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
            int inserted = Math.min(stack.getCount(), limit - existing.getCount());
            if (inserted <= 0) {
                return stack;
            }

            if (!simulate) {
                ItemStack updated = existing.isEmpty() ? stack.copy() : existing.copy();
                updated.setCount(existing.isEmpty() ? inserted : existing.getCount() + inserted);
                blockEntity.brewingInventory.setItem(inventorySlot, updated);
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(inserted);
            return remainder;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int inventorySlot = resolveSlot(slot);
            if (inventorySlot < 0 || amount <= 0) {
                return ItemStack.EMPTY;
            }

            ItemStack existing = blockEntity.brewingInventory.getItem(inventorySlot);
            boolean canExtractOutput = inventorySlot == OUTPUT_SLOT && blockEntity.isOutputReady();
            boolean canExtractBucket = inventorySlot < INGREDIENT_SLOT_COUNT
                    && blockEntity.canModifyInputs()
                    && existing.is(Items.BUCKET);
            if (existing.isEmpty() || (!canExtractOutput && !canExtractBucket)) {
                return ItemStack.EMPTY;
            }

            int extracted = Math.min(amount, existing.getCount());
            ItemStack result = existing.copy();
            result.setCount(extracted);
            if (!simulate) {
                blockEntity.brewingInventory.removeItem(inventorySlot, extracted);
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            int inventorySlot = resolveSlot(slot);
            if (inventorySlot < 0) {
                return 0;
            }
            if (inventorySlot < INGREDIENT_SLOT_COUNT) {
                // One item per automated insertion lets a hopper distribute repeated ingredients over all four slots.
                return 1;
            }
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int inventorySlot = resolveSlot(slot);
            return canInsert && inventorySlot >= 0 && blockEntity.canPlaceInSlot(inventorySlot, stack);
        }

        private int resolveSlot(int slot) {
            return slot >= 0 && slot < slots.length ? slots[slot] : -1;
        }
    }
}
