package lekavar.lma.build

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.util.regex.Pattern

abstract class AuditMinecraftLogsTask extends DefaultTask {
    @InputFiles
    abstract ConfigurableFileCollection getLogFiles()

    @InputFile
    abstract RegularFileProperty getAllowlistFile()

    @OutputFile
    abstract RegularFileProperty getReportFile()

    @TaskAction
    void audit() {
        def allowlist = new JsonSlurper().parse(allowlistFile.get().asFile)
        List<Map<String, String>> rules = (allowlist.events as List).collect { raw ->
            [
                    pathPattern : raw.pathPattern as String,
                    eventPattern: raw.eventPattern as String,
                    reason      : raw.reason as String
            ]
        }
        List<Map<String, String>> accepted = []
        List<Map<String, String>> rejected = []

        logFiles.files.sort { it.absolutePath }.each { File logFile ->
            if (!logFile.isFile()) {
                rejected << [file: logFile.absolutePath, event: 'Required log file is missing']
                return
            }
            AuditMinecraftLogsTask.auditFile(logFile, rules, accepted, rejected)
        }

        File report = reportFile.get().asFile
        report.parentFile.mkdirs()
        report.text = JsonOutput.prettyPrint(JsonOutput.toJson([
                status  : rejected.empty ? 'success' : 'failure',
                accepted: accepted,
                rejected: rejected
        ]))

        if (!rejected.empty) {
            throw new IllegalStateException('Unapproved Minecraft log events:\n' +
                    rejected.collect { "${it.file}: ${it.event}" }.join('\n'))
        }
    }

    private static void auditFile(File logFile, List<Map<String, String>> rules,
                                  List<Map<String, String>> accepted,
                                  List<Map<String, String>> rejected) {
        Pattern header = Pattern.compile('^.*\\[[^]\\r\\n]+/(TRACE|DEBUG|INFO|WARN|ERROR|FATAL)](?:.*)$')
        String currentSeverity = null
        List<String> currentLines = []

        logFile.eachLine('UTF-8') { String line ->
            def matcher = header.matcher(line)
            if (matcher.matches()) {
                AuditMinecraftLogsTask.auditEvent(logFile, currentSeverity, currentLines, rules, accepted, rejected)
                currentSeverity = matcher.group(1)
                currentLines = [line]
            } else if (currentSeverity != null) {
                currentLines << line
            }
        }
        AuditMinecraftLogsTask.auditEvent(logFile, currentSeverity, currentLines, rules, accepted, rejected)
    }

    private static void auditEvent(File logFile, String severity, List<String> lines,
                                   List<Map<String, String>> rules,
                                   List<Map<String, String>> accepted,
                                   List<Map<String, String>> rejected) {
        if (severity == null || lines.empty) {
            return
        }
        String event = lines.join('\n')
        boolean relevantWarning = severity == 'WARN' &&
                event =~ /(?is)(exception|missing|failed|error|unable|could not|mixin target|texture .*limits mip|dropping miplevel|shader|model|resource reload)/
        if (!(severity in ['ERROR', 'FATAL']) && !relevantWarning) {
            return
        }

        String normalizedPath = logFile.absolutePath.replace('\\', '/')
        Map<String, String> matchingRule = rules.find { rule ->
            Pattern.compile(rule.pathPattern, Pattern.CASE_INSENSITIVE).matcher(normalizedPath).find() &&
                    Pattern.compile(rule.eventPattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(event).find()
        }
        String headline = lines.first().trim()
        if (matchingRule == null) {
            rejected << [file: normalizedPath, event: headline]
        } else {
            accepted << [file: normalizedPath, event: headline, reason: matchingRule.reason]
        }
    }
}
