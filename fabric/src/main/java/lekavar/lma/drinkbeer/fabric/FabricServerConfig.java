package lekavar.lma.drinkbeer.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.DrinkBeerConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class FabricServerConfig {
    private static final String FILE_NAME = "drinkbeer-server.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile DrinkBeerConfig.Values current = DrinkBeerConfig.defaults();

    static void register() {
        DrinkBeerConfig.install(() -> current);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> load(
                server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve(FILE_NAME)
        ));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> current = DrinkBeerConfig.defaults());
    }

    static DrinkBeerConfig.Values load(Path path) {
        if (Files.notExists(path)) {
            current = DrinkBeerConfig.defaults();
            writeDefaults(path);
            return current;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            double rawSaturation = json.has("beerSaturationModifier")
                    ? json.get("beerSaturationModifier").getAsDouble()
                    : DrinkBeerConfig.DEFAULT_BEER_SATURATION_MODIFIER;
            boolean worldChanges = json.has("enableWorldChangingFlavorEffects")
                    ? json.get("enableWorldChangingFlavorEffects").getAsBoolean()
                    : DrinkBeerConfig.DEFAULT_ENABLE_WORLD_CHANGING_FLAVOR_EFFECTS;
            int rawMaxChanges = json.has("maxWorldChangesPerDrink")
                    ? json.get("maxWorldChangesPerDrink").getAsInt()
                    : DrinkBeerConfig.DEFAULT_MAX_WORLD_CHANGES_PER_DRINK;

            DrinkBeerConfig.Values loaded = new DrinkBeerConfig.Values(rawSaturation, worldChanges, rawMaxChanges);
            if (loaded.beerSaturationModifier() != rawSaturation) {
                DrinkBeer.LOGGER.warn("Clamped beerSaturationModifier in {} from {} to {}", path,
                        rawSaturation, loaded.beerSaturationModifier());
            }
            if (loaded.maxWorldChangesPerDrink() != rawMaxChanges) {
                DrinkBeer.LOGGER.warn("Clamped maxWorldChangesPerDrink in {} from {} to {}", path,
                        rawMaxChanges, loaded.maxWorldChangesPerDrink());
            }
            current = loaded;
        } catch (IOException | RuntimeException exception) {
            DrinkBeer.LOGGER.warn("Could not read {}; using defaults without overwriting the file", path, exception);
            current = DrinkBeerConfig.defaults();
        }
        return current;
    }

    private static void writeDefaults(Path path) {
        JsonObject json = new JsonObject();
        json.addProperty("beerSaturationModifier", DrinkBeerConfig.DEFAULT_BEER_SATURATION_MODIFIER);
        json.addProperty("enableWorldChangingFlavorEffects", DrinkBeerConfig.DEFAULT_ENABLE_WORLD_CHANGING_FLAVOR_EFFECTS);
        json.addProperty("maxWorldChangesPerDrink", DrinkBeerConfig.DEFAULT_MAX_WORLD_CHANGES_PER_DRINK);
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(json) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            DrinkBeer.LOGGER.warn("Could not create default Fabric server config at {}", path, exception);
        }
    }

    private FabricServerConfig() {
    }
}
