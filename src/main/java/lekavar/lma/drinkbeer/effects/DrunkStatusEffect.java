package lekavar.lma.drinkbeer.effects;

import lekavar.lma.drinkbeer.registries.MobEffectRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;

public class DrunkStatusEffect extends MobEffect {
    public final static int MAX_DRUNK_AMPLIFIER = 4;
    public final static int MIN_DRUNK_AMPLIFIER = 0;
    private final static int BASE_DURATION = 1200;
    private static final boolean VISIBLE = false;
    private static final int[] DRUNK_DURATIONS = {3600, 3000, 2400, 1800, 1200};
    private static final int[] NAUSEA_DURATIONS = {160, 160, 200, 300, 600};
    private static final int[] SLOWNESS_DURATIONS = {0, 80, 160, 200, 600};
    private static final int[] HARMFUL_STATUS_EFFECT_INTERVALS = {200, 160, 200, 300, 20};

    public DrunkStatusEffect() {
        super(MobEffectCategory.HARMFUL, new Color(255, 222, 173, 255).getRGB());
    }

    //Value: > 0:Increase drunk amplifier 0 <:Decrease drunk amplifier
    public static void addStatusEffect(LivingEntity user, int value) {
        if (value == 0) {
            return;
        }

        MobEffectInstance statusEffectInstance = user.getEffect(MobEffectRegistry.DRUNK.get());
        int currentDrunkAmplifier = statusEffectInstance == null
                ? MIN_DRUNK_AMPLIFIER - 1
                : Mth.clamp(statusEffectInstance.getAmplifier(), MIN_DRUNK_AMPLIFIER, MAX_DRUNK_AMPLIFIER);
        long requestedAmplifier = (long) currentDrunkAmplifier + value;
        int newDrunkAmplifier = (int) Math.max(
                MIN_DRUNK_AMPLIFIER - 1L,
                Math.min(MAX_DRUNK_AMPLIFIER, requestedAmplifier)
        );

        if (currentDrunkAmplifier < MIN_DRUNK_AMPLIFIER && newDrunkAmplifier < MIN_DRUNK_AMPLIFIER) {
            return;
        } else if (currentDrunkAmplifier >= MIN_DRUNK_AMPLIFIER && newDrunkAmplifier < MIN_DRUNK_AMPLIFIER) {
            user.removeEffect(MobEffectRegistry.DRUNK.get());
        } else if (currentDrunkAmplifier < MIN_DRUNK_AMPLIFIER) {
            user.addEffect(new MobEffectInstance(MobEffectRegistry.DRUNK.get(), getDrunkDuration(newDrunkAmplifier), newDrunkAmplifier));
        } else {
            if (newDrunkAmplifier > currentDrunkAmplifier) {
                user.addEffect(new MobEffectInstance(MobEffectRegistry.DRUNK.get(), getDrunkDuration(newDrunkAmplifier), newDrunkAmplifier));
            } else if (newDrunkAmplifier < currentDrunkAmplifier) {
                user.removeEffect(MobEffectRegistry.DRUNK.get());
                user.addEffect(new MobEffectInstance(MobEffectRegistry.DRUNK.get(), getDrunkDuration(newDrunkAmplifier), newDrunkAmplifier));
            }
        }
    }

    public static void addStatusEffect(LivingEntity user) {
        addStatusEffect(user, 1);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffectInstance currentEffect = entity.getEffect(MobEffectRegistry.DRUNK.get());
        if (currentEffect == null) {
            return;
        }
        int safeAmplifier = Mth.clamp(amplifier, MIN_DRUNK_AMPLIFIER, MAX_DRUNK_AMPLIFIER);
        int time = currentEffect.getDuration();
        //Always give harmful status effects
        giveHarmfulStatusEffects(entity, safeAmplifier, time);
        //Give next lower Drunk status effect when duration's out
        if (time <= 1) {
            decreaseDrunkStatusEffect(entity, safeAmplifier);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    private void giveHarmfulStatusEffects(LivingEntity entity, int amplifier, int time) {
        if (amplifier >= MAX_DRUNK_AMPLIFIER) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, time, 0, false, VISIBLE));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, time, MAX_DRUNK_AMPLIFIER - 1, false, VISIBLE));
        } else if (time % HARMFUL_STATUS_EFFECT_INTERVALS[amplifier] == 0) {
            int nauseaDuration = NAUSEA_DURATIONS[amplifier];
            int slownessDuration = SLOWNESS_DURATIONS[amplifier];
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, nauseaDuration, 0, false, VISIBLE));
            if (amplifier > 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDuration, amplifier - 1, false, VISIBLE));
            }
        }
    }

    private static void decreaseDrunkStatusEffect(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            entity.removeEffect(MobEffectRegistry.DRUNK.get());
            MobEffectInstance nextDrunkStatusEffect = getDecreasedDrunkStatusEffect(amplifier);
            if (nextDrunkStatusEffect != null) {
                entity.addEffect(nextDrunkStatusEffect);
            }
        }
    }

    private static MobEffectInstance getDecreasedDrunkStatusEffect(int currentAmplifier) {
        int nextDrunkAmplifier = currentAmplifier - 1;
        if (nextDrunkAmplifier < MIN_DRUNK_AMPLIFIER) {
            return null;
        } else {
            return new MobEffectInstance(MobEffectRegistry.DRUNK.get(), getDrunkDuration(nextDrunkAmplifier), nextDrunkAmplifier);
        }
    }

    public static int getNextDrunkAmplifier(LivingEntity user) {
        MobEffectInstance statusEffectInstance = user.getEffect(MobEffectRegistry.DRUNK.get());
        int drunkAmplifier = statusEffectInstance == null ? -1 : statusEffectInstance.getAmplifier();
        return drunkAmplifier < MAX_DRUNK_AMPLIFIER ? drunkAmplifier + 1 : drunkAmplifier;
    }

    public static int getDrunkDuration(int amplifier) {
        return amplifier >= MIN_DRUNK_AMPLIFIER && amplifier <= MAX_DRUNK_AMPLIFIER
                ? DRUNK_DURATIONS[amplifier]
                : BASE_DURATION;
    }

    /**
     * @deprecated Use {@link #getDrunkDuration(int)}. Kept for binary/source compatibility with integrations.
     */
    @Deprecated(forRemoval = false)
    public static int getDrunkDuratioin(int amplifier) {
        return getDrunkDuration(amplifier);
    }
}
