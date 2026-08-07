package lekavar.lma.drinkbeer.fabric;

import com.google.gson.JsonParser;
import lekavar.lma.drinkbeer.DrinkBeerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricServerConfigTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void missingFileCreatesCompleteDefaults() throws IOException {
        Path config = configPath("new-world");

        assertEquals(DrinkBeerConfig.defaults(), FabricServerConfig.load(config));
        assertTrue(Files.isRegularFile(config));

        var json = JsonParser.parseString(Files.readString(config)).getAsJsonObject();
        assertEquals(DrinkBeerConfig.DEFAULT_BEER_SATURATION_MODIFIER,
                json.get("beerSaturationModifier").getAsDouble());
        assertEquals(DrinkBeerConfig.DEFAULT_ENABLE_WORLD_CHANGING_FLAVOR_EFFECTS,
                json.get("enableWorldChangingFlavorEffects").getAsBoolean());
        assertEquals(DrinkBeerConfig.DEFAULT_MAX_WORLD_CHANGES_PER_DRINK,
                json.get("maxWorldChangesPerDrink").getAsInt());
    }

    @Test
    void missingFieldsUseDefaults() throws IOException {
        Path config = write("partial-world", "{\"enableWorldChangingFlavorEffects\": false}");

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
        Path config = write("clamped-world", contents);

        DrinkBeerConfig.Values values = FabricServerConfig.load(config);

        assertEquals(0.0D, values.beerSaturationModifier());
        assertFalse(values.enableWorldChangingFlavorEffects());
        assertEquals(32768, values.maxWorldChangesPerDrink());
        assertEquals(contents, Files.readString(config));
    }

    @Test
    void damagedFileFallsBackWithoutOverwritingIt() throws IOException {
        String contents = "{ this is not valid JSON";
        Path config = write("damaged-world", contents);

        assertEquals(DrinkBeerConfig.defaults(), FabricServerConfig.load(config));
        assertEquals(contents, Files.readString(config));
    }

    @Test
    void worldsUseIndependentConfigPaths() throws IOException {
        Path first = write("world-a", "{\"beerSaturationModifier\": 0.25}");
        Path second = write("world-b", "{\"beerSaturationModifier\": 0.75}");

        assertEquals(0.25D, FabricServerConfig.load(first).beerSaturationModifier());
        assertEquals(0.75D, FabricServerConfig.load(second).beerSaturationModifier());
        assertEquals(0.25D, FabricServerConfig.load(first).beerSaturationModifier());
    }

    private Path write(String world, String contents) throws IOException {
        Path path = configPath(world);
        Files.createDirectories(path.getParent());
        Files.writeString(path, contents);
        return path;
    }

    private Path configPath(String world) {
        return temporaryDirectory.resolve(world).resolve("serverconfig").resolve("drinkbeer-server.json");
    }
}
