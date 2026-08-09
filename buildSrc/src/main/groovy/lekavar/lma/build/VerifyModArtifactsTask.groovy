package lekavar.lma.build

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.util.jar.JarFile

abstract class VerifyModArtifactsTask extends DefaultTask {
    @InputFiles
    abstract ConfigurableFileCollection getArtifacts()

    @Input
    abstract Property<String> getExpectedMinecraftVersion()

    @Input
    abstract Property<String> getExpectedModVersion()

    @Input
    abstract Property<Integer> getExpectedJavaVersion()

    @OutputFile
    abstract RegularFileProperty getReportFile()

    @TaskAction
    void verifyArtifacts() {
        def require = { boolean condition, String message ->
            if (!condition) {
                throw new IllegalStateException(message)
            }
        }
        List<Map<String, Object>> verified = []
        artifacts.files.sort { it.name }.each { File artifact ->
            require(artifact.isFile() && artifact.length() > 1024L, "Missing release artifact ${artifact}")
            require(!artifact.name.endsWith('-sources.jar'), "Source jar is not a release artifact: ${artifact}")

            new JarFile(artifact).withCloseable { JarFile jar ->
                List<String> entries = jar.entries().collect { it.name }
                require(entries.size() == entries.toSet().size(), "Duplicate ZIP entries in ${artifact}")
                List<String> forbidden = entries.findAll { String name ->
                    String lower = name.toLowerCase(Locale.ROOT)
                    lower.contains('gametest') || lower.contains('clientsmokeprobe') ||
                            lower.contains('fabricclientsmoketest') || lower.contains('holidaygametestscenarios') ||
                            lower.contains('client-test-evidence') || lower.contains('world-fixture')
                }
                require(forbidden.empty, "Test-only content leaked into ${artifact}: ${forbidden}")

                boolean fabric = entries.contains('fabric.mod.json')
                boolean neoForge = entries.contains('META-INF/neoforge.mods.toml')
                require(fabric ^ neoForge, "Artifact must contain exactly one loader metadata file: ${artifact}")

                if (fabric) {
                    def metadata = new JsonSlurper().parse(jar.getInputStream(jar.getEntry('fabric.mod.json')))
                    require(metadata.version == "${expectedModVersion.get()}+fabric",
                            "Wrong Fabric mod version in ${artifact}: ${metadata.version}")
                    require(metadata.depends.minecraft == "=${expectedMinecraftVersion.get()}",
                            "Fabric metadata does not pin Minecraft ${expectedMinecraftVersion.get()}")
                    require(metadata.depends.java == ">=${expectedJavaVersion.get()}",
                            "Fabric metadata does not require Java ${expectedJavaVersion.get()}")
                } else {
                    String metadata = jar.getInputStream(jar.getEntry('META-INF/neoforge.mods.toml')).getText('UTF-8')
                    require(metadata.contains("version=\"${expectedModVersion.get()}+neoforge\""),
                            "Wrong NeoForge mod version in ${artifact}")
                    require(metadata.contains("versionRange=\"[${expectedMinecraftVersion.get()}]\""),
                            "NeoForge metadata does not pin Minecraft ${expectedMinecraftVersion.get()}")
                    require(metadata.contains("features={java_version=\"[${expectedJavaVersion.get()},)\"}"),
                            "NeoForge metadata does not require Java ${expectedJavaVersion.get()}")
                }

                def manifest = jar.manifest
                require(manifest != null, "Missing manifest in ${artifact}")
                require(manifest.mainAttributes.getValue('Built-On-Minecraft') == expectedMinecraftVersion.get(),
                        "Wrong Built-On-Minecraft manifest value in ${artifact}")
                verified << [file: artifact.absolutePath, bytes: artifact.length(), loader: fabric ? 'fabric' : 'neoforge']
            }
        }
        require(verified*.loader.toSet() == ['fabric', 'neoforge'] as Set,
                "Expected one Fabric and one NeoForge artifact, got ${verified*.loader}")

        File report = reportFile.get().asFile
        report.parentFile.mkdirs()
        report.text = JsonOutput.prettyPrint(JsonOutput.toJson([status: 'success', artifacts: verified]))
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message)
        }
    }
}
