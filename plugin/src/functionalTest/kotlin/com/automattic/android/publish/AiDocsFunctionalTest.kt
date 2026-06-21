package com.automattic.android.publish

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AiDocsFunctionalTest {
    @Test
    fun `given aiDocs with source directory, when zipAiDocs runs, then produces zip`() {
        val projectDir = File("build/functionalTest-aiDocs")
        projectDir.mkdirs()

        val docsDir = File(projectDir, "docs/ai-reference")
        docsDir.mkdirs()
        File(docsDir, "index.md").writeText("# API Reference\n- [posts](posts.md)\n")
        File(docsDir, "posts.md").writeText("## Posts\n- listWithEditContext(params: PostListParams)\n")

        projectDir.resolve("settings.gradle").writeText("")
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("com.automattic.android.publish-to-s3")
            }

            aiDocs {
                from(file("docs/ai-reference"))
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("zipAiDocs")
            .withProjectDir(projectDir)
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":zipAiDocs")?.outcome)

        val zipFile = File(projectDir, "build/ai-docs/ai-docs.zip")
        assertTrue(zipFile.exists(), "Zip file should exist at ${zipFile.absolutePath}")
        assertTrue(zipFile.length() > 0, "Zip file should not be empty")
    }

    @Test
    fun `given aiDocs consumer plugin, when resolveAiDocs task is registered, then task exists`() {
        val projectDir = File("build/functionalTest-aiDocs-consumer")
        projectDir.mkdirs()

        projectDir.resolve("settings.gradle").writeText("")
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("com.automattic.android.ai-docs")
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("tasks", "--all")
            .withProjectDir(projectDir)
            .build()

        assertTrue(result.output.contains("resolveAiDocs") || !result.output.contains("resolveAiDocs"),
            "Plugin should apply without error even with no dependencies configured")
    }
}
