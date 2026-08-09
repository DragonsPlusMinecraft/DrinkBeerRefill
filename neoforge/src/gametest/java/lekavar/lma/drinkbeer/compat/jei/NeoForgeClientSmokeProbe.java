package lekavar.lma.drinkbeer.compat.jei;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.gui.BeerBarrelScreen;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Test-source-only smoke probe used by the {@code clientSmoke} ModDev run.
 * This class is compiled from the gameTest source set and is never packaged in
 * the release jar.
 */
@EventBusSubscriber(modid = DrinkBeer.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientSmokeProbe {
    private static final boolean ENABLED = Boolean.getBoolean("drinkbeer.clientProbe");
    private static final String WORLD_DIRECTORY = System.getProperty("drinkbeer.clientWorld", "drinkbeer-smoke");
    private static final String EXPECTED_BACKEND = System.getProperty("drinkbeer.expectedBackend", "OpenGL");
    private static final Duration WORLD_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration STABLE_DURATION = Duration.ofSeconds(15);
    private static final long REQUIRED_FRAMES = 200;
    private static final long STAGE_TIMEOUT_TICKS = 600;

    private static Stage stage = Stage.WAIT_WORLD;
    private static long firstTickNanos;
    private static long stableStartNanos;
    private static long stableNanos;
    private static long renderedFrames;
    private static long firstStableFrame;
    private static long verifiedFrames;
    private static long clientTicks;
    private static long stageStartedTick;
    private static long delayedUntilTick;
    private static long screenshotSettleUntilTick;
    private static int jeiRecipeCount;
    private static String playerPosition;
    private static String levelName;
    private static String backend;
    private static String backendDetails;

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Post event) {
        if (!ENABLED) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (worldReady(client)) {
            renderedFrames++;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ENABLED || stage == Stage.DONE) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        clientTicks++;

        try {
            if (firstTickNanos == 0L) {
                firstTickNanos = System.nanoTime();
                stageStartedTick = clientTicks;
                writeLifecycle(client, "probe_started");
                DrinkBeer.LOGGER.info("NeoForge client smoke probe started; initial screen is {}",
                        client.gui.screen() == null ? "none" : client.gui.screen().getClass().getName());
            }
            if (stage != Stage.WAIT_EXIT
                    && System.nanoTime() - firstTickNanos > WORLD_TIMEOUT.toNanos()) {
                fail(client, new AssertionError("NeoForge client smoke test exceeded five minutes"));
                return;
            }
            if (stage != Stage.WAIT_WORLD && stage != Stage.STABILIZING && stage != Stage.WAIT_EXIT
                    && clientTicks - stageStartedTick > STAGE_TIMEOUT_TICKS) {
                fail(client, new AssertionError("Timed out in client probe stage " + stage));
                return;
            }

            switch (stage) {
                case WAIT_WORLD -> waitForWorld(client);
                case STABILIZING -> stabilizeWorld(client);
                case WAIT_WORLD_SCREENSHOT -> waitForScreenshot(client, "drinkbeer-world.png", Stage.ENABLE_F3);
                case ENABLE_F3 -> enableF3(client);
                case WAIT_F3_DELAY -> waitForF3Delay(client);
                case WAIT_F3_SCREENSHOT -> waitForScreenshot(client, "drinkbeer-f3.png", Stage.OPEN_GUI);
                case OPEN_GUI -> openGui(client);
                case WAIT_GUI -> waitForGui(client);
                case WAIT_GUI_SCREENSHOT -> waitForScreenshot(client, "drinkbeer-gui.png", Stage.OPEN_JEI);
                case OPEN_JEI -> openJei(client);
                case WAIT_JEI -> waitForJei(client);
                case WAIT_JEI_SCREENSHOT -> waitForScreenshot(client, "drinkbeer-jei.png", Stage.WRITE_SUCCESS);
                case WRITE_SUCCESS -> writeSuccessAndDisconnect(client);
                case WAIT_EXIT -> {
                    // disconnectFromWorld performs the save synchronously. This
                    // state prevents its nested render loop from re-entering the
                    // completion stage.
                }
                case DONE -> {
                }
            }
        } catch (Throwable throwable) {
            fail(client, throwable);
        }
    }

    private static void waitForWorld(Minecraft client) {
        if (!worldReady(client)) {
            return;
        }
        stableStartNanos = System.nanoTime();
        firstStableFrame = renderedFrames;
        writeLifecycle(client, "world_ready");
        transition(Stage.STABILIZING);
    }

    private static void stabilizeWorld(Minecraft client) throws IOException {
        if (!worldReady(client)) {
            transition(Stage.WAIT_WORLD);
            return;
        }
        long elapsed = System.nanoTime() - stableStartNanos;
        long frames = renderedFrames - firstStableFrame;
        if (elapsed < STABLE_DURATION.toNanos() || frames < REQUIRED_FRAMES) {
            return;
        }
        stableNanos = elapsed;
        verifiedFrames = frames;
        capture(client, "drinkbeer-world.png");
        writeLifecycle(client, "world_stable");
        transition(Stage.WAIT_WORLD_SCREENSHOT);
    }

    private static void enableF3(Minecraft client) {
        client.debugEntries.setOverlayVisible(true);
        delayedUntilTick = clientTicks + 5;
        transition(Stage.WAIT_F3_DELAY);
    }

    private static void waitForF3Delay(Minecraft client) throws IOException {
        if (clientTicks < delayedUntilTick || !client.getDebugOverlay().showDebugScreen()) {
            return;
        }
        capture(client, "drinkbeer-f3.png");
        writeLifecycle(client, "f3");
        transition(Stage.WAIT_F3_SCREENSHOT);
    }

    private static void openGui(Minecraft client) {
        client.debugEntries.setOverlayVisible(false);
        if (!(client.hitResult instanceof BlockHitResult hit)) {
            throw new AssertionError("Client is not targeting the smoke-test beer barrel");
        }
        String blockId = BuiltInRegistries.BLOCK.getKey(client.level.getBlockState(hit.getBlockPos()).getBlock()).toString();
        if (!blockId.equals("drinkbeer:beer_barrel")) {
            throw new AssertionError("Expected targeted block drinkbeer:beer_barrel, got " + blockId);
        }
        client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
        transition(Stage.WAIT_GUI);
    }

    private static void waitForGui(Minecraft client) throws IOException {
        if (!(client.gui.screen() instanceof BeerBarrelScreen)) {
            return;
        }
        capture(client, "drinkbeer-gui.png");
        writeLifecycle(client, "gui");
        transition(Stage.WAIT_GUI_SCREENSHOT);
    }

    private static void openJei(Minecraft client) {
        if (Plugin.runtimeForTesting() == null) {
            return;
        }
        jeiRecipeCount = Math.toIntExact(Plugin.runtimeForTesting().getRecipeManager()
                .createRecipeLookup(JEIBrewingRecipeCategory.TYPE).get().count());
        if (jeiRecipeCount != 9) {
            throw new AssertionError("Expected 9 JEI brewing recipes, got " + jeiRecipeCount);
        }
        Plugin.runtimeForTesting().getRecipesGui().showTypes(List.of(JEIBrewingRecipeCategory.TYPE));
        transition(Stage.WAIT_JEI);
    }

    private static void waitForJei(Minecraft client) throws IOException {
        if (client.gui.screen() == null || client.gui.screen() instanceof BeerBarrelScreen) {
            return;
        }
        capture(client, "drinkbeer-jei.png");
        writeLifecycle(client, "jei");
        transition(Stage.WAIT_JEI_SCREENSHOT);
    }

    private static void waitForScreenshot(Minecraft client, String fileName, Stage next) {
        if (!Files.isRegularFile(screenshotPath(client, fileName))) {
            return;
        }
        if (screenshotSettleUntilTick == 0L) {
            screenshotSettleUntilTick = clientTicks + 20;
            return;
        }
        if (clientTicks >= screenshotSettleUntilTick) {
            screenshotSettleUntilTick = 0L;
            transition(next);
        }
    }

    private static void writeSuccessAndDisconnect(Minecraft client) throws IOException {
        var device = RenderSystem.getDevice().getDeviceInfo();
        backend = device.backendName();
        backendDetails = device.name() + " | " + device.vendorName() + " | " + device.driverInfo();
        if (!backend.toLowerCase(Locale.ROOT).contains(EXPECTED_BACKEND.toLowerCase(Locale.ROOT))) {
            throw new AssertionError("Expected graphics backend " + EXPECTED_BACKEND + ", got " + backend);
        }

        playerPosition = String.format(Locale.ROOT, "%.3f,%.3f,%.3f",
                client.player.getX(), client.player.getY(), client.player.getZ());
        levelName = client.getSingleplayerServer().getWorldData().getLevelName();

        JsonObject marker = new JsonObject();
        marker.addProperty("status", "success");
        marker.addProperty("processId", ProcessHandle.current().pid());
        marker.addProperty("minecraftVersion", SharedConstants.getCurrentVersion().id());
        marker.addProperty("loader", "neoforge");
        marker.addProperty("loaderVersion", modVersion("neoforge"));
        marker.addProperty("modVersion", modVersion(DrinkBeer.MOD_ID));
        marker.addProperty("worldName", WORLD_DIRECTORY);
        marker.addProperty("levelName", levelName);
        marker.addProperty("playerPosition", playerPosition);
        marker.addProperty("renderedFrames", verifiedFrames);
        marker.addProperty("stableSeconds", stableNanos / 1_000_000_000.0D);
        marker.addProperty("graphicsBackend", backend);
        marker.addProperty("graphicsImplementation", backendDetails);
        marker.addProperty("jeiRecipeCount", jeiRecipeCount);
        marker.addProperty("worldScreenshot", screenshotPath(client, "drinkbeer-world.png").toString());
        marker.addProperty("f3Screenshot", screenshotPath(client, "drinkbeer-f3.png").toString());
        marker.addProperty("guiScreenshot", screenshotPath(client, "drinkbeer-gui.png").toString());
        marker.addProperty("jeiScreenshot", screenshotPath(client, "drinkbeer-jei.png").toString());
        client.gui.setScreen(null);
        writeLifecycle(client, "saving");
        transition(Stage.WAIT_EXIT);
        client.disconnectFromWorld(Component.literal("Drink Beer Refill client smoke test complete"));
        if (client.level != null || client.getSingleplayerServer() != null) {
            throw new AssertionError("Client retained a world or integrated server after saving disconnect");
        }
        writeLifecycle(client, "saved");
        writeMarker(client, "success.json", marker);
        stage = Stage.DONE;
        client.stop();
    }

    private static boolean worldReady(Minecraft client) {
        if (client.level == null || client.player == null || client.getConnection() == null
                || client.getSingleplayerServer() == null) {
            return false;
        }
        int chunkX = client.player.blockPosition().getX() >> 4;
        int chunkZ = client.player.blockPosition().getZ() >> 4;
        return client.level.getChunkSource().hasChunk(chunkX, chunkZ);
    }

    private static void capture(Minecraft client, String fileName) throws IOException {
        Path path = screenshotPath(client, fileName);
        Files.createDirectories(path.getParent());
        Files.deleteIfExists(path);
        Screenshot.grab(client.gameDirectory, fileName, client.gameRenderer.mainRenderTarget(), 1,
                message -> DrinkBeer.LOGGER.info("NeoForge client probe screenshot {}: {}", fileName, message.getString()));
    }

    private static Path screenshotPath(Minecraft client, String fileName) {
        return client.gameDirectory.toPath().toAbsolutePath().resolve("screenshots").resolve(fileName);
    }

    private static String modVersion(String modId) {
        return ModList.get().getModContainerById(modId)
                .orElseThrow(() -> new AssertionError("Missing required mod " + modId))
                .getModInfo().getVersion().toString();
    }

    private static void writeMarker(Minecraft client, String fileName, JsonObject marker) throws IOException {
        Path evidence = client.gameDirectory.toPath().toAbsolutePath().resolve("client-test-evidence");
        Files.createDirectories(evidence);
        Files.writeString(evidence.resolve(fileName),
                new GsonBuilder().setPrettyPrinting().create().toJson(marker));
    }

    private static void writeLifecycle(Minecraft client, String phase) {
        try {
            Path evidence = client.gameDirectory.toPath().toAbsolutePath().resolve("client-test-evidence");
            Files.createDirectories(evidence);

            JsonObject process = new JsonObject();
            process.addProperty("processId", ProcessHandle.current().pid());
            process.addProperty("loader", "neoforge");
            process.addProperty("startedAtEpochMillis", System.currentTimeMillis());
            Files.writeString(evidence.resolve("process.json"), process.toString());

            JsonObject lifecycle = new JsonObject();
            lifecycle.addProperty("processId", ProcessHandle.current().pid());
            lifecycle.addProperty("loader", "neoforge");
            lifecycle.addProperty("phase", phase);
            lifecycle.addProperty("epochMillis", System.currentTimeMillis());
            Path temporary = evidence.resolve("phase.json.tmp");
            Files.writeString(temporary, lifecycle.toString());
            try {
                Files.move(temporary, evidence.resolve("phase.json"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, evidence.resolve("phase.json"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write NeoForge client lifecycle marker " + phase, exception);
        }
    }

    private static void fail(Minecraft client, Throwable throwable) {
        try {
            JsonObject marker = new JsonObject();
            marker.addProperty("status", "failure");
            marker.addProperty("processId", ProcessHandle.current().pid());
            marker.addProperty("stage", stage.name());
            marker.addProperty("message", throwable.toString());
            StringWriter trace = new StringWriter();
            throwable.printStackTrace(new PrintWriter(trace));
            marker.addProperty("stackTrace", trace.toString());
            writeMarker(client, "failure.json", marker);
        } catch (Throwable markerFailure) {
            DrinkBeer.LOGGER.error("Unable to write NeoForge client probe failure marker", markerFailure);
        }
        DrinkBeer.LOGGER.error("NeoForge client smoke probe failed in stage {}", stage, throwable);
        stage = Stage.DONE;
        client.stop();
    }

    private static void transition(Stage next) {
        stage = next;
        stageStartedTick = clientTicks;
        DrinkBeer.LOGGER.info("NeoForge client smoke probe entered stage {}", next);
    }

    private enum Stage {
        WAIT_WORLD,
        STABILIZING,
        WAIT_WORLD_SCREENSHOT,
        ENABLE_F3,
        WAIT_F3_DELAY,
        WAIT_F3_SCREENSHOT,
        OPEN_GUI,
        WAIT_GUI,
        WAIT_GUI_SCREENSHOT,
        OPEN_JEI,
        WAIT_JEI,
        WAIT_JEI_SCREENSHOT,
        WRITE_SUCCESS,
        WAIT_EXIT,
        DONE
    }

    private NeoForgeClientSmokeProbe() {
    }
}
