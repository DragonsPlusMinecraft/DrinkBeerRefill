package lekavar.lma.drinkbeer.effects;

import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;


public class NightHowlStatusEffect {
    private final static int BASE_NIGHT_VISION_TIME = 2400;

    public static void addStatusEffect(ItemStack stack, Level world, LivingEntity user) {
        if (stack.getItem() == ItemRegistry.BEER_MUG_NIGHT_HOWL_KVASS.get()) {
            addStatusEffect(world, user);
            playRandomHowlSound(world, user);
        }
    }

    public static void addStatusEffect(Level world, LivingEntity user) {
        // Duration is longest at full moon and shortest at new moon.
        user.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, getNightVisionTime(getMoonPhase(world))));
    }

    public static Pair<MobEffect, Integer> getStatusEffectPair(Level world) {
        return Pair.of(MobEffects.NIGHT_VISION.value(), getNightVisionTime(getMoonPhase(world)));
    }

    public static void playRandomHowlSound(Level world, LivingEntity user) {
        if (!world.isClientSide) {
            world.playSound(null, user.blockPosition(),
                    SoundEventRegistry.NIGHT_HOWL[world.getRandom().nextInt(SoundEventRegistry.NIGHT_HOWL.length)].get(),
                    SoundSource.PLAYERS, 1.2f, 1f);
        }
    }

    public static int getNightVisionTime(int moonPhase) {
        if (moonPhase < 0 || moonPhase > 7) {
            throw new IllegalArgumentException("Moon phase must be between 0 and 7");
        }
        return BASE_NIGHT_VISION_TIME + (moonPhase == 0 ? Math.abs(moonPhase - 1 - 4) * 1200 : Math.abs(moonPhase - 4) * 1200);
    }

    public static int getMoonPhase(Level world) {
        long timeOfDay = world.getDayTime();
        return (int) (timeOfDay / 24000L % 8L + 8L) % 8;
    }
}
