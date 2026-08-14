package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidRegistryTest {
    @Test
    void registersNineVirtualFluidPairsWithStableMappings() {
        assertEquals(9, FluidRegistry.beers().size());
        assertEquals(250, FluidRegistry.SERVING_MILLIBUCKETS);

        for (FluidRegistry.BeerFluid beer : FluidRegistry.beers()) {
            assertEquals(
                    ResourceLocation.fromNamespaceAndPath(DrinkBeer.MOD_ID, beer.path()),
                    BuiltInRegistries.FLUID.getKey(beer.source())
            );
            assertEquals(
                    ResourceLocation.fromNamespaceAndPath(DrinkBeer.MOD_ID, beer.flowingPath()),
                    BuiltInRegistries.FLUID.getKey(beer.flowing())
            );
            assertSame(beer.source(), beer.flowing().getSource());
            assertSame(beer.flowing(), beer.source().getFlowing());
            assertTrue(beer.source().isSame(beer.flowing()));
            assertSame(Items.AIR, beer.source().getBucket());
            assertEquals(beer, FluidRegistry.byFluid(beer.source()).orElseThrow());
            assertEquals(beer, FluidRegistry.byFluid(beer.flowing()).orElseThrow());
            assertEquals(beer, FluidRegistry.byFilledMug(beer.filledMug()).orElseThrow());
            assertEquals(0xFF000000, beer.tintColor() & 0xFF000000);
        }
    }

    @Test
    void onlyTheTwoCreamyDrinksUseTheMilkyAppearance() {
        assertEquals(
                java.util.Set.of("blaze_milk_stout", "frothy_pink_eggnog"),
                FluidRegistry.beers().stream()
                        .filter(beer -> beer.appearance() == FluidRegistry.Appearance.MILKY)
                        .map(FluidRegistry.BeerFluid::path)
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

    @Test
    void mixedBeerIsNotConvertedToAFluid() {
        assertFalse(FluidRegistry.byFilledMug(ItemRegistry.MIXED_BEER.get()).isPresent());
    }

}
