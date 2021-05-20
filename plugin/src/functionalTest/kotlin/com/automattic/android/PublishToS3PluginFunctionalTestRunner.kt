package com.automattic.android.publish

import java.io.File
import org.gradle.testkit.runner.GradleRunner

fun publishToS3HelpersPluginFunctionalTestRunnerWithArguments(vararg arguments: String): GradleRunner {
    val projectDir = File("build/functionalTest")
    projectDir.mkdirs()
    projectDir.resolve("settings.gradle").writeText("")
    projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("com.automattic.android.publish-to-s3-helpers")
            }
        """)

    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments(arguments.toList())
    runner.withProjectDir(projectDir)
    return runner
}
