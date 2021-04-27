package com.automattic.android.publish

import com.automattic.android.publish.CalculateVersionNameTask
import com.automattic.android.publish.CheckS3VersionTask
import com.automattic.android.publish.PublishToS3PluginExtension
import org.gradle.api.Project
import org.gradle.api.Plugin

class PublishToS3Plugin: Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("configureS3PublishPlugin", PublishToS3PluginExtension::class.java)
        project.tasks.register("calculateVersionName", CalculateVersionNameTask::class.java)
        project.tasks.register("isVersionPublishedToS3", CheckS3VersionTask::class.java) {
            it.publishedGroupId = extension.s3GroupId.get()
            it.moduleName = extension.s3ModuleName.get()
            it.versionName = extension.s3VersionName.get()
        }
    }
}
