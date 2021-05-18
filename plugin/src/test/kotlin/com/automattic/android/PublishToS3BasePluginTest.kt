package com.automattic.android.publish

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class PublishToS3BasePluginTest {
    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.automattic.android.publish-to-s3-base")

        // Verify the result
        assertNotNull(project.tasks.findByName("calculateVersionName"))
        assertNotNull(project.tasks.findByName("isVersionPublishedToS3"))
    }
}
