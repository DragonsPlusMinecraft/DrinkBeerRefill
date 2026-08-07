package lekavar.lma.drinkbeer.utils.beer;

import lekavar.lma.drinkbeer.registries.BlockRegistry;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BeersTest {
    @Test
    void mapsRecipeBoardsByRegistryId() {
        assertEquals(Beers.BEER_MUG, Beers.byRecipeBoardBlock(BlockRegistry.RECIPE_BOARD_BEER_MUG.get()));
        assertEquals(
                Beers.BEER_MUG_APPLE_LAMBIC,
                Beers.byRecipeBoardBlock(BlockRegistry.RECIPE_BOARD_BEER_MUG_APPLE_LAMBIC.get())
        );
        assertNull(Beers.byRecipeBoardBlock(Blocks.STONE));
    }
}
