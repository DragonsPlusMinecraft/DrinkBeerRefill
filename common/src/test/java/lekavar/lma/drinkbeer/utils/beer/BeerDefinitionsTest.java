package lekavar.lma.drinkbeer.utils.beer;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeerDefinitionsTest {
    @Test
    void upstreamBeerIdsAndNutritionAreExact() {
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9),
                BeerDefinitions.ALL.stream().map(BeerDefinition::id).toList());
        assertEquals(List.of(2, 1, 1, 1, 1, 1, 9, 2, 4),
                BeerDefinitions.ALL.stream().map(BeerDefinition::nutrition).toList());
        assertEquals(BeerDefinitions.ALL, List.of(Beers.values()).stream().map(Beers::getDefinition).toList());
        for (Beers beer : Beers.values()) {
            assertEquals(16, beer.getBeerItem().getDefaultMaxStackSize(), beer.name());
        }
    }

    @Test
    void fixedStatusEffectsMatchUpstreamDurations() {
        List<Integer> expectedDurations = List.of(1200, 1800, 2400, 300, 400, 1200, 0, 2400, 0);
        for (int i = 0; i < BeerDefinitions.ALL.size(); i++) {
            BeerDefinition definition = BeerDefinitions.ALL.get(i);
            MobEffectInstance effect = definition.effectFactory() == null ? null : definition.effectFactory().get();
            if (expectedDurations.get(i) == 0) {
                assertNull(effect, "beer id " + definition.id());
            } else {
                assertEquals(expectedDurations.get(i).intValue(), effect.getDuration(), "beer id " + definition.id());
                assertEquals(0, effect.getAmplifier(), "beer id " + definition.id());
            }
        }
    }

    @Test
    void saturationSupportsImprovedAndExactUpstreamModes() {
        for (BeerDefinition definition : BeerDefinitions.ALL) {
            FoodProperties exact = definition.foodProperties(0.0F);
            FoodProperties improved = definition.foodProperties(0.1F);
            assertEquals(0.0F, exact.saturation(), 0.0001F, "beer id " + definition.id());
            assertEquals(definition.nutrition() * 0.2F, improved.saturation(), 0.0001F, "beer id " + definition.id());
            assertTrue(improved.canAlwaysEat(), "beer id " + definition.id());
            assertEquals(32, improved.eatDurationTicks(), "beer id " + definition.id());
        }
    }

    @Test
    void specialActionsAreAttachedOnlyToTheirUpstreamBeers() {
        assertAll(
                () -> assertEquals(BeerSpecialAction.FROTHY_GIFT, BeerDefinitions.FROTHY_PINK_EGGNOG.specialAction()),
                () -> assertEquals(BeerSpecialAction.NIGHT_HOWL, BeerDefinitions.NIGHT_HOWL_KVASS.specialAction()),
                () -> assertTrue(BeerDefinitions.ALL.stream()
                        .filter(definition -> definition != BeerDefinitions.FROTHY_PINK_EGGNOG)
                        .filter(definition -> definition != BeerDefinitions.NIGHT_HOWL_KVASS)
                        .allMatch(definition -> definition.specialAction() == BeerSpecialAction.NONE)),
                () -> assertFalse(BeerDefinitions.PUMPKIN_KVASS.hasEffectTooltip())
        );
    }
}
