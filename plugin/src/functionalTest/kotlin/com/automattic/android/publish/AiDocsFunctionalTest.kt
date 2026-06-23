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
        projectDir.deleteRecursively()
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
    fun `given aiDocs consumer plugin with a resolve notation, when listing tasks, then resolveAiDocs is registered`() {
        val projectDir = File("build/functionalTest-aiDocs-consumer")
        projectDir.deleteRecursively()
        projectDir.mkdirs()

        projectDir.resolve("settings.gradle").writeText("")
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("com.automattic.android.ai-docs")
            }

            aiDocs {
                resolve("com.example:docs:1.0.0")
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("tasks", "--all")
            .withProjectDir(projectDir)
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":tasks")?.outcome)
        assertTrue(result.output.contains("resolveAiDocs"), "resolveAiDocs task should be registered")
    }

    @Test
    fun `given both publish-to-s3 and ai-docs plugins, when configuring, then the aiDocs extension is shared`() {
        val projectDir = File("build/functionalTest-aiDocs-both")
        projectDir.deleteRecursively()
        projectDir.mkdirs()

        projectDir.resolve("settings.gradle").writeText("")
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("com.automattic.android.publish-to-s3")
                id("com.automattic.android.ai-docs")
            }

            aiDocs {
                resolve("com.example:docs:1.0.0")
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("tasks", "--all")
            .withProjectDir(projectDir)
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":tasks")?.outcome)
        assertTrue(result.output.contains("resolveAiDocs"), "resolveAiDocs task should be registered")
    }
}
