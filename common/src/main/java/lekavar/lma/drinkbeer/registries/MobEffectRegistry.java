package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.effects.DrunkFrostWalkerStatusEffect;
import lekavar.lma.drinkbeer.effects.DrunkStatusEffect;
import lekavar.lma.drinkbeer.platform.Registration;
import lekavar.lma.drinkbeer.platform.RegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;


public class MobEffectRegistry {
    private static final RegistryProvider<MobEffect> STATUS_EFFECTS = Registration.provider(BuiltInRegistries.MOB_EFFECT);
    public static final Holder<MobEffect> DRUNK_FROST_WALKER = STATUS_EFFECTS.<MobEffect>register("drunk_frost_walker", DrunkFrostWalkerStatusEffect::new).holder();
    public static final Holder<MobEffect> DRUNK = STATUS_EFFECTS.<MobEffect>register("drunk", DrunkStatusEffect::new).holder();

    public static void init() {
    }
}
