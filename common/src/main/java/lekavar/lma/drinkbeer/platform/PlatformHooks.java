package lekavar.lma.drinkbeer.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public interface PlatformHooks {
    <T, R extends T> RegistryHandle<R> register(Registry<T> registry, String path, Supplier<? extends R> factory);

    <T extends AbstractContainerMenu> RegistryHandle<MenuType<T>> registerMenu(
            String path,
            ExtendedMenuFactory<T> factory
    );

    <T extends BlockEntity> RegistryHandle<BlockEntityType<T>> registerBlockEntityType(
            String path,
            BlockEntityFactory<T> factory,
            Supplier<? extends Block>... validBlocks
    );

    void registerNetworking();

    void registerConfig();

    void openMenu(Player player, MenuProvider provider, BlockPos pos);

    void sendRefreshTradeBox(BlockPos pos);
}
