package lekavar.lma.drinkbeer.effects;

import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.ReplaceDisk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;

public class DrunkFrostWalkerStatusEffect extends MobEffect {
    private final static ReplaceDisk REPLACE_EFFECT = new ReplaceDisk(
            new LevelBasedValue.Clamped(LevelBasedValue.perLevel(3.0F, 1.0F), 0.0F, 16.0F),
            LevelBasedValue.constant(1.0F),
            new Vec3i(0, -1, 0),
            Optional.of(
                    BlockPredicate.allOf(
                            BlockPredicate.matchesTag(new Vec3i(0, 1, 0), BlockTags.AIR),
                            BlockPredicate.matchesBlocks(Blocks.WATER),
                            BlockPredicate.matchesFluids(Fluids.WATER),
                            BlockPredicate.unobstructed()
                    )
            ),
            BlockStateProvider.simple(Blocks.FROSTED_ICE),
            Optional.of(GameEvent.BLOCK_PLACE)
    );

    public DrunkFrostWalkerStatusEffect() {
        super(MobEffectCategory.BENEFICIAL, new Color(30, 144, 255, 255).getRGB());
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity instanceof Player && !entity.level().isClientSide()) {
            REPLACE_EFFECT.apply((ServerLevel) entity.level(), 1, null, entity, entity.position());
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void onEffectAdded(LivingEntity livingEntity, int amplifier) {
        DrunkStatusEffect.addStatusEffect(livingEntity);
    }
}
