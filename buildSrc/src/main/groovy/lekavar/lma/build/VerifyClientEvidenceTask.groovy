package lekavar.lma.build

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class VerifyClientEvidenceTask extends DefaultTask {
    @InputFiles
    abstract ConfigurableFileCollection getSuccessMarkers()

    @Input
    abstract Property<String> getExpectedMinecraftVersion()

    @Input
    abstract Property<String> getExpectedModVersion()

    @Input
    abstract Property<String> getExpectedBackend()

    @Input
    abstract MapProperty<String, String> getExpectedLoaderVersions()

    @Input
    abstract ListProperty<String> getExpectedLoaders()

    @Input
    abstract Property<Boolean> getRequireWindowsWatchdog()

    @OutputFile
    abstract RegularFileProperty getReportFile()

    @TaskAction
    void verifyEvidence() {
        def require = { boolean condition, String message ->
            if (!condition) {
                throw new IllegalStateException(message)
            }
        }
        def parser = new JsonSlurper()
        List<Map<String, Object>> verified = []
        Set<String> foundLoaders = []

        successMarkers.files.sort { it.absolutePath }.each { File markerFile ->
            require(markerFile.isFile(), "Missing client success marker ${markerFile}")
            def marker = parser.parse(markerFile)
            String loader = marker.loader as String
            foundLoaders << loader

            require(marker.status == 'success', "Client marker is not successful: ${markerFile}")
            require(marker.minecraftVersion == expectedMinecraftVersion.get(),
                    "Wrong Minecraft version in ${markerFile}: ${marker.minecraftVersion}")
            require(marker.loaderVersion == expectedLoaderVersions.get().get(loader),
                    "Wrong ${loader} version in ${markerFile}: ${marker.loaderVersion}")
            require(marker.modVersion == "${expectedModVersion.get()}+${loader}",
                    "Wrong mod version in ${markerFile}: ${marker.modVersion}")
            require((marker.graphicsBackend as String).toLowerCase(Locale.ROOT)
                    .contains(expectedBackend.get().toLowerCase(Locale.ROOT)),
                    "Wrong graphics backend in ${markerFile}: ${marker.graphicsBackend}")
            require((marker.renderedFrames as long) >= 200L,
                    "Fewer than 200 stable frames in ${markerFile}")
            require((marker.stableSeconds as double) >= 15.0D,
                    "Less than 15 stable seconds in ${markerFile}")
            require((marker.jeiRecipeCount as int) == 9,
                    "Expected 9 JEI recipes in ${markerFile}, got ${marker.jeiRecipeCount}")
            require((marker.processId as long) > 0L, "Missing exact client PID in ${markerFile}")

            ['worldScreenshot', 'f3Screenshot', 'guiScreenshot', 'jeiScreenshot'].each { property ->
                File screenshot = new File(marker[property] as String)
                require(screenshot.isFile() && screenshot.length() > 1024L,
                        "Missing or empty ${property} from ${markerFile}: ${screenshot}")
            }

            File failureMarker = new File(markerFile.parentFile, 'failure.json')
            require(!failureMarker.exists(), "Failure marker exists beside ${markerFile}")

            if (requireWindowsWatchdog.get()) {
                File watchdogFile = new File(markerFile.parentFile, 'watchdog.json')
                require(watchdogFile.isFile(), "Missing Windows watchdog evidence ${watchdogFile}")
                def watchdog = parser.parse(watchdogFile)
                require(watchdog.status == 'success', "Watchdog failed for ${markerFile}: ${watchdog.message}")
                require((watchdog.processId as long) == (marker.processId as long),
                        "Watchdog PID does not match client marker for ${markerFile}")
                require((watchdog.windowTitle as String).toLowerCase(Locale.ROOT).contains('minecraft'),
                        "Watchdog did not identify a Minecraft window for ${markerFile}")
                List captures = watchdog.captures as List
                require(captures.size() >= 5, "Watchdog captured too few window states for ${markerFile}")
                captures.each { capture ->
                    File screenshot = new File(capture as String)
                    require(screenshot.isFile() && screenshot.length() > 1024L,
                            "Missing external window capture ${screenshot}")
                }
            }

            verified << [
                    loader         : loader,
                    processId      : marker.processId,
                    renderedFrames : marker.renderedFrames,
                    stableSeconds  : marker.stableSeconds,
                    graphicsBackend: marker.graphicsBackend,
                    worldName      : marker.worldName
            ]
        }

        require(foundLoaders == expectedLoaders.get().toSet(),
                "Expected client evidence for ${expectedLoaders.get()}, found ${foundLoaders}")

        File report = reportFile.get().asFile
        report.parentFile.mkdirs()
        report.text = JsonOutput.prettyPrint(JsonOutput.toJson([status: 'success', clients: verified]))
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message)
        }
    }
}
