package com.automattic.android.publish

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.zip.ZipFile

private const val COORDINATE_PARTS = 3

abstract class ResolveAiDocsTask : DefaultTask() {
    // Tracked for up-to-date checks so a re-published artifact with the same coordinate but
    // different content (the SNAPSHOT workflow) triggers a re-extraction.
    @get:InputFiles
    abstract val aiDocsArtifactFiles: ConfigurableFileCollection

    // coordinate "group:artifact:version" -> resolved ai-docs zip. Plain String/File keep this
    // configuration-cache compatible (`ResolvedArtifactResult` itself is not serializable).
    @get:Internal
    abstract val resolvedArtifacts: MapProperty<String, File>

    // Used only to warn about requested coordinates that resolved no `ai-docs` artifact.
    @get:Input
    abstract val requestedCoordinates: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun resolve() {
        val outputDir = outputDirectory.get().asFile
        outputDir.mkdirs()

        val resolved = resolvedArtifacts.get()

        warnForUnresolvedCoordinates(resolved.keys)

        resolved.forEach { (coordinate, zip) ->
            val parts = coordinate.split(":")
            if (parts.size != COORDINATE_PARTS) return@forEach
            val (group, artifact, version) = parts

            val artifactDir = File(outputDir, "$group/$artifact")
            val versionDir = File(artifactDir, version)

            // A single `aiDocs` configuration conflict-resolves each module to one version, so only
            // the version being unpacked should remain; remove any other version directory.
            cleanStaleVersions(artifactDir, keep = version)

            // Extract into a freshly cleaned directory so an interrupted previous run can't leave a
            // partial/mixed docs tree.
            if (versionDir.exists()) versionDir.deleteRecursively()
            versionDir.mkdirs()
            unpackZip(zip, versionDir)
            logger.lifecycle("AI docs saved to: ${versionDir.absolutePath}")
        }
    }

    private fun warnForUnresolvedCoordinates(resolvedKeys: Set<String>) {
        // Compare by module (group:artifact): the resolved version can legitimately differ from the
        // requested one (dynamic versions, conflict resolution), which is not a "not found".
        val resolvedModules = resolvedKeys.map { it.substringBeforeLast(":") }.toSet()
        requestedCoordinates.get()
            .filterNot { it.substringBeforeLast(":") in resolvedModules }
            .forEach { logger.warn("AI docs artifact not found for $it") }
    }

    private fun unpackZip(zipFile: File, targetDir: File) {
        val canonicalTarget = targetDir.canonicalFile.toPath()
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val targetFile = File(targetDir, entry.name)
                // Guard against "zip slip": entries must not escape the target directory.
                require(targetFile.canonicalFile.toPath().startsWith(canonicalTarget)) {
                    "Zip entry '${entry.name}' would extract outside '$canonicalTarget'"
                }
                if (entry.isDirectory) {
                    targetFile.mkdirs()
                } else {
                    unpackEntry(zip, entry, targetFile)
                }
            }
        }
    }

    private fun unpackEntry(zip: ZipFile, entry: java.util.zip.ZipEntry, targetFile: File) {
        targetFile.parentFile.mkdirs()
        zip.getInputStream(entry).use { input ->
            targetFile.outputStream().use { output -> input.copyTo(output) }
        }
    }

    private fun cleanStaleVersions(artifactDir: File, keep: String) {
        if (!artifactDir.exists()) return

        artifactDir.listFiles()
            ?.filter { it.isDirectory && it.name != keep }
            ?.forEach { staleDir ->
                logger.lifecycle("Removing stale AI docs: ${staleDir.absolutePath}")
                staleDir.deleteRecursively()
            }
    }
}
