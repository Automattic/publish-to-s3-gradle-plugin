package com.automattic.android.publish

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.zip.ZipFile

abstract class ResolveAiDocsTask : DefaultTask() {
    @Internal
    override fun getDescription() = "Resolves and unpacks AI documentation from dependencies"

    @get:Internal
    lateinit var aiDocsConfiguration: Configuration

    @get:Input
    abstract val requestedCoordinates: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun resolve() {
        val outputDir = outputDirectory.get().asFile
        outputDir.mkdirs()

        val resolvedArtifacts = aiDocsConfiguration.resolvedConfiguration.resolvedArtifacts

        requestedCoordinates.get().forEach { notation ->
            val parts = notation.split(":")
            val group = parts[0]
            val artifact = parts[1]
            val version = parts[2]

            val matchingArtifact = resolvedArtifacts.find {
                it.moduleVersion.id.group == group && it.moduleVersion.id.name == artifact
            } ?: run {
                logger.warn("AI docs artifact not found for $group:$artifact:$version")
                return@forEach
            }

            val artifactDir = File(outputDir, "$group/$artifact")
            val versionDir = File(artifactDir, version)

            cleanStaleVersions(artifactDir, version)

            if (isUpToDate(versionDir)) {
                logger.lifecycle("AI docs up-to-date: ${versionDir.absolutePath}")
                return@forEach
            }

            versionDir.mkdirs()
            unpackZip(matchingArtifact.file, versionDir)
            logger.lifecycle("AI docs saved to: ${versionDir.absolutePath}")
        }
    }

    private fun isUpToDate(versionDir: File): Boolean =
        versionDir.exists() && versionDir.list()?.isNotEmpty() == true

    private fun unpackZip(zipFile: File, targetDir: File) {
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val targetFile = File(targetDir, entry.name)
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

    private fun cleanStaleVersions(artifactDir: File, currentVersion: String) {
        if (!artifactDir.exists()) return

        artifactDir.listFiles()
            ?.filter { it.isDirectory && it.name != currentVersion }
            ?.forEach { staleDir ->
                logger.lifecycle("Removing stale AI docs: ${staleDir.absolutePath}")
                staleDir.deleteRecursively()
            }
    }
}
