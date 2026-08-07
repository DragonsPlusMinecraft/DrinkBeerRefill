package lekavar.lma.drinkbeer.utils.beer;

import lekavar.lma.drinkbeer.effects.NightHowlStatusEffect;
import lekavar.lma.drinkbeer.utils.gift.GiftRewards;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class BeerConsumptionEffects {
    public static void finishStandardDrink(BeerDefinition definition, Level level, LivingEntity consumer) {
        finishDrink(definition, level, consumer, true);
    }

    public static void finishMixedDrink(BeerDefinition definition, Level level, LivingEntity consumer) {
        finishDrink(definition, level, consumer, false);
    }

    private static void finishDrink(BeerDefinition definition, Level level, LivingEntity consumer, boolean addDynamicEffect) {
        if (level.isClientSide()) {
            return;
        }

        if (definition.specialAction() == BeerSpecialAction.NIGHT_HOWL) {
            if (addDynamicEffect) {
                NightHowlStatusEffect.addStatusEffect(level, consumer);
            }
            NightHowlStatusEffect.playRandomHowlSound(level, consumer);
        } else if (definition.specialAction() == BeerSpecialAction.FROTHY_GIFT && consumer instanceof Player player) {
            GiftRewards.giveOrDrop(player, GiftRewards.randomColoredGift(level.getRandom()));
        }
    }

    private BeerConsumptionEffects() {
    }
}
