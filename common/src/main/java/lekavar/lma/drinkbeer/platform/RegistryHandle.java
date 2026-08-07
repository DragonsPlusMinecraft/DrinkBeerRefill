package lekavar.lma.drinkbeer.platform;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public interface RegistryHandle<T> extends Supplier<T> {
    ResourceLocation id();

    Holder<T> holder();
}
