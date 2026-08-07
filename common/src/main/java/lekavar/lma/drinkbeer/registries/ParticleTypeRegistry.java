package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.platform.Registration;
import lekavar.lma.drinkbeer.platform.RegistryProvider;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;


public class ParticleTypeRegistry {
    private static final RegistryProvider<ParticleType<?>> PARTICLES = Registration.provider(BuiltInRegistries.PARTICLE_TYPE);
    public static final Supplier<ParticleType<SimpleParticleType>> MIXED_BEER_DEFAULT = PARTICLES.register("mixed_beer_default", DrinkBeerSimpleParticleType::new);
    public static final Supplier<ParticleType<SimpleParticleType>> CALL_BELL_TINKLE_PAW = PARTICLES.register("call_bell_tinkle_paw", DrinkBeerSimpleParticleType::new);

    private static final class DrinkBeerSimpleParticleType extends SimpleParticleType {
        private DrinkBeerSimpleParticleType() {
            super(true);
        }
    }

    public static void init() {
    }
}
