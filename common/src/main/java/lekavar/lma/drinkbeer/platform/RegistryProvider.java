package lekavar.lma.drinkbeer.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;
import java.util.function.Supplier;

public final class RegistryProvider<T> {
    private final Registry<T> registry;

    RegistryProvider(Registry<T> registry) {
        this.registry = registry;
    }

    public <R extends T> RegistryHandle<R> register(String path, Supplier<? extends R> factory) {
        return Registration.register(registry, path, factory);
    }

    public <R extends T> RegistryHandle<R> registerWithKey(
            String path,
            Function<ResourceKey<T>, ? extends R> factory
    ) {
        return Registration.registerWithKey(registry, path, factory);
    }
}
