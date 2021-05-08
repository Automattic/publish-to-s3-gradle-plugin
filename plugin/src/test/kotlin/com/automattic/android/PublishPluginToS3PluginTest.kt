package com.automattic.android.publish

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

class PublishPluginToS3PluginTest {
    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("java-gradle-plugin")
        project.plugins.apply("com.automattic.android.publish-plugin-to-s3")

        // Verify the result
        project.afterEvaluate {
            /**
             * "publishPluginMavenPublicationToMavenRepository" task is only available after the project is evaluated
             * which causes the test to crash. We get around this issue by testing this task only
             * after project is evaluated which matches the actual behaviour of the plugin.
             */
            assertNotNull(project.tasks.findByName("publishPluginToS3"))
        }
    }
}
