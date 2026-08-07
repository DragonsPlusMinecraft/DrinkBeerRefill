package lekavar.lma.drinkbeer.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class DrunkFrostWalkerStatusEffect extends MobEffect {
    public DrunkFrostWalkerStatusEffect() {
        super(MobEffectCategory.BENEFICIAL, new Color(30, 144, 255, 255).getRGB());
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity instanceof Player && !entity.level().isClientSide()) {
            FrostWalkerEnchantment.onEntityMoved(entity, entity.level(), BlockPos.containing(entity.position()), 1);
        }
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        DrunkStatusEffect.addStatusEffect(entity);
        super.addAttributeModifiers(entity, attributes, amplifier);
    }
}
