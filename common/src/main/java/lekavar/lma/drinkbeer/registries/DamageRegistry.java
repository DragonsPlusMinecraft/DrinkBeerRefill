package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class DamageRegistry {
    public final static ResourceKey<DamageType> ALCOHOL = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(DrinkBeer.MOD_ID, "alcohol"));

    public static DamageSource alcohol(RegistryAccess access) {
        return new DamageSource(
                access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ALCOHOL));
    }
}
