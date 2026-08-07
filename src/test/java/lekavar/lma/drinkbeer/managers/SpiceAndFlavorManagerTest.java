package lekavar.lma.drinkbeer.managers;

import lekavar.lma.drinkbeer.utils.mixedbeer.Flavors;
import lekavar.lma.drinkbeer.utils.mixedbeer.MixedBeerOnUsing;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.world.effect.MobEffects;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpiceAndFlavorManagerTest {
    @Test
    void allFifteenSpicesRetainTheirUpstreamFlavorMapping() {
        assertEquals(
                List.of(
                        Flavors.FIERY, Flavors.SPICY, Flavors.FIERY,
                        Flavors.AROMATIC, Flavors.AROMATIC1,
                        Flavors.REFRESHING, Flavors.REFRESHING1,
                        Flavors.STORMY, Flavors.NUTTY, Flavors.SWEET,
                        Flavors.LUSCIOUS, Flavors.NUTTY1, Flavors.MELLOW,
                        Flavors.SWEET, Flavors.DRYING
                ),
                Arrays.stream(Spices.values()).map(Spices::getFlavor).toList()
        );
    }

    @Test
    void aromaticAndSpicyRemainOrderSensitive() {
        MixedBeerOnUsing aromaticThenSpicy = drinkWithEffect(100);
        aromaticThenSpicy.setSpiceList(List.of(4, 2));
        SpiceAndFlavorManager.applyFlavorValue(aromaticThenSpicy);

        MixedBeerOnUsing spicyThenAromatic = drinkWithEffect(100);
        spicyThenAromatic.setSpiceList(List.of(2, 4));
        SpiceAndFlavorManager.applyFlavorValue(spicyThenAromatic);

        assertEquals(1620, aromaticThenSpicy.getStatusEffectList().getFirst().getValue());
        assertEquals(980, spicyThenAromatic.getStatusEffectList().getFirst().getValue());
        assertEquals(-3.0F, aromaticThenSpicy.getHealth());
        assertEquals(-3.0F, spicyThenAromatic.getHealth());
    }

    @Test
    void valueChangingFlavorsMatchUpstreamNumbers() {
        MixedBeerOnUsing drink = drinkWithEffect(100);
        drink.setSpiceList(List.of(3, 5, 7, 12, 11, 13));
        SpiceAndFlavorManager.applyFlavorValue(drink);

        assertEquals(-1, drink.getDrunkValue());
        assertEquals(5, drink.getHunger());
        assertEquals(0.0F, drink.getHealth()); // -4 fiery +4 luscious
        assertEquals(1400, drink.getStatusEffectList().stream()
                .filter(effect -> effect.getKey() == MobEffects.DIG_SPEED.value())
                .findFirst().orElseThrow().getValue());
        assertEquals(1600, drink.getStatusEffectList().stream()
                .filter(effect -> effect.getKey() == MobEffects.DAMAGE_RESISTANCE.value())
                .findFirst().orElseThrow().getValue());
    }

    @Test
    void fatherFlavorNormalizationProducesAllThreeUpstreamCombinations() {
        assertEquals(Flavors.SOOOOO_SPICY, SpiceAndFlavorManager.getCombinedFlavor(List.of(1, 2, 3)));
        assertEquals(Flavors.THE_FALL_OF_THE_GIANT, SpiceAndFlavorManager.getCombinedFlavor(List.of(8, 8, 8)));
        assertEquals(Flavors.CLOYING, SpiceAndFlavorManager.getCombinedFlavor(List.of(10, 11, 14)));
    }

    private static MixedBeerOnUsing drinkWithEffect(int duration) {
        MixedBeerOnUsing drink = new MixedBeerOnUsing();
        drink.addStatusEffect(List.of(Pair.of(MobEffects.DIG_SPEED.value(), duration)));
        return drink;
    }
}
