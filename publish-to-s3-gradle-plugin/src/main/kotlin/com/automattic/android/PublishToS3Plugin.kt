package com.automattic.android.publish

import com.automattic.android.publish.CheckS3VersionTask
import org.gradle.api.Project
import org.gradle.api.Plugin

class PublishToS3Plugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("greeting") { task ->
            task.doLast {
                println("Hello from plugin 'com.automattic.android.publish-to-s3'")
            }
        }

        project.tasks.register("isVersionPublishedToS3", CheckS3VersionTask::class.java) { task ->
            task.doLast {
                println("hello from check version")
            }
        }
    }
}
