package com.automattic.android.publish

import com.automattic.android.publish.PublishToS3PluginBaseExtension
import com.automattic.android.publish.CalculateVersionNameTask
import com.automattic.android.publish.CheckS3VersionTask
import org.gradle.api.Project
import org.gradle.api.Plugin

class PublishToS3BasePlugin  : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("s3PublishBasePlugin", PublishToS3PluginBaseExtension::class.java)

        project.tasks.register("calculateVersionName", CalculateVersionNameTask::class.java)
        project.tasks.register("isVersionPublishedToS3", CheckS3VersionTask::class.java) {
            it.publishedGroupId = extension.groupId
            it.moduleName = extension.artifactId
        }
    }
}

