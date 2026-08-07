package lekavar.lma.drinkbeer.blockentities;

import lekavar.lma.drinkbeer.items.MixedBeerBlockItem;
import lekavar.lma.drinkbeer.items.SpiceBlockItem;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.DrinkBeerTags;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BartendingTableBlockEntity extends BlockEntity {
    private static final int BASE_BEER_SLOT = 0;
    private static final int MIXED_BEER_SLOT = 1;

    private final SimpleContainer inv = new OneItemContainer(2);
    private final IItemHandler combinedItemHandler = new BartendingTableItemHandler(this, HandlerMode.COMBINED);
    private final IItemHandler beerInputHandler = new BartendingTableItemHandler(this, HandlerMode.BEER_INPUT);
    private final IItemHandler spiceInputHandler = new BartendingTableItemHandler(this, HandlerMode.SPICE_INPUT);
    private final IItemHandler outputHandler = new BartendingTableItemHandler(this, HandlerMode.OUTPUT);

    public BartendingTableBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.BARTENDING_TABLE_TILEENTITY.get(), pos, state);
    }

    public TableActionResult placeBeer(ItemStack itemStack) {
        TableActionResult validation = validateBeerInput(itemStack);
        if (validation != TableActionResult.SUCCESS) {
            return validation;
        }
        ItemStack placedBeer = itemStack.copy();
        placedBeer.setCount(1);
        inv.setItem(BASE_BEER_SLOT, placedBeer);
        markDirty();
        return TableActionResult.SUCCESS;
    }

    public TableActionResult putSpice(ItemStack itemStack) {
        TableActionResult validation = validateSpiceInput(itemStack);
        if (validation != TableActionResult.SUCCESS) {
            return validation;
        }

        ItemStack beerItem = getBeerStack();
        var beerId = beerItem.getItem() instanceof MixedBeerBlockItem ? MixedBeerBlockItem.getBeerId(beerItem) : Beers.byItem(beerItem.getItem()).getId();
        var spiceList = MixedBeerManager.getSpiceList(beerItem);
        spiceList.add(Spices.byItem(itemStack.getItem()).getId());
        ItemStack flavoredBeer = MixedBeerManager.genMixedBeerItemStack(beerId, spiceList);
        inv.setItem(BASE_BEER_SLOT, ItemStack.EMPTY);
        inv.setItem(MIXED_BEER_SLOT, flavoredBeer);
        markDirty();
        return TableActionResult.SUCCESS;
    }

    public ItemStack takeBeer(boolean simulate) {
        var ret = inv.getItem(BASE_BEER_SLOT).copy();
        if (ret.isEmpty())
            ret = inv.getItem(MIXED_BEER_SLOT).copy();
        if (!simulate && !ret.isEmpty()) {
            inv.clearContent();
            markDirty();
        }
        return ret;
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return combinedItemHandler;
        }
        if (side == Direction.UP) {
            return beerInputHandler;
        }
        if (side == Direction.DOWN) {
            return outputHandler;
        }
        return spiceInputHandler;
    }

    public SimpleContainer getInventory() {
        return inv;
    }

    private ItemStack getBeerStack() {
        ItemStack baseBeer = inv.getItem(BASE_BEER_SLOT);
        return baseBeer.isEmpty() ? inv.getItem(MIXED_BEER_SLOT) : baseBeer;
    }

    private TableActionResult validateBeerInput(ItemStack itemStack) {
        if (itemStack.isEmpty() || !itemStack.is(DrinkBeerTags.BEERS)) {
            return TableActionResult.INVALID_ITEM;
        }
        return inv.isEmpty() ? TableActionResult.SUCCESS : TableActionResult.OCCUPIED;
    }

    private TableActionResult validateSpiceInput(ItemStack itemStack) {
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpiceBlockItem)) {
            return TableActionResult.INVALID_ITEM;
        }
        if (inv.isEmpty()) {
            return TableActionResult.NO_BEER;
        }
        return MixedBeerManager.getSpiceList(getBeerStack()).size() >= MixedBeerManager.MAX_SPICES_COUNT
                ? TableActionResult.SPICE_FULL
                : TableActionResult.SUCCESS;
    }


    public void markDirty() {
        if (level == null) {
            setChanged();
            return;
        }
        var pos = getBlockPos();
        var bs = level.getBlockState(pos);
        level.sendBlockUpdated(pos, bs, bs, Block.UPDATE_CLIENTS);
        setChanged();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        handleUpdateTag(pkt.getTag(),registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // The packet uses the CompoundTag returned by #getUpdateTag. An alternative overload of #create exists
        // that allows you to specify a custom update tag, including the ability to omit data the client might not need.
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        ContainerHelper.saveAllItems(tag, this.inv.getItems(), true, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        ContainerHelper.loadAllItems(tag, this.inv.getItems(), registries);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries);
        ContainerHelper.saveAllItems(tag, this.inv.getItems(), true, registries);
    }

    @Override
    public void loadAdditional(@Nonnull CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries);
        ContainerHelper.loadAllItems(tag, this.inv.getItems(), registries);
    }

    static class OneItemContainer extends SimpleContainer {
        public OneItemContainer(int pSize) {
            super(pSize);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    public enum TableActionResult {
        SUCCESS,
        INVALID_ITEM,
        OCCUPIED,
        NO_BEER,
        SPICE_FULL
    }

    private enum HandlerMode {
        COMBINED,
        BEER_INPUT,
        SPICE_INPUT,
        OUTPUT
    }

    private static class BartendingTableItemHandler implements IItemHandler {
        private final BartendingTableBlockEntity blockEntity;
        private final HandlerMode mode;

        private BartendingTableItemHandler(BartendingTableBlockEntity blockEntity, HandlerMode mode) {
            this.blockEntity = blockEntity;
            this.mode = mode;
        }

        @Override
        public int getSlots() {
            return mode == HandlerMode.COMBINED ? 2 : 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (!isValidSlot(slot)) {
                return ItemStack.EMPTY;
            }
            if (isBeerSlot(slot) || mode == HandlerMode.OUTPUT) {
                return blockEntity.getBeerStack().copy();
            }
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isValidSlot(slot) || stack.isEmpty() || mode == HandlerMode.OUTPUT) {
                return stack;
            }

            TableActionResult validation;
            if (isBeerSlot(slot)) {
                validation = blockEntity.validateBeerInput(stack);
            } else if (isSpiceSlot(slot)) {
                validation = blockEntity.validateSpiceInput(stack);
            } else {
                return stack;
            }

            if (validation != TableActionResult.SUCCESS) {
                return stack;
            }

            if (!simulate) {
                if (isBeerSlot(slot)) {
                    blockEntity.placeBeer(stack);
                } else {
                    blockEntity.putSpice(stack);
                }
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            boolean canExtract = mode == HandlerMode.OUTPUT || mode == HandlerMode.COMBINED && slot == 0;
            if (!isValidSlot(slot) || amount <= 0 || !canExtract) {
                return ItemStack.EMPTY;
            }
            ItemStack beer = blockEntity.getBeerStack();
            if (beer.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return blockEntity.takeBeer(simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return isValidSlot(slot) ? 1 : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (!isValidSlot(slot) || mode == HandlerMode.OUTPUT) {
                return false;
            }
            if (isBeerSlot(slot)) {
                return blockEntity.validateBeerInput(stack) == TableActionResult.SUCCESS;
            }
            return isSpiceSlot(slot) && blockEntity.validateSpiceInput(stack) == TableActionResult.SUCCESS;
        }

        private boolean isValidSlot(int slot) {
            return slot >= 0 && slot < getSlots();
        }

        private boolean isBeerSlot(int slot) {
            return mode == HandlerMode.BEER_INPUT || (mode == HandlerMode.COMBINED && slot == 0);
        }

        private boolean isSpiceSlot(int slot) {
            return mode == HandlerMode.SPICE_INPUT || (mode == HandlerMode.COMBINED && slot == 1);
        }
    }

}
