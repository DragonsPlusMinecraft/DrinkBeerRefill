package lekavar.lma.drinkbeer.neoforge.client;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateMilkTextureCompatTest {
    private static final String STILL_TEXTURE = "assets/create/textures/fluid/milk_still.png";
    private static final String FLOWING_TEXTURE = "assets/create/textures/fluid/milk_flow.png";

    @TempDir
    Path temporaryModRoot;

    @Test
    void enablesTheAtlasOnlyWhenBothCreateTexturesExist() throws IOException {
        assertFalse(CreateMilkTextureCompat.hasExpectedMilkTextures(temporaryModRoot::resolve));

        createTexture(STILL_TEXTURE);
        assertFalse(CreateMilkTextureCompat.hasExpectedMilkTextures(temporaryModRoot::resolve));

        createTexture(FLOWING_TEXTURE);
        assertTrue(CreateMilkTextureCompat.hasExpectedMilkTextures(temporaryModRoot::resolve));
        assertFalse(CreateMilkTextureCompat.hasExpectedMilkTextures(path -> null));
    }

    @Test
    void nestedPackOnlyStitchesTheTwoExternalSprites() throws IOException {
        Path pack = projectRoot().resolve(Path.of(
                "neoforge", "src", "main", "resources", "resourcepacks", "create_milk_atlas"
        ));
        Set<String> files;
        try (var paths = Files.walk(pack)) {
            files = paths.filter(Files::isRegularFile)
                    .map(pack::relativize)
                    .map(path -> path.toString().replace('\\', '/'))
                    .collect(Collectors.toSet());
        }
        assertEquals(Set.of("pack.mcmeta", "assets/minecraft/atlases/blocks.json"), files);

        try (Reader reader = Files.newBufferedReader(pack.resolve("pack.mcmeta"))) {
            assertEquals(34, JsonParser.parseReader(reader).getAsJsonObject()
                    .getAsJsonObject("pack").get("pack_format").getAsInt());
        }

        try (Reader reader = Files.newBufferedReader(pack.resolve(
                Path.of("assets", "minecraft", "atlases", "blocks.json")
        ))) {
            var sources = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("sources");
            assertEquals(2, sources.size());
            assertEquals(
                    Set.of("create:fluid/milk_still", "create:fluid/milk_flow"),
                    sources.asList().stream()
                            .map(source -> source.getAsJsonObject().get("resource").getAsString())
                            .collect(Collectors.toSet())
            );
            assertTrue(sources.asList().stream()
                    .allMatch(source -> source.getAsJsonObject().get("type").getAsString().equals("single")));
        }
    }

    private void createTexture(String path) throws IOException {
        Path texture = temporaryModRoot.resolve(path);
        Files.createDirectories(texture.getParent());
        Files.createFile(texture);
    }

    private static Path projectRoot() {
        return Path.of(System.getProperty("drinkbeer.projectDir")).getParent();
    }
}
