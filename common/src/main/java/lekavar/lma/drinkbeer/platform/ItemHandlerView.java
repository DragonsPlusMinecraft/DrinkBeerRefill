package lekavar.lma.drinkbeer.platform;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ItemHandlerView {
    int getSlots();

    @NotNull ItemStack getStackInSlot(int slot);

    @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate);

    @NotNull ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, @NotNull ItemStack stack);

    List<ItemStack> createSnapshot();

    void restoreSnapshot(List<ItemStack> snapshot);

    void onTransferCommitted();
}
