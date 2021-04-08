package com.automattic.android.publish

import java.io.File
import org.gradle.testkit.runner.GradleRunner

fun publishToS3PluginFunctionalTestRunnerWithArguments(vararg arguments: String): GradleRunner {
    val projectDir = File("build/functionalTest")
    projectDir.mkdirs()
    projectDir.resolve("settings.gradle").writeText("")
    projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("com.automattic.android.publish-to-s3")
            }
        """)

    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    // TODO: Once a proper version is published to S3, we should update the `versionName`
    runner.withArguments(arguments.toList())
    runner.withProjectDir(projectDir)
    return runner
}
