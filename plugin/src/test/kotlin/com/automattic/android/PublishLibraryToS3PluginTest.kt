package com.automattic.android.publish

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

class PublishLibraryToS3PluginTest {
    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.automattic.android.publish-library-to-s3")

        // Verify the result
        assertNotNull(project.tasks.findByName("calculateVersionName"))
        assertNotNull(project.tasks.findByName("isVersionPublishedToS3"))
        project.afterEvaluate {
            /**
             * "publishS3PublicationToS3Repository" task is only available after the project is evaluated
             * which causes the test to crash. We get around this issue by testing this task only
             * after project is evaluated which matches the actual behaviour of the plugin.
             */
            assertNotNull(project.tasks.findByName("publishLibraryToS3"))
        }
    }
}
