package com.automattic.android.publish

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ListAllTasksFunctionalTest {
    @Test
    fun `lists all tasks`() {
        functionalTestRunner("-q", "tasks", "--all").build()
    }
}

fun functionalTestRunner(vararg arguments: String): GradleRunner {
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
    runner.withArguments(arguments.toList())
    runner.withProjectDir(projectDir)
    return runner
}
