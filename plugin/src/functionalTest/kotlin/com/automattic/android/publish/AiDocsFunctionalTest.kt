package com.automattic.android.publish

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun `given a published ai-docs artifact, when resolveAiDocs runs, then it unpacks and cleans stale versions`() {
        val projectDir = File("build/functionalTest-aiDocs-resolve")
        projectDir.deleteRecursively()
        projectDir.mkdirs()

        val repoDir = File(projectDir, "maven-repo")
        publishFakeAiDocs(repoDir, version = "1.0.0", entry = "index.md", content = "# v1\n")
        publishFakeAiDocs(repoDir, version = "2.0.0", entry = "index.md", content = "# v2\n")

        projectDir.resolve("settings.gradle").writeText("")

        fun resolve(version: String): TaskOutcome? {
            projectDir.resolve("build.gradle.kts").writeText("""
                plugins {
                    id("com.automattic.android.ai-docs")
                }

                repositories {
                    maven { url = uri("${repoDir.toURI()}") }
                }

                aiDocs {
                    resolve("com.example:docs:$version")
                }
            """.trimIndent())

            return GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("resolveAiDocs")
                .withProjectDir(projectDir)
                .build()
                .task(":resolveAiDocs")?.outcome
        }

        assertEquals(TaskOutcome.SUCCESS, resolve("1.0.0"))
        val v1 = File(projectDir, "build/ai-docs/com.example/docs/1.0.0/index.md")
        assertTrue(v1.exists(), "v1 docs should be unpacked at ${v1.absolutePath}")

        assertEquals(TaskOutcome.SUCCESS, resolve("2.0.0"))
        val v2 = File(projectDir, "build/ai-docs/com.example/docs/2.0.0/index.md")
        assertTrue(v2.exists(), "v2 docs should be unpacked")
        assertFalse(
            File(projectDir, "build/ai-docs/com.example/docs/1.0.0").exists(),
            "stale v1 docs should be removed"
        )
    }

    /** Lays out a POM + docs zip at the Maven coordinate `com.example:docs:<version>:ai-docs@zip`. */
    private fun publishFakeAiDocs(repoDir: File, version: String, entry: String, content: String) {
        val artifactDir = File(repoDir, "com/example/docs/$version").apply { mkdirs() }
        File(artifactDir, "docs-$version.pom").writeText(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>docs</artifactId>
              <version>$version</version>
              <packaging>jar</packaging>
            </project>
            """.trimIndent()
        )
        ZipOutputStream(File(artifactDir, "docs-$version-ai-docs.zip").outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry(entry))
            zos.write(content.toByteArray())
            zos.closeEntry()
        }
    }
}
