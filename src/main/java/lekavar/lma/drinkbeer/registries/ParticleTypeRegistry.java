package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class ParticleTypeRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, DrinkBeer.MOD_ID);
    public static final Supplier<ParticleType<SimpleParticleType>> MIXED_BEER_DEFAULT = PARTICLES.register("mixed_beer_default", () -> new SimpleParticleType(true));
    public static final Supplier<ParticleType<SimpleParticleType>> CALL_BELL_TINKLE_PAW = PARTICLES.register("call_bell_tinkle_paw", () -> new SimpleParticleType(true));

}
