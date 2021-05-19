package com.automattic.android.publish

import java.io.File
import org.gradle.testkit.runner.GradleRunner

fun helpersPluginFunctionalTestRunner(vararg arguments: String): GradleRunner =
    functionalTestRunner("com.automattic.android.publish-to-s3-helpers", arguments.toList())

private fun functionalTestRunner(pluginId: String, arguments: List<String>): GradleRunner {
    val projectDir = File("build/functionalTest")
    projectDir.mkdirs()
    projectDir.resolve("settings.gradle").writeText("")
    projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("$pluginId")
            }
        """)

    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments(arguments)
    runner.withProjectDir(projectDir)
    return runner
}
