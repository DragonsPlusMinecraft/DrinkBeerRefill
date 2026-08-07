package lekavar.lma.drinkbeer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegistrySnapshotTest {
    private static final List<NamedRegistry> REGISTRIES = List.of(
            new NamedRegistry("block", BuiltInRegistries.BLOCK),
            new NamedRegistry("item", BuiltInRegistries.ITEM),
            new NamedRegistry("mob_effect", BuiltInRegistries.MOB_EFFECT),
            new NamedRegistry("sound_event", BuiltInRegistries.SOUND_EVENT),
            new NamedRegistry("particle_type", BuiltInRegistries.PARTICLE_TYPE),
            new NamedRegistry("block_entity_type", BuiltInRegistries.BLOCK_ENTITY_TYPE),
            new NamedRegistry("menu", BuiltInRegistries.MENU),
            new NamedRegistry("recipe_type", BuiltInRegistries.RECIPE_TYPE),
            new NamedRegistry("recipe_serializer", BuiltInRegistries.RECIPE_SERIALIZER),
            new NamedRegistry("data_component_type", BuiltInRegistries.DATA_COMPONENT_TYPE),
            new NamedRegistry("creative_mode_tab", BuiltInRegistries.CREATIVE_MODE_TAB)
    );

    @Test
    void registryIdsMatchTheCrossLoaderSnapshot() throws IOException {
        List<String> actual = new ArrayList<>();
        for (NamedRegistry named : REGISTRIES) {
            named.registry().keySet().stream()
                    .filter(id -> id.getNamespace().equals(DrinkBeer.MOD_ID))
                    .map(id -> named.name() + " " + id)
                    .forEach(actual::add);
        }
        actual.sort(String::compareTo);

        InputStream stream = RegistrySnapshotTest.class.getResourceAsStream("/registry-ids.txt");
        assertNotNull(stream, () -> "Missing registry snapshot. Current values:\n" + String.join("\n", actual));
        List<String> expected = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                .lines()
                .filter(line -> !line.isBlank())
                .sorted()
                .toList();
        assertEquals(expected, actual);
    }

    private record NamedRegistry(String name, Registry<?> registry) {
    }
}
