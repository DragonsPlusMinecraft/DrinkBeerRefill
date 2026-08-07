package lekavar.lma.drinkbeer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
    private static final String MOD_ID = "drinkbeer";
    private static final Path RESOURCES = Path.of(System.getProperty("drinkbeer.projectDir"))
            .resolve(Path.of("src", "main", "resources"));
    private static final Set<String> EXPECTED_BLOCK_IDS = Set.of(
            "bartending_table_normal", "beer_barrel", "beer_mug", "beer_mug_apple_lambic",
            "beer_mug_blaze_milk_stout", "beer_mug_blaze_stout", "beer_mug_frothy_pink_eggnog",
            "beer_mug_haars_icey_pale_lager", "beer_mug_night_howl_kvass", "beer_mug_pumpkin_kvass",
            "beer_mug_sweet_berry_kriek", "colored_lights", "empty_beer_mug", "gift_blue", "gift_green",
            "gift_red", "gift_white", "golden_call_bell", "horse_model_1", "horse_model_2",
            "horse_model_3", "iron_call_bell", "lekas_call_bell", "recipe_board_beer_mug",
            "recipe_board_beer_mug_apple_lambic", "recipe_board_beer_mug_blaze_milk_stout",
            "recipe_board_beer_mug_blaze_stout", "recipe_board_beer_mug_frothy_pink_eggnog",
            "recipe_board_beer_mug_haars_icey_pale_lager", "recipe_board_beer_mug_night_howl_kvass",
            "recipe_board_beer_mug_pumpkin_kvass", "recipe_board_beer_mug_sweet_berry_kriek",
            "recipe_board_package", "side_colored_lights", "spice_amethyst_nigella_seeds",
            "spice_blaze_paprika", "spice_citrine_nigella_seeds", "spice_dried_eglia_bud",
            "spice_dried_selaginella", "spice_frozen_persimmon", "spice_glace_goji_berries",
            "spice_golden_cinnamon_powder", "spice_ice_mint", "spice_ice_patchouli",
            "spice_roasted_pecans", "spice_roasted_red_pine_nuts", "spice_silver_needle_white_tea",
            "spice_smoked_eglia_bud", "spice_storm_shards", "star_of_bethlehem",
            "the_great_star_of_bethlehem", "trade_box_normal"
    );

    @Test
    void blockLootTablesUseTheMinecraft1201DirectoryAndStableIds() throws IOException {
        Path data = RESOURCES.resolve(Path.of("data", MOD_ID));
        Path blockLootDirectory = data.resolve(Path.of("loot_tables", "blocks"));

        assertFalse(Files.exists(data.resolve("loot_table")));
        assertTrue(Files.isDirectory(blockLootDirectory));
        Set<String> lootTableNames;
        try (var files = Files.list(blockLootDirectory)) {
            lootTableNames = files
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString().replaceFirst("\\.json$", ""))
                    .collect(Collectors.toSet());
        }

        assertEquals(52, lootTableNames.size());
        assertEquals(EXPECTED_BLOCK_IDS, lootTableNames);
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
    void bundledRecipesUseTheMinecraft1201Schema() throws IOException {
        Path data = RESOURCES.resolve(Path.of("data", MOD_ID));
        Path recipeDirectory = data.resolve("recipes");
        assertFalse(Files.exists(data.resolve("recipe")));

        List<Path> recipes;
        try (var paths = Files.list(recipeDirectory)) {
            recipes = paths.filter(path -> path.toString().endsWith(".json")).toList();
        }

        for (Path recipePath : recipes) {
            String source = Files.readString(recipePath);
            assertFalse(source.contains("\"id\""), recipePath.toString());
            assertFalse(source.contains("\"c:"), recipePath.toString());
            try (Reader reader = Files.newBufferedReader(recipePath)) {
                var recipe = JsonParser.parseReader(reader).getAsJsonObject();
                assertTrue(recipe.getAsJsonObject("result").has("item"), recipePath.toString());
            }
        }

        List<Path> brewingRecipes = recipes.stream()
                .filter(path -> path.getFileName().toString().startsWith("beer_mug"))
                .toList();
        assertEquals(9, brewingRecipes.size());
        for (Path recipePath : brewingRecipes) {
            try (Reader reader = Files.newBufferedReader(recipePath)) {
                var recipe = JsonParser.parseReader(reader).getAsJsonObject();
                assertEquals("drinkbeer:brewing", recipe.get("type").getAsString(), recipePath.toString());
                assertEquals(4, recipe.getAsJsonArray("ingredients").size(), recipePath.toString());
                assertTrue(recipe.get("brewing_time").getAsInt() > 0, recipePath.toString());
                assertTrue(recipe.getAsJsonObject("cup").has("item"), recipePath.toString());

                int cupCount = recipe.getAsJsonObject("cup").get("count").getAsInt();
                int resultCount = recipe.getAsJsonObject("result").get("count").getAsInt();
                assertTrue(cupCount > 0, recipePath.toString());
                assertEquals(cupCount, resultCount, recipePath.toString());
            }
        }
    }

    @Test
    void dataDirectoriesAndPackMetadataMatchMinecraft1201() throws IOException {
        Path data = RESOURCES.resolve("data");
        assertTrue(Files.isDirectory(data.resolve(Path.of(MOD_ID, "recipes"))));
        assertTrue(Files.isDirectory(data.resolve(Path.of(MOD_ID, "loot_tables"))));
        assertTrue(Files.isDirectory(data.resolve(Path.of(MOD_ID, "structures"))));
        assertTrue(Files.isDirectory(data.resolve(Path.of(MOD_ID, "tags", "items"))));
        assertTrue(Files.isDirectory(data.resolve(Path.of("forge", "tags", "items"))));
        assertTrue(Files.isDirectory(data.resolve(Path.of("minecraft", "tags", "items"))));
        assertFalse(Files.exists(data.resolve("c")));
        assertFalse(Files.exists(data.resolve(Path.of(MOD_ID, "structure"))));
        assertFalse(Files.exists(data.resolve(Path.of(MOD_ID, "tags", "item"))));
        assertFalse(Files.exists(RESOURCES.resolve(Path.of("META-INF", "neoforge.mods.toml"))));
        assertFalse(Files.exists(data.resolve(Path.of(MOD_ID, "data_components"))));

        try (Reader reader = Files.newBufferedReader(RESOURCES.resolve("pack.mcmeta"))) {
            var pack = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("pack");
            assertEquals(15, pack.get("pack_format").getAsInt());
        }
    }

    @Test
    void englishAndChineseLanguageFilesExposeTheSameKeys() throws IOException {
        Path languageDirectory = RESOURCES.resolve(Path.of("assets", MOD_ID, "lang"));
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
            assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("assets", MOD_ID, "blockstates", blockId + ".json"))), blockId);
            assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("assets", MOD_ID, "models", "item", blockId + ".json"))), blockId);
            assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("data", MOD_ID, "loot_tables", "blocks", blockId + ".json"))), blockId);
        }

        for (String recipe : List.of("colored_lights", "side_colored_lights", "star_of_bethlehem", "the_great_star_of_bethlehem")) {
            assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("data", MOD_ID, "recipes", recipe + ".json"))), recipe);
        }

        for (String sound : List.of("gift_open_sound", "neigh1_sound", "neigh2_sound", "bell_sound")) {
            assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("assets", MOD_ID, "sounds", sound + ".ogg"))), sound);
        }

        assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of("data", MOD_ID, "tags", "items", "beers.json"))));
    }

    @Test
    void localClientResourceReferencesResolve() throws IOException {
        Path assets = RESOURCES.resolve(Path.of("assets", MOD_ID));
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

    private static void assertLocalAssetReference(Path assets, String reference, String directory,
                                                  String extension, Path source) {
        String prefix = MOD_ID + ":";
        if (!reference.startsWith(prefix)) {
            return;
        }
        Path target = assets.resolve(directory).resolve(reference.substring(prefix.length()) + extension);
        assertTrue(Files.isRegularFile(target), () -> target + " is referenced by " + source);
    }
}
