package com.automattic.android.publish

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

abstract class ZipAiDocsTask : DefaultTask() {
    @get:InputDirectory
    abstract val sourceDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputZip: RegularFileProperty

    @TaskAction
    fun zip() {
        val sourceDir = sourceDirectory.get().asFile
        val outputFile = outputZip.get().asFile

        outputFile.parentFile.mkdirs()

        ZipOutputStream(outputFile.outputStream()).use { zos ->
            sourceDir.walkTopDown()
                .filter { it.isFile }
                // Sort by path for a deterministic ZIP (reproducible output / stable build cache).
                .sortedBy { it.relativeTo(sourceDir).invariantSeparatorsPath }
                .forEach { file ->
                    // Use forward slashes so the ZIP layout is correct on all platforms.
                    val entryPath = file.relativeTo(sourceDir).invariantSeparatorsPath
                    zos.putNextEntry(ZipEntry(entryPath))
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
        }

        val sizeKb = outputFile.length() / KB_DIVISOR
        logger.lifecycle("AI docs zipped: ${outputFile.absolutePath} (${sizeKb}KB)")
    }

    companion object {
        private const val KB_DIVISOR = 1024
    }
}
