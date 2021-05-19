package com.automattic.android.publish

import org.gradle.api.Project
import org.gradle.api.Plugin

private const val PUBLISH_PLUGIN_TASK_NAME = "publishPluginToS3"

class PublishPluginToS3Plugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("maven-publish")

        val extension = project.extensions.create("s3PublishPlugin", PublishPluginToS3Extension::class.java)
        project.setupS3Repository()

        project.tasks.register(PUBLISH_PLUGIN_TASK_NAME, PublishToS3Task::class.java) {
            it.publishedGroupId = extension.groupId
            it.moduleName = extension.artifactId
            it.finalizedBy(project.tasks.named("publishPluginMavenPublicationToMavenRepository"))
        }

        project.addFilterAndFinalizerToPublishToMavenRepositoryTasks(filterTask = PUBLISH_PLUGIN_TASK_NAME)
    }
}

