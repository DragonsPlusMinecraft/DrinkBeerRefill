package lekavar.lma.drinkbeer.fabric;

import lekavar.lma.drinkbeer.platform.ItemHandlerView;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class FabricItemStorageAdapter extends SnapshotParticipant<List<ItemStack>> implements Storage<ItemVariant> {
    private final ItemHandlerView handler;

    FabricItemStorageAdapter(ItemHandlerView handler) {
        this.handler = handler;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long inserted = 0;
        for (int slot = 0; slot < handler.getSlots() && inserted < maxAmount; slot++) {
            int requested = (int) Math.min(Integer.MAX_VALUE, maxAmount - inserted);
            ItemStack candidate = resource.toStack(requested);
            ItemStack simulatedRemainder = handler.insertItem(slot, candidate, true);
            int accepted = requested - simulatedRemainder.getCount();
            if (accepted <= 0) {
                continue;
            }
            updateSnapshots(transaction);
            ItemStack remainder = handler.insertItem(slot, resource.toStack(accepted), false);
            inserted += accepted - remainder.getCount();
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long extracted = 0;
        for (int slot = 0; slot < handler.getSlots() && extracted < maxAmount; slot++) {
            extracted += extractFromSlot(slot, resource, maxAmount - extracted, transaction);
        }
        return extracted;
    }

    private long extractFromSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        ItemStack stored = handler.getStackInSlot(slot);
        if (stored.isEmpty() || !resource.matches(stored)) {
            return 0;
        }
        int requested = (int) Math.min(Integer.MAX_VALUE, maxAmount);
        ItemStack simulated = handler.extractItem(slot, requested, true);
        if (simulated.isEmpty() || !resource.matches(simulated)) {
            return 0;
        }
        updateSnapshots(transaction);
        return handler.extractItem(slot, simulated.getCount(), false).getCount();
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = new ArrayList<>(handler.getSlots());
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            views.add(new SlotView(slot));
        }
        return views.iterator();
    }

    @Override
    protected List<ItemStack> createSnapshot() {
        return handler.createSnapshot();
    }

    @Override
    protected void readSnapshot(List<ItemStack> snapshot) {
        handler.restoreSnapshot(snapshot);
    }

    @Override
    protected void onFinalCommit() {
        handler.onTransferCommitted();
    }

    private final class SlotView implements StorageView<ItemVariant> {
        private final int slot;

        private SlotView(int slot) {
            this.slot = slot;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            return extractFromSlot(slot, resource, maxAmount, transaction);
        }

        @Override
        public boolean isResourceBlank() {
            return handler.getStackInSlot(slot).isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(handler.getStackInSlot(slot));
        }

        @Override
        public long getAmount() {
            return handler.getStackInSlot(slot).getCount();
        }

        @Override
        public long getCapacity() {
            return handler.getSlotLimit(slot);
        }
    }
}
