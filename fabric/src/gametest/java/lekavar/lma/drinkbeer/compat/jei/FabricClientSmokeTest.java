package lekavar.lma.drinkbeer.compat.jei;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.gui.BeerBarrelScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.phys.BlockHitResult;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("UnstableApiUsage")
public final class FabricClientSmokeTest implements FabricClientGameTest {
    private static final int CLIENT_TIMEOUT_TICKS = 6_000;
    private static final long REQUIRED_FRAMES = 200;
    private static final Duration REQUIRED_STABLE_TIME = Duration.ofSeconds(15);
    private static final String EXPECTED_BACKEND = System.getProperty("drinkbeer.expectedBackend", "OpenGL");

    @Override
    public void runTest(ClientGameTestContext context) {
        Path evidenceDirectory = Path.of("client-test-evidence").toAbsolutePath();
        writeLifecycle(evidenceDirectory, "probe_started");
        AtomicLong renderedWorldFrames = new AtomicLong();
        context.runOnClient(client -> WorldRenderEvents.END_MAIN.register(renderContext -> {
            if (client.level != null && client.player != null) {
                renderedWorldFrames.incrementAndGet();
            }
        }));

        Path worldScreenshot;
        Path f3Screenshot;
        Path guiScreenshot;
        Path jeiScreenshot;
        Path saveDirectory;
        String worldName;
        String playerPosition;
        String backend;
        String backendDetails;
        int jeiRecipeCount;
        long verifiedFrames;
        long stableNanos;

        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            saveDirectory = singleplayer.getWorldSave().getSaveDirectory();
            worldName = saveDirectory.getFileName().toString();

            singleplayer.getServer().runCommand("gamemode creative @p");
            singleplayer.getServer().runCommand("time set noon");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("execute at @p run fill ~-6 ~-1 ~-2 ~6 ~-1 ~8 minecraft:smooth_stone");
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~ ~2 drinkbeer:beer_barrel");
            singleplayer.getServer().runCommand("execute at @p run setblock ~-3 ~ ~3 drinkbeer:bartending_table_normal");
            singleplayer.getServer().runCommand("execute at @p run setblock ~1 ~ ~3 drinkbeer:mixed_beer");
            singleplayer.getServer().runCommand("execute at @p run data merge block ~1 ~ ~3 {MixedBeer:{beerId:5,spiceList:[I;1,2]}}");
            singleplayer.getServer().runCommand("execute at @p run setblock ~3 ~ ~3 drinkbeer:colored_lights");
            singleplayer.getServer().runCommand("execute at @p run setblock ~4 ~ ~3 drinkbeer:star_of_bethlehem");
            singleplayer.getServer().runCommand("execute at @p run setblock ~-4 ~ ~3 drinkbeer:gift_red");
            singleplayer.getServer().runCommand("execute at @p run tp @p ~ ~ ~-1.5 0 18");
            context.waitTicks(20);
            singleplayer.getClientWorld().waitForChunksRender();

            context.waitFor(client -> client.level != null
                    && client.player != null
                    && client.getConnection() != null
                    && client.getSingleplayerServer() != null
                    && client.level.getChunkSource().hasChunk(
                    client.player.blockPosition().getX() >> 4,
                    client.player.blockPosition().getZ() >> 4
            ), CLIENT_TIMEOUT_TICKS);
            writeLifecycle(evidenceDirectory, "world_ready");

            long stableStart = System.nanoTime();
            long firstFrame = renderedWorldFrames.get();
            context.waitFor(client -> System.nanoTime() - stableStart >= REQUIRED_STABLE_TIME.toNanos()
                    && renderedWorldFrames.get() - firstFrame >= REQUIRED_FRAMES, CLIENT_TIMEOUT_TICKS);
            stableNanos = System.nanoTime() - stableStart;
            verifiedFrames = renderedWorldFrames.get() - firstFrame;

            worldScreenshot = context.takeScreenshot("drinkbeer-world");
            writeLifecycle(evidenceDirectory, "world_stable");
            context.waitTicks(20);

            context.runOnClient(client -> client.debugEntries.setOverlayVisible(true));
            context.waitFor(client -> client.getDebugOverlay().showDebugScreen(), 200);
            context.waitTicks(5);
            f3Screenshot = context.takeScreenshot("drinkbeer-f3");
            writeLifecycle(evidenceDirectory, "f3");
            context.waitTicks(20);
            context.runOnClient(client -> client.debugEntries.setOverlayVisible(false));

            context.waitFor(client -> client.hitResult instanceof BlockHitResult hit
                    && BuiltInRegistries.BLOCK.getKey(client.level.getBlockState(hit.getBlockPos()).getBlock())
                    .toString().equals("drinkbeer:beer_barrel"), 400);
            context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            context.waitForScreen(BeerBarrelScreen.class);
            context.waitTicks(5);
            guiScreenshot = context.takeScreenshot("drinkbeer-gui");
            writeLifecycle(evidenceDirectory, "gui");
            context.waitTicks(20);

            context.waitFor(client -> Plugin.runtimeForTesting() != null, 1_200);
            jeiRecipeCount = Math.toIntExact(Plugin.runtimeForTesting().getRecipeManager()
                    .createRecipeLookup(JEIBrewingRecipeCategory.TYPE).get().count());
            if (jeiRecipeCount != 9) {
                throw new AssertionError("Expected 9 JEI brewing recipes, got " + jeiRecipeCount);
            }
            context.runOnClient(client -> Plugin.runtimeForTesting().getRecipesGui()
                    .showTypes(List.of(JEIBrewingRecipeCategory.TYPE)));
            context.waitFor(client -> client.screen != null
                    && !(client.screen instanceof BeerBarrelScreen), 400);
            context.waitTicks(5);
            jeiScreenshot = context.takeScreenshot("drinkbeer-jei");
            writeLifecycle(evidenceDirectory, "jei");
            context.waitTicks(20);

            playerPosition = context.computeOnClient(client -> String.format(
                    java.util.Locale.ROOT,
                    "%.3f,%.3f,%.3f",
                    client.player.getX(), client.player.getY(), client.player.getZ()
            ));
            backend = context.computeOnClient(client -> RenderSystem.getDevice().getBackendName());
            backendDetails = context.computeOnClient(client -> RenderSystem.getDevice().getImplementationInformation());
            if (!backend.toLowerCase(java.util.Locale.ROOT)
                    .contains(EXPECTED_BACKEND.toLowerCase(java.util.Locale.ROOT))) {
                throw new AssertionError("Expected graphics backend " + EXPECTED_BACKEND + ", got " + backend);
            }
            context.waitTicks(10);
            writeLifecycle(evidenceDirectory, "saving");
        }

        writeLifecycle(evidenceDirectory, "saved");
        Path fixtureDirectory = evidenceDirectory.resolve("world-fixture");
        try {
            normalizeFixtureDataPacks(saveDirectory.resolve("level.dat"));
            replaceDirectory(saveDirectory, fixtureDirectory);
            Files.createDirectories(evidenceDirectory);

            JsonObject marker = new JsonObject();
            marker.addProperty("status", "success");
            marker.addProperty("processId", ProcessHandle.current().pid());
            marker.addProperty("minecraftVersion", SharedConstants.getCurrentVersion().id());
            marker.addProperty("loader", "fabric");
            marker.addProperty("loaderVersion", modVersion("fabricloader"));
            marker.addProperty("modVersion", modVersion(DrinkBeer.MOD_ID));
            marker.addProperty("worldName", worldName);
            marker.addProperty("playerPosition", playerPosition);
            marker.addProperty("renderedFrames", verifiedFrames);
            marker.addProperty("stableSeconds", stableNanos / 1_000_000_000.0D);
            marker.addProperty("graphicsBackend", backend);
            marker.addProperty("graphicsImplementation", backendDetails);
            marker.addProperty("jeiRecipeCount", jeiRecipeCount);
            marker.addProperty("worldScreenshot", worldScreenshot.toAbsolutePath().toString());
            marker.addProperty("f3Screenshot", f3Screenshot.toAbsolutePath().toString());
            marker.addProperty("guiScreenshot", guiScreenshot.toAbsolutePath().toString());
            marker.addProperty("jeiScreenshot", jeiScreenshot.toAbsolutePath().toString());
            marker.addProperty("worldFixture", fixtureDirectory.toString());
            Files.writeString(
                    evidenceDirectory.resolve("success.json"),
                    new GsonBuilder().setPrettyPrinting().create().toJson(marker)
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write Fabric client smoke-test evidence", exception);
        }
    }

    private static void writeLifecycle(Path evidenceDirectory, String phase) {
        try {
            Files.createDirectories(evidenceDirectory);
            JsonObject process = new JsonObject();
            process.addProperty("processId", ProcessHandle.current().pid());
            process.addProperty("loader", "fabric");
            process.addProperty("startedAtEpochMillis", System.currentTimeMillis());
            Files.writeString(evidenceDirectory.resolve("process.json"), process.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            JsonObject lifecycle = new JsonObject();
            lifecycle.addProperty("processId", ProcessHandle.current().pid());
            lifecycle.addProperty("loader", "fabric");
            lifecycle.addProperty("phase", phase);
            lifecycle.addProperty("epochMillis", System.currentTimeMillis());
            Path temporary = evidenceDirectory.resolve("phase.json.tmp");
            Files.writeString(temporary, lifecycle.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temporary, evidenceDirectory.resolve("phase.json"),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, evidenceDirectory.resolve("phase.json"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write Fabric client lifecycle marker " + phase, exception);
        }
    }

    private static String modVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .orElseThrow(() -> new AssertionError("Missing required mod " + modId))
                .getMetadata().getVersion().getFriendlyString();
    }

    /**
     * Fabric records required loader packs in level.dat even though they are
     * automatically supplied on every Fabric load. A NeoForge load uses
     * different required-pack names, so retaining those selections creates
     * misleading "missing data pack" warnings. The compatibility fixture keeps
     * every non-loader pack selection and all world data, but drops only the
     * Fabric-provided entries before the fixture is copied.
     */
    private static void normalizeFixtureDataPacks(Path levelDat) throws IOException {
        CompoundTag root = NbtIo.readCompressed(levelDat, NbtAccounter.unlimitedHeap());
        CompoundTag data = root.getCompoundOrEmpty("Data");
        CompoundTag dataPacks = data.getCompoundOrEmpty("DataPacks");
        ListTag enabled = dataPacks.getListOrEmpty("Enabled");
        ListTag normalized = new ListTag();

        for (int index = 0; index < enabled.size(); index++) {
            String packId = enabled.getStringOr(index, "");
            if (!packId.equals(DrinkBeer.MOD_ID) && !packId.startsWith("fabric-")) {
                normalized.add(StringTag.valueOf(packId));
            }
        }
        if (normalized.isEmpty()) {
            normalized.add(StringTag.valueOf("vanilla"));
        }

        dataPacks.put("Enabled", normalized);
        data.put("DataPacks", dataPacks);
        root.put("Data", data);
        NbtIo.writeCompressed(root, levelDat);
    }

    private static void replaceDirectory(Path source, Path destination) throws IOException {
        if (Files.exists(destination)) {
            try (var paths = Files.walk(destination)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
        try (var paths = Files.walk(source)) {
            for (Path path : paths.toList()) {
                if (path.getFileName().toString().equals("session.lock")) {
                    continue;
                }
                Path target = destination.resolve(source.relativize(path).toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
        }
    }
}
