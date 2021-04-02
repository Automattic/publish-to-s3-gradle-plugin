package com.automattic.android.publish

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import kotlin.test.Test
import kotlin.test.assertTrue

class PublishToS3PluginFunctionalTest {
    @Test fun `can run task`() {
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
        runner.withArguments("greeting")
        runner.withProjectDir(projectDir)
        val result = runner.build();

        // Verify the result
        assertTrue(result.output.contains("Hello from plugin 'com.automattic.android.publish-to-s3'"))
    }
}
