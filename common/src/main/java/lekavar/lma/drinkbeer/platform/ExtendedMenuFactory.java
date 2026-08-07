package lekavar.lma.drinkbeer.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@FunctionalInterface
public interface ExtendedMenuFactory<T extends AbstractContainerMenu> {
    T create(int containerId, Inventory inventory, BlockPos pos);
}
