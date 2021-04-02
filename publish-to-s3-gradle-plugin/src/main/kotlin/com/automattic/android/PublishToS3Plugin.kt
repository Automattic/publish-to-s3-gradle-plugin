package com.automattic.android.publish

import org.gradle.api.Project
import org.gradle.api.Plugin

class PublishToS3Plugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("greeting") { task ->
            task.doLast {
                println("Hello from plugin 'com.automattic.android.publish-to-s3'")
            }
        }
    }
}
