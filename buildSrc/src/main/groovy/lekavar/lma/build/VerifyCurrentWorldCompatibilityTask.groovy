package lekavar.lma.build

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class VerifyCurrentWorldCompatibilityTask extends DefaultTask {
    @InputDirectory
    abstract DirectoryProperty getSourceFixture()

    @InputDirectory
    abstract DirectoryProperty getReopenedWorld()

    @OutputFile
    abstract RegularFileProperty getReportFile()

    @TaskAction
    void verifyWorlds() {
        File sourceLevel = sourceFixture.file('level.dat').get().asFile
        File reopenedLevel = reopenedWorld.file('level.dat').get().asFile
        requireFile(sourceLevel, 'Fabric source level.dat')
        requireFile(reopenedLevel, 'NeoForge reopened level.dat')

        File sourceRegionDirectory = overworldRegionDirectory(sourceFixture.get().asFile)
        File reopenedRegionDirectory = overworldRegionDirectory(reopenedWorld.get().asFile)
        List<File> sourceRegions = regionFiles(sourceRegionDirectory)
        List<File> reopenedRegions = regionFiles(reopenedRegionDirectory)
        if (sourceRegions.empty || reopenedRegions.empty) {
            throw new IllegalStateException('The same-version cross-loader world is missing overworld region data')
        }
        Set<String> reopenedRegionNames = reopenedRegions.collect { it.name }.toSet()
        List<String> missingRegions = sourceRegions.collect { it.name }.findAll { !reopenedRegionNames.contains(it) }
        if (!missingRegions.empty) {
            throw new IllegalStateException("NeoForge did not preserve Fabric region files: ${missingRegions}")
        }

        File sourceConfig = sourceFixture.file('serverconfig/drinkbeer-server.json').get().asFile
        File reopenedConfig = reopenedWorld.file('serverconfig/drinkbeer-server.json').get().asFile
        requireFile(sourceConfig, 'Fabric same-version server configuration')
        requireFile(reopenedConfig, 'NeoForge-preserved same-version server configuration')
        if (!Arrays.equals(sourceConfig.bytes, reopenedConfig.bytes)) {
            throw new IllegalStateException('NeoForge changed the Fabric same-version server configuration fixture')
        }

        File report = reportFile.get().asFile
        report.parentFile.mkdirs()
        report.text = JsonOutput.prettyPrint(JsonOutput.toJson([
                status              : 'success',
                scope               : 'same-minecraft-version-only',
                source              : sourceLevel.absolutePath,
                reopened            : reopenedLevel.absolutePath,
                sourceRegionCount   : sourceRegions.size(),
                reopenedRegionCount : reopenedRegions.size(),
                sourceRegionLayout  : sourceFixture.get().asFile.toPath().relativize(sourceRegionDirectory.toPath()).toString(),
                reopenedRegionLayout: reopenedWorld.get().asFile.toPath().relativize(reopenedRegionDirectory.toPath()).toString(),
                preservedConfigBytes: reopenedConfig.length()
        ]))
    }

    private static File overworldRegionDirectory(File worldDirectory) {
        List<File> candidates = [
                new File(worldDirectory, 'dimensions/minecraft/overworld/region'),
                new File(worldDirectory, 'region')
        ]
        return candidates.find { !regionFiles(it).empty } ?: candidates.first()
    }

    private static List<File> regionFiles(File directory) {
        if (!directory.isDirectory()) {
            return []
        }
        return directory.listFiles()
                .findAll { it.isFile() && it.name.endsWith('.mca') && it.length() >= 8192L }
                .sort { it.name }
    }

    private static void requireFile(File file, String description) {
        if (!file.isFile() || file.length() < 16L) {
            throw new IllegalStateException("Missing or empty ${description}: ${file}")
        }
    }
}
