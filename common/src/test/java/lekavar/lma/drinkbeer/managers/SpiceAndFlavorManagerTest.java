package lekavar.lma.drinkbeer.managers;

import lekavar.lma.drinkbeer.utils.mixedbeer.MixedBeerOnUsing;
import net.minecraft.world.effect.MobEffects;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpiceAndFlavorManagerTest {
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

    private static MixedBeerOnUsing drinkWithEffect(int duration) {
        MixedBeerOnUsing drink = new MixedBeerOnUsing();
        drink.addStatusEffect(List.of(Pair.of(MobEffects.HASTE.value(), duration)));
        return drink;
    }
}
