package lekavar.lma.drinkbeer.neoforge;

import lekavar.lma.drinkbeer.platform.ItemHandlerView;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;
import java.util.Objects;

final class NeoForgeItemHandlerAdapter extends SnapshotJournal<List<ItemStack>>
        implements ResourceHandler<ItemResource> {
    private final ItemHandlerView delegate;

    NeoForgeItemHandlerAdapter(ItemHandlerView delegate) {
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return delegate.getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
        Objects.checkIndex(index, size());
        return ItemResource.of(delegate.getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return delegate.getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        if (!resource.isEmpty() && !isValid(index, resource)) {
            return 0;
        }
        return resource.isEmpty()
                ? delegate.getSlotLimit(index)
                : Math.min(delegate.getSlotLimit(index), resource.getMaxStackSize());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return !resource.isEmpty() && delegate.isItemValid(index, resource.toStack());
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            return 0;
        }

        ItemStack candidate = resource.toStack(amount);
        int accepted = amount - delegate.insertItem(index, candidate, true).getCount();
        if (accepted <= 0) {
            return 0;
        }

        updateSnapshots(transaction);
        return accepted - delegate.insertItem(index, resource.toStack(accepted), false).getCount();
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            return 0;
        }

        ItemStack stored = delegate.getStackInSlot(index);
        if (!resource.matches(stored)) {
            return 0;
        }
        ItemStack simulated = delegate.extractItem(index, amount, true);
        if (simulated.isEmpty() || !resource.matches(simulated)) {
            return 0;
        }

        updateSnapshots(transaction);
        return delegate.extractItem(index, simulated.getCount(), false).getCount();
    }

    @Override
    protected List<ItemStack> createSnapshot() {
        return delegate.createSnapshot();
    }

    @Override
    protected void revertToSnapshot(List<ItemStack> snapshot) {
        delegate.restoreSnapshot(snapshot);
    }

    @Override
    protected void onRootCommit(List<ItemStack> originalState) {
        delegate.onTransferCommitted();
    }
}
