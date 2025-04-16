package lekavar.lma.drinkbeer.blockentities;

import lekavar.lma.drinkbeer.gui.BeerBarrelMenu;
import lekavar.lma.drinkbeer.recipes.BrewingRecipe;
import lekavar.lma.drinkbeer.recipes.IBrewingInventory;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.RecipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BeerBarrelBlockEntity extends BlockEntity implements MenuProvider {

    private final BrewingInventory brewingInventory = new BrewingInventory(this);
    public final IItemHandler itemHandler = new BarrelInvWrapper(this);
    private int remainingBrewTime;
    // 0 - waiting for ingredient, 1 - brewing, 2 - waiting for pickup product
    private int statusCode;
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
                case 0 -> remainingBrewTime = value;
                case 1 -> statusCode = value;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public BeerBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.BEER_BARREL_TILEENTITY.get(), pos, state);
    }

    public void tickServer() {
        // waiting for ingredient
        if (statusCode == 0) {
            // ingredient slots must have no empty slot
            if (brewingInventory.getIngredients().size() == 4) {
                // Try match Recipe
                RecipeHolder<BrewingRecipe> recipeholder = level.getRecipeManager().getRecipeFor(RecipeRegistry.RECIPE_TYPE_BREWING.get(), brewingInventory, this.level).orElse(null);
                if (recipeholder==null) clearResult();
                var recipe = recipeholder.value();
                if (canBrew(recipe)) {
                    // Show Standard Brewing Time & Result
                    setResult(recipe);
                    // Check Weather have enough cup.
                    if (hasEnoughEmptyCap(recipe)) {
                        startBrewing(recipe);
                    }
                }
                // Time remainingBrewTime will be reset since it also represents Standard Brewing Time if valid in this stage
                else {
                    clearResult();
                }
            } else {
                clearResult();
            }
        }
        // brewing
        else if (statusCode == 1) {
            if (remainingBrewTime > 0) {
                remainingBrewTime--;
            }
            // Enter "waiting for pickup"
            else {
                // Prevent wired glitch such as remainingTime been set to one;
                remainingBrewTime = 0;
                // Enter Next Stage
                statusCode = 2;
            }
            setChanged();
        }
        // waiting for pickup
        else if (statusCode == 2) {
            // Reset Stage to 0 (waiting for ingredients) after pickup Item
            if (brewingInventory.getItem(5).isEmpty()) {
                statusCode = 0;
                setChanged();
            }
        }
        // Error status reset
        else {
            remainingBrewTime = 0;
            statusCode = 0;
            setChanged();
        }
    }


    private boolean canBrew(@Nullable BrewingRecipe recipe) {
        if (recipe != null) {
            return recipe.matches(brewingInventory, this.level);
        } else {
            return false;
        }
    }

    private boolean hasEnoughEmptyCap(BrewingRecipe recipe) {
        return recipe.isCupQualified(brewingInventory);
    }

    private void startBrewing(BrewingRecipe recipe) {
        // Consume Ingredient & Cup;
        for (int i = 0; i < 4; i++) {
            ItemStack ingred = brewingInventory.getItem(i);
            if (shouldReturnBucket(ingred)) brewingInventory.setItem(i, Items.BUCKET.getDefaultInstance());
            else ingred.shrink(1);
        }
        brewingInventory.getItem(4).shrink(recipe.getRequiredCupCount());
        // Set Remaining Time;
        remainingBrewTime = recipe.getBrewingTime();
        // Change Status Code to 1 (brewing)
        statusCode = 1;
        markDirty();
    }

    private boolean shouldReturnBucket(ItemStack item) {
        return item.getItem() instanceof BucketItem || item.getItem() instanceof MilkBucketItem;
    }

    private void clearResult() {
        if (!brewingInventory.getItem(5).isEmpty()) {
            brewingInventory.setItem(5, ItemStack.EMPTY);
            remainingBrewTime = 0;
            markDirty();
        }
    }

    private void setResult(BrewingRecipe recipe) {
        var result = recipe.assemble(brewingInventory, level.registryAccess());
        if (!ItemStack.matches(result, brewingInventory.getItem(5))) {
            brewingInventory.setItem(5, recipe.assemble(brewingInventory, level.registryAccess()));
            remainingBrewTime = recipe.getBrewingTime();
            markDirty();
        }
    }

    public BrewingInventory getBrewingInventory() {
        return brewingInventory;
    }

    public void markDirty() {
        var pos = getBlockPos();
        var bs = level.getBlockState(pos);
        level.sendBlockUpdated(pos, bs, bs, Block.UPDATE_CLIENTS);
        setChanged();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries);
        tag.put("inv", brewingInventory.createTag(registries));
        tag.putShort("RemainingBrewTime", (short) this.remainingBrewTime);
        tag.putShort("statusCode", (short) this.statusCode);
    }

    @Override
    public void loadAdditional(@Nonnull CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries);
        this.remainingBrewTime = tag.getShort("RemainingBrewTime");
        this.statusCode = tag.getShort("statusCode");
        brewingInventory.fromTag((ListTag) tag.get("inv"), registries);

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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        handleUpdateTag(pkt.getTag(),registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.put("inv", brewingInventory.createTag(registries));
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        brewingInventory.fromTag((ListTag) tag.get("inv"),registries);
    }

    public static class BrewingInventory extends SimpleContainer implements IBrewingInventory {
        BeerBarrelBlockEntity be;

        public BrewingInventory(BeerBarrelBlockEntity be) {
            super(6);
            this.be = be;
        }

        @NotNull
        @Override
        public List<ItemStack> getIngredients() {
            List<ItemStack> ret = new ArrayList<>();
            if (isEmpty()) return ret;
            for (int i = 0; i < 4; i++) {
                if (!getItem(i).isEmpty()) ret.add(getItem(i));
            }
            return ret;
        }

        @NotNull
        @Override
        public ItemStack getCup() {
            return getItem(4);
        }

        @Override
        public boolean canPlaceItem(int pIndex, ItemStack pStack) {
            return super.canPlaceItem(pIndex, pStack);
        }


        @Override
        public boolean stillValid(Player pPlayer) {
            if (be.level.getBlockEntity(be.worldPosition) != be) {
                return false;
            } else {
                return !(pPlayer.distanceToSqr((double) be.worldPosition.getX() + 0.5D, (double) be.worldPosition.getY() + 0.5D, (double) be.worldPosition.getZ() + 0.5D) > 64.0D);
            }
        }

        @Override
        public int size() {
            return 6;
        }
    }

    static class BarrelInvWrapper extends ItemStackHandler {

        private BrewingInventory brewingInventory;
        private BeerBarrelBlockEntity be;

        public BarrelInvWrapper(BeerBarrelBlockEntity be) {
            this.brewingInventory = be.brewingInventory;
            this.be = be;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            // Well I do want to do nothing here but....
            brewingInventory.setItem(5, stack);
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (be.statusCode != 2) return ItemStack.EMPTY;
            return brewingInventory.getItem(5);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (be.statusCode != 2) return ItemStack.EMPTY;
            var ret = brewingInventory.getItem(5).copy();
            amount = Math.min(ret.getCount(), amount);
            ret.setCount(amount);
            if (!simulate) {
                brewingInventory.getItem(5).shrink(amount);
            }
            return ret;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }

}
