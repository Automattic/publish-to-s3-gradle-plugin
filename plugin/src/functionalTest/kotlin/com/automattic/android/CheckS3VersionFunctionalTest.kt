package com.automattic.android.publish

import com.automattic.android.publish.publishToS3PluginFunctionalTestRunnerWithArguments
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class CheckS3VersionFunctionalTest {
    @Test
    fun `verify version exists`() {
       // TODO: Once a proper version is published to S3, we should update the `versionName`
        val runner = publishToS3PluginFunctionalTestRunnerWithArguments("-q",
            "isVersionPublishedToS3", "--published-group-id=org.wordpress.utils",
            "--module-name=utils", "--version-name=68-4f34ac114ea99eaac229428e2c416735e430d3d8")
        val result = runner.build()
        assertTrue(result.output.trim().toBoolean())
    }

    @Test
    fun `verify version does not exists`() {
        val runner = publishToS3PluginFunctionalTestRunnerWithArguments("-q",
            "isVersionPublishedToS3", "--published-group-id=org.wordpress.utils",
            "--module-name=utils", "--version-name=thisversiondoesntexist")
        val result = runner.build()
        assertFalse(result.output.trim().toBoolean())
    }
}
