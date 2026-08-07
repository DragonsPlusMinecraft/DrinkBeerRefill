package lekavar.lma.drinkbeer.fabric;

import lekavar.lma.drinkbeer.platform.RegistryHandle;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

final class FabricRegistryHandle<T> implements RegistryHandle<T> {
    private final ResourceLocation id;
    private final T value;
    private final Holder<T> holder;

    FabricRegistryHandle(ResourceLocation id, T value, Holder<T> holder) {
        this.id = id;
        this.value = value;
        this.holder = holder;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public Holder<T> holder() {
        return holder;
    }
}
