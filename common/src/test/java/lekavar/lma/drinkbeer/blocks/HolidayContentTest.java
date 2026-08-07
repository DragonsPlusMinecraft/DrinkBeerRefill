package lekavar.lma.drinkbeer.blocks;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HolidayContentTest {
    @Test
    void allElevenUpstreamRegistryIdsAreStable() {
        List<Block> blocks = List.of(
                BlockRegistry.GIFT_RED.get(), BlockRegistry.GIFT_BLUE.get(),
                BlockRegistry.GIFT_GREEN.get(), BlockRegistry.GIFT_WHITE.get(),
                BlockRegistry.COLORED_LIGHTS.get(), BlockRegistry.SIDE_COLORED_LIGHTS.get(),
                BlockRegistry.STAR_OF_BETHLEHEM.get(), BlockRegistry.THE_GREAT_STAR_OF_BETHLEHEM.get(),
                BlockRegistry.HORSE_MODEL_1.get(), BlockRegistry.HORSE_MODEL_2.get(), BlockRegistry.HORSE_MODEL_3.get()
        );
        assertEquals(
                List.of(
                        "gift_red", "gift_blue", "gift_green", "gift_white",
                        "colored_lights", "side_colored_lights",
                        "star_of_bethlehem", "the_great_star_of_bethlehem",
                        "horse_model_1", "horse_model_2", "horse_model_3"
                ),
                blocks.stream().map(BuiltInRegistries.BLOCK::getKey).map(id -> {
                    assertEquals(DrinkBeer.MOD_ID, id.getNamespace());
                    return id.getPath();
                }).toList()
        );
    }

    @Test
    void randomAppearanceStateSpacesAreComplete() {
        assertEquals(16, BlockRegistry.GIFT_RED.get().getStateDefinition().getPossibleStates().size());
        assertEquals(32, BlockRegistry.COLORED_LIGHTS.get().getStateDefinition().getPossibleStates().size());
        assertEquals(32, BlockRegistry.SIDE_COLORED_LIGHTS.get().getStateDefinition().getPossibleStates().size());
        assertEquals(8, BlockRegistry.HORSE_MODEL_1.get().getStateDefinition().getPossibleStates().size());
    }

    @Test
    void lightsAndStarsExposeTheirUpstreamPhysicalProperties() {
        var centerLights = BlockRegistry.COLORED_LIGHTS.get().defaultBlockState();
        var sideLights = BlockRegistry.SIDE_COLORED_LIGHTS.get().defaultBlockState();
        assertEquals(15, centerLights.getLightEmission());
        assertEquals(15, sideLights.getLightEmission());
        assertTrue(centerLights.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty());
        assertTrue(sideLights.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty());
        assertEquals(15, BlockRegistry.STAR_OF_BETHLEHEM.get().defaultBlockState()
                .getLightEmission());
        assertEquals(15, BlockRegistry.THE_GREAT_STAR_OF_BETHLEHEM.get().defaultBlockState()
                .getLightEmission());
        assertTrue(ItemRegistry.STAR_OF_BETHLEHEM.get().getDefaultInstance().has(DataComponents.FIRE_RESISTANT));
        assertTrue(ItemRegistry.THE_GREAT_STAR_OF_BETHLEHEM.get().getDefaultInstance().has(DataComponents.FIRE_RESISTANT));
    }

    @Test
    void allFourHolidaySoundsUseUpstreamIds() {
        assertEquals(
                List.of("gift_open_sound", "neigh1_sound", "neigh2_sound", "bell_sound"),
                List.of(SoundEventRegistry.GIFT_OPEN.get(), SoundEventRegistry.NEIGH_1.get(),
                                SoundEventRegistry.NEIGH_2.get(), SoundEventRegistry.BELL.get())
                        .stream().map(BuiltInRegistries.SOUND_EVENT::getKey).map(id -> id.getPath()).toList()
        );
    }
}
