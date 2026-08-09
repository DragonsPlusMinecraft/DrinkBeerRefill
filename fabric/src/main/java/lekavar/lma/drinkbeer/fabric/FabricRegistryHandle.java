package lekavar.lma.drinkbeer.fabric;

import lekavar.lma.drinkbeer.platform.RegistryHandle;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

final class FabricRegistryHandle<T> implements RegistryHandle<T> {
    private final Identifier id;
    private final T value;
    private final Holder<T> holder;

    FabricRegistryHandle(Identifier id, T value, Holder<T> holder) {
        this.id = id;
        this.value = value;
        this.holder = holder;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public Holder<T> holder() {
        return holder;
    }
}
