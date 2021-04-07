package com.automattic.android.publish

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PublishToS3PluginFunctionalTest {
    @Test
    fun `verify version exists`() {
        // Setup the test build
        val projectDir = File("build/functionalTest")
        projectDir.mkdirs()
        projectDir.resolve("settings.gradle").writeText("")
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("com.automattic.android.publish-to-s3")
            }
        """)

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        // TODO: Once a proper version is published to S3, we should update the `versionName`
        runner.withArguments("-q", "isVersionPublishedToS3", "--packagePath=org.wordpress.utils",
            "--moduleName=utils", "--versionName=68-4f34ac114ea99eaac229428e2c416735e430d3d8")
        runner.withProjectDir(projectDir)
        val result = runner.build()
        val resultAsBoolean = result.output.trim().toBoolean()

        // Verify the result
        assertTrue(resultAsBoolean)
    }

    @Test
    fun `verify version does not exists`() {
        // Setup the test build
        val projectDir = File("build/functionalTest")
        projectDir.mkdirs()
        projectDir.resolve("settings.gradle").writeText("")
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("com.automattic.android.publish-to-s3")
            }
        """)

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("-q", "isVersionPublishedToS3", "--packagePath=org.wordpress.utils",
            "--moduleName=utils", "--versionName=thisversiondoesntexist")
        runner.withProjectDir(projectDir)
        val result = runner.build()
        val resultAsBoolean = result.output.trim().toBoolean()

        // Verify the result
        assertFalse(resultAsBoolean)
    }
}
