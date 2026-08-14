package lekavar.lma.drinkbeer.neoforge.client;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/** Adds Create's milk sprites to the block atlas without redistributing them. */
final class CreateMilkTextureCompat {
    private static final String CREATE_MOD_ID = "create";
    private static final List<String> REQUIRED_TEXTURES = List.of(
            "assets/create/textures/fluid/milk_still.png",
            "assets/create/textures/fluid/milk_flow.png"
    );
    private static final ResourceLocation PACK_LOCATION = ResourceLocation.fromNamespaceAndPath(
            DrinkBeer.MOD_ID,
            "resourcepacks/create_milk_atlas"
    );

    private static Boolean createMilkTexturesAvailable;

    static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || !canUseCreateMilkTextures()) {
            return;
        }

        event.addPackFinders(
                PACK_LOCATION,
                PackType.CLIENT_RESOURCES,
                Component.translatable("pack.drinkbeer.create_milk_atlas"),
                PackSource.BUILT_IN,
                true,
                Pack.Position.TOP
        );
    }

    static boolean canUseCreateMilkTextures() {
        if (createMilkTexturesAvailable == null) {
            createMilkTexturesAvailable = detectCreateMilkTextures();
        }
        return createMilkTexturesAvailable;
    }

    private static boolean detectCreateMilkTextures() {
        ModList modList = ModList.get();
        if (modList == null || !modList.isLoaded(CREATE_MOD_ID)) {
            return false;
        }

        var modFile = modList.getModFileById(CREATE_MOD_ID);
        return modFile != null && hasExpectedMilkTextures(modFile.getFile()::findResource);
    }

    static boolean hasExpectedMilkTextures(Function<String, Path> resourceFinder) {
        return REQUIRED_TEXTURES.stream()
                .map(resourceFinder)
                .allMatch(path -> path != null && Files.isRegularFile(path));
    }

    private CreateMilkTextureCompat() {
    }
}
