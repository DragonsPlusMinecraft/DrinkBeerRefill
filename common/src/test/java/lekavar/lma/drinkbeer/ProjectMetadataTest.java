package lekavar.lma.drinkbeer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectMetadataTest {
    private static final Path ROOT = Path.of(System.getProperty("drinkbeer.projectDir")).getParent();

    @Test
    void neoForgeRangeUsesCompileVersionAsOpenEndedMinimum() throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(ROOT.resolve("gradle.properties"))) {
            properties.load(reader);
        }

        String neoVersion = properties.getProperty("neo_version");
        assertEquals("[" + neoVersion + ",)", properties.getProperty("neo_version_range"));
    }
}
