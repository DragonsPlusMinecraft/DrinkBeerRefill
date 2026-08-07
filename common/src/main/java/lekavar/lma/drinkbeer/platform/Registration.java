package lekavar.lma.drinkbeer.platform;

import net.minecraft.core.Registry;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class Registration {
    public static <T, R extends T> RegistryHandle<R> register(
            Registry<T> registry,
            String path,
            Supplier<? extends R> factory
    ) {
        return Platform.hooks().register(registry, path, factory);
    }

    public static <T> RegistryProvider<T> provider(Registry<T> registry) {
        return new RegistryProvider<>(registry);
    }

    public static <T extends AbstractContainerMenu> RegistryHandle<MenuType<T>> registerMenu(
            String path,
            ExtendedMenuFactory<T> factory
    ) {
        return Platform.hooks().registerMenu(path, factory);
    }

    @SafeVarargs
    public static <T extends BlockEntity> RegistryHandle<BlockEntityType<T>> registerBlockEntityType(
            String path,
            BlockEntityFactory<T> factory,
            Supplier<? extends Block>... validBlocks
    ) {
        return Platform.hooks().registerBlockEntityType(path, factory, validBlocks);
    }

    private Registration() {
    }
}
