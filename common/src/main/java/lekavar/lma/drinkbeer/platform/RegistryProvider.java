package lekavar.lma.drinkbeer.platform;

import net.minecraft.core.Registry;

import java.util.function.Supplier;

public final class RegistryProvider<T> {
    private final Registry<T> registry;

    RegistryProvider(Registry<T> registry) {
        this.registry = registry;
    }

    public <R extends T> RegistryHandle<R> register(String path, Supplier<? extends R> factory) {
        return Registration.register(registry, path, factory);
    }
}
