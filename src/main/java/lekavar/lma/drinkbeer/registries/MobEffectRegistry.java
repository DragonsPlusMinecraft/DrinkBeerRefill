package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.effects.DrunkFrostWalkerStatusEffect;
import lekavar.lma.drinkbeer.effects.DrunkStatusEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;


public class MobEffectRegistry {
    public static final DeferredRegister<MobEffect> STATUS_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, DrinkBeer.MOD_ID);
    public static final RegistryObject<MobEffect> DRUNK_FROST_WALKER = STATUS_EFFECTS.register("drunk_frost_walker", DrunkFrostWalkerStatusEffect::new);
    public static final RegistryObject<MobEffect> DRUNK = STATUS_EFFECTS.register("drunk", DrunkStatusEffect::new);
}

