package lekavar.lma.drinkbeer.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemStackHelper {
    public static boolean isSameItem(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem();
    }

    public static void giveToPlayer(Player player, ItemStack stack) {
        player.getInventory().add(stack);
        if (!stack.isEmpty()) {
            player.drop(stack, false);
        }
    }
}
