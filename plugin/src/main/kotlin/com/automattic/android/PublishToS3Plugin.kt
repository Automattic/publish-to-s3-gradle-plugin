package com.automattic.android.publish

import com.automattic.android.publish.CalculateVersionNameTask
import com.automattic.android.publish.CheckS3VersionTask
import org.gradle.api.Project
import org.gradle.api.Plugin

class PublishToS3Plugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("calculateVersionName", CalculateVersionNameTask::class.java)
        project.tasks.register("isVersionPublishedToS3", CheckS3VersionTask::class.java)
    }
}
