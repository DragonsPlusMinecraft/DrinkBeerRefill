package lekavar.lma.drinkbeer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DrinkBeerConfigTest {
    @Test
    void valuesClampToTheSharedLoaderIndependentRanges() {
        assertEquals(
                new DrinkBeerConfig.Values(0.0D, false, 0),
                new DrinkBeerConfig.Values(-1.0D, false, -1)
        );
        assertEquals(
                new DrinkBeerConfig.Values(1.0D, true, 32768),
                new DrinkBeerConfig.Values(2.0D, true, 40000)
        );
    }

    @Test
    void nonFiniteSaturationFallsBackToTheDefault() {
        assertEquals(
                DrinkBeerConfig.DEFAULT_BEER_SATURATION_MODIFIER,
                new DrinkBeerConfig.Values(Double.NaN, true, 1).beerSaturationModifier()
        );
        assertEquals(
                DrinkBeerConfig.DEFAULT_BEER_SATURATION_MODIFIER,
                new DrinkBeerConfig.Values(Double.POSITIVE_INFINITY, true, 1).beerSaturationModifier()
        );
    }
}
