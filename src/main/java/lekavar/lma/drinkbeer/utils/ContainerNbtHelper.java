package lekavar.lma.drinkbeer.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class ContainerNbtHelper {
    public static void saveAllItems(CompoundTag tag, Container container) {
        ListTag items = new ListTag();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = stack.save(new CompoundTag());
                itemTag.putByte("Slot", (byte) slot);
                items.add(itemTag);
            }
        }
        tag.put("Items", items);
    }

    public static void loadAllItems(CompoundTag tag, Container container) {
        container.clearContent();
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int index = 0; index < items.size(); index++) {
            CompoundTag itemTag = items.getCompound(index);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < container.getContainerSize()) {
                container.setItem(slot, ItemStack.of(itemTag));
            }
        }
    }

    private ContainerNbtHelper() {
    }
}
