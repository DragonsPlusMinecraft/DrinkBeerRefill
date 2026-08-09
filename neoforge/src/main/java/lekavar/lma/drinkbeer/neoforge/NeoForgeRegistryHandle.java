package lekavar.lma.drinkbeer.neoforge;

import lekavar.lma.drinkbeer.platform.RegistryHandle;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredHolder;

final class NeoForgeRegistryHandle<B, T extends B> implements RegistryHandle<T> {
    private final DeferredHolder<B, T> holder;

    NeoForgeRegistryHandle(DeferredHolder<B, T> holder) {
        this.holder = holder;
    }

    @Override
    public T get() {
        return holder.get();
    }

    @Override
    public Identifier id() {
        return holder.getId();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Holder<T> holder() {
        return (Holder<T>) (Holder<?>) holder;
    }
}
