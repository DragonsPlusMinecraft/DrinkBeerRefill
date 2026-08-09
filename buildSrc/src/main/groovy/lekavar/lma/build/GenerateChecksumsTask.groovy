package lekavar.lma.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.security.MessageDigest

abstract class GenerateChecksumsTask extends DefaultTask {
    @InputFiles
    abstract ConfigurableFileCollection getArtifacts()

    @OutputFile
    abstract RegularFileProperty getChecksumFile()

    @TaskAction
    void generate() {
        File output = checksumFile.get().asFile
        output.parentFile.mkdirs()
        output.text = artifacts.files.sort { it.name }.collect { File artifact ->
            MessageDigest digest = MessageDigest.getInstance('SHA-256')
            artifact.withInputStream { stream ->
                byte[] buffer = new byte[64 * 1024]
                int count
                while ((count = stream.read(buffer)) >= 0) {
                    if (count > 0) {
                        digest.update(buffer, 0, count)
                    }
                }
            }
            "${digest.digest().encodeHex()}  ${artifact.name}"
        }.join(System.lineSeparator()) + System.lineSeparator()
    }
}
