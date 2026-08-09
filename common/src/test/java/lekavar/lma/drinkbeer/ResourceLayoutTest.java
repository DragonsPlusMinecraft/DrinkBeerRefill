package lekavar.lma.drinkbeer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceLayoutTest {
    private static final Path RESOURCES = Path.of(System.getProperty("drinkbeer.projectDir"))
            .resolve(Path.of("src", "main", "resources"));

    @Test
    void blockLootTablesUseTheMinecraft121Directory() throws IOException {
        Path obsoleteDirectory = RESOURCES.resolve(Path.of("data", DrinkBeer.MOD_ID, "loot_table", "block"));
        Path blockLootDirectory = RESOURCES.resolve(Path.of("data", DrinkBeer.MOD_ID, "loot_table", "blocks"));

        assertFalse(Files.exists(obsoleteDirectory));
        assertTrue(Files.isDirectory(blockLootDirectory));
        Set<String> lootTableNames;
        try (var files = Files.list(blockLootDirectory)) {
            lootTableNames = files
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString().replaceFirst("\\.json$", ""))
                    .collect(Collectors.toSet());
        }

        Set<String> blocksRequiringTables = BuiltInRegistries.BLOCK.keySet().stream()
                .filter(id -> id.getNamespace().equals(DrinkBeer.MOD_ID))
                .map(id -> id.getPath())
                .filter(path -> !path.equals("mixed_beer")) // Mixed beer supplies a component-preserving dynamic drop.
                .collect(Collectors.toSet());

        assertEquals(52, lootTableNames.size());
        assertEquals(blocksRequiringTables, lootTableNames);
    }

    @Test
    void everyJsonResourceParses() throws IOException {
        List<Path> jsonFiles;
        try (var paths = Files.walk(RESOURCES)) {
            jsonFiles = paths.filter(path -> path.toString().endsWith(".json")).toList();
        }

        assertFalse(jsonFiles.isEmpty());
        for (Path jsonFile : jsonFiles) {
            try (Reader reader = Files.newBufferedReader(jsonFile)) {
                JsonParser.parseReader(reader);
            }
        }
    }

    @Test
    void recipesUseMinecraft12111IngredientSyntax() throws IOException {
        Path recipeDirectory = RESOURCES.resolve(Path.of("data", DrinkBeer.MOD_ID, "recipe"));
        try (var paths = Files.list(recipeDirectory)) {
            for (Path recipePath : paths.filter(path -> path.toString().endsWith(".json")).toList()) {
                JsonElement recipe;
                try (Reader reader = Files.newBufferedReader(recipePath)) {
                    recipe = JsonParser.parseReader(reader);
                }
                assertFalse(containsLegacyIngredientObject(recipe),
                        () -> recipePath + " still uses pre-1.21.11 {item:...}/{tag:...} ingredients");
            }
        }
    }

    @Test
    void bundledBrewingRecipesHaveCompleteInputsAndBalancedOutputCounts() throws IOException {
        Path recipeDirectory = RESOURCES.resolve(Path.of("data", DrinkBeer.MOD_ID, "recipe"));
        List<Path> brewingRecipes;
        try (var paths = Files.list(recipeDirectory)) {
            brewingRecipes = paths
                    .filter(path -> path.getFileName().toString().startsWith("beer_mug"))
                    .filter(path -> path.toString().endsWith(".json"))
                    .toList();
        }

        assertEquals(9, brewingRecipes.size());
        for (Path recipePath : brewingRecipes) {
            try (Reader reader = Files.newBufferedReader(recipePath)) {
                var recipe = JsonParser.parseReader(reader).getAsJsonObject();
                assertEquals("drinkbeer:brewing", recipe.get("type").getAsString(), recipePath.toString());
                assertEquals(4, recipe.getAsJsonArray("ingredients").size(), recipePath.toString());
                assertTrue(recipe.get("brewing_time").getAsInt() > 0, recipePath.toString());

                int cupCount = recipe.getAsJsonObject("cup").get("count").getAsInt();
                int resultCount = recipe.getAsJsonObject("result").get("count").getAsInt();
                assertTrue(cupCount > 0, recipePath.toString());
                assertEquals(cupCount, resultCount, recipePath.toString());
            }
        }
    }

    @Test
    void englishAndChineseLanguageFilesExposeTheSameKeys() throws IOException {
        Path languageDirectory = RESOURCES.resolve(Path.of("assets", DrinkBeer.MOD_ID, "lang"));
        Set<String> englishKeys;
        Set<String> chineseKeys;

        try (Reader reader = Files.newBufferedReader(languageDirectory.resolve("en_us.json"))) {
            englishKeys = JsonParser.parseReader(reader).getAsJsonObject().keySet();
        }
        try (Reader reader = Files.newBufferedReader(languageDirectory.resolve("zh_cn.json"))) {
            chineseKeys = JsonParser.parseReader(reader).getAsJsonObject().keySet();
        }

        assertEquals(englishKeys, chineseKeys);
    }

    @Test
    void upstreamHolidayContentHasCompleteResources() {
        List<String> blockIds = List.of(
                "gift_red", "gift_blue", "gift_green", "gift_white",
                "colored_lights", "side_colored_lights",
                "star_of_bethlehem", "the_great_star_of_bethlehem",
                "horse_model_1", "horse_model_2", "horse_model_3"
        );

        for (String blockId : blockIds) {
            assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("assets", DrinkBeer.MOD_ID, "blockstates", blockId + ".json"))), blockId);
            assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("assets", DrinkBeer.MOD_ID, "models", "item", blockId + ".json"))), blockId);
            assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("data", DrinkBeer.MOD_ID, "loot_table", "blocks", blockId + ".json"))), blockId);
        }

        for (String recipe : List.of("colored_lights", "side_colored_lights", "star_of_bethlehem", "the_great_star_of_bethlehem")) {
            assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("data", DrinkBeer.MOD_ID, "recipe", recipe + ".json"))), recipe);
        }

        for (String sound : List.of("gift_open_sound", "neigh1_sound", "neigh2_sound", "bell_sound")) {
            assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("assets", DrinkBeer.MOD_ID, "sounds", sound + ".ogg"))), sound);
        }

        assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("data", DrinkBeer.MOD_ID, "tags", "item", "beers.json"))));
    }

    @Test
    void localClientResourceReferencesResolve() throws IOException {
        Path assets = RESOURCES.resolve(Path.of("assets", DrinkBeer.MOD_ID));
        Path blockstates = assets.resolve("blockstates");
        try (var files = Files.list(blockstates)) {
            for (Path blockstate : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                JsonElement root;
                try (Reader reader = Files.newBufferedReader(blockstate)) {
                    root = JsonParser.parseReader(reader);
                }
                for (String model : stringProperties(root, "model")) {
                    assertLocalAssetReference(assets, model, "models", ".json", blockstate);
                }
            }
        }

        Path models = assets.resolve("models");
        try (var files = Files.walk(models)) {
            for (Path model : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                JsonElement root;
                try (Reader reader = Files.newBufferedReader(model)) {
                    root = JsonParser.parseReader(reader);
                }
                if (!root.isJsonObject()) {
                    continue;
                }
                var object = root.getAsJsonObject();
                for (String texture : stringProperties(root, "texture")) {
                    assertFalse(texture.equals("#missing"),
                            () -> model + " contains an unresolved Blockbench #missing texture slot");
                }
                if (object.has("parent")) {
                    assertLocalAssetReference(assets, object.get("parent").getAsString(), "models", ".json", model);
                }
                if (object.has("textures")) {
                    for (JsonElement texture : object.getAsJsonObject("textures").asMap().values()) {
                        String reference = texture.getAsString();
                        if (!reference.startsWith("#")) {
                            assertLocalAssetReference(assets, reference, "textures", ".png", model);
                        }
                    }
                }
            }
        }

        Path soundsFile = assets.resolve("sounds.json");
        JsonElement soundsRoot;
        try (Reader reader = Files.newBufferedReader(soundsFile)) {
            soundsRoot = JsonParser.parseReader(reader);
        }
        for (JsonElement event : soundsRoot.getAsJsonObject().asMap().values()) {
            for (JsonElement sound : event.getAsJsonObject().getAsJsonArray("sounds")) {
                String reference = sound.isJsonPrimitive()
                        ? sound.getAsString()
                        : sound.getAsJsonObject().get("name").getAsString();
                assertLocalAssetReference(assets, reference, "sounds", ".ogg", soundsFile);
            }
        }
    }

    private static List<String> stringProperties(JsonElement element, String propertyName) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        collectStringProperties(element, propertyName, values);
        return values;
    }

    private static void collectStringProperties(JsonElement element, String propertyName, List<String> values) {
        if (element.isJsonObject()) {
            for (var entry : element.getAsJsonObject().entrySet()) {
                if (entry.getKey().equals(propertyName) && entry.getValue().isJsonPrimitive()) {
                    values.add(entry.getValue().getAsString());
                }
                collectStringProperties(entry.getValue(), propertyName, values);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                collectStringProperties(child, propertyName, values);
            }
        }
    }

    private static boolean containsLegacyIngredientObject(JsonElement element) {
        if (element.isJsonObject()) {
            var object = element.getAsJsonObject();
            if (object.has("item") || object.has("tag")) {
                return true;
            }
            return object.asMap().values().stream().anyMatch(ResourceLayoutTest::containsLegacyIngredientObject);
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (containsLegacyIngredientObject(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void assertLocalAssetReference(Path assets, String reference, String directory,
                                                  String extension, Path source) {
        String prefix = DrinkBeer.MOD_ID + ":";
        if (!reference.startsWith(prefix)) {
            return;
        }
        Path target = assets.resolve(directory).resolve(reference.substring(prefix.length()) + extension);
        assertTrue(Files.isRegularFile(target), () -> target + " is referenced by " + source);
    }
}
