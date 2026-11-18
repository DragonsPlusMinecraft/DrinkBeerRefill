package lekavar.lma.drinkbeer.fabric;

import lekavar.lma.drinkbeer.DrinkBeerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FabricServerConfigTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void missingFieldsUseDefaults() throws IOException {
        Path config = write("{\"enableWorldChangingFlavorEffects\": false}");

        DrinkBeerConfig.Values values = FabricServerConfig.load(config);

        assertEquals(DrinkBeerConfig.DEFAULT_BEER_SATURATION_MODIFIER, values.beerSaturationModifier());
        assertFalse(values.enableWorldChangingFlavorEffects());
        assertEquals(DrinkBeerConfig.DEFAULT_MAX_WORLD_CHANGES_PER_DRINK, values.maxWorldChangesPerDrink());
    }

    @Test
    void outOfRangeValuesAreClampedWithoutRewritingTheFile() throws IOException {
        String contents = """
                {
                  "beerSaturationModifier": -4.0,
                  "enableWorldChangingFlavorEffects": false,
                  "maxWorldChangesPerDrink": 99999
                }
                """;
        Path config = write(contents);

        DrinkBeerConfig.Values values = FabricServerConfig.load(config);

        assertEquals(0.0D, values.beerSaturationModifier());
        assertFalse(values.enableWorldChangingFlavorEffects());
        assertEquals(32768, values.maxWorldChangesPerDrink());
        assertEquals(contents, Files.readString(config));
    }

    @Test
    void damagedFileFallsBackWithoutOverwritingIt() throws IOException {
        String contents = "{ this is not valid JSON";
        Path config = write(contents);

        assertEquals(DrinkBeerConfig.defaults(), FabricServerConfig.load(config));
        assertEquals(contents, Files.readString(config));
    }

    private Path write(String contents) throws IOException {
        return Files.writeString(temporaryDirectory.resolve("drinkbeer-server.json"), contents);
    }
}
