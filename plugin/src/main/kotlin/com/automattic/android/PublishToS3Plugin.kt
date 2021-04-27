package com.automattic.android.publish

import com.automattic.android.publish.CalculateVersionNameTask
import com.automattic.android.publish.CheckS3VersionTask
import com.automattic.android.publish.PublishToS3PluginExtension
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.credentials.AwsCredentials
import java.net.URI

class PublishToS3Plugin: Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("configureS3PublishPlugin", PublishToS3PluginExtension::class.java)

        project.plugins.apply("maven-publish")

        project.tasks.register("calculateVersionName", CalculateVersionNameTask::class.java)
        project.tasks.register("isVersionPublishedToS3", CheckS3VersionTask::class.java) {
            it.publishedGroupId = extension.s3GroupId.get()
            it.moduleName = extension.s3ModuleName.get()
            it.versionName = extension.s3VersionName.get()
        }
        project.tasks.register("publishToS3", PublishToS3Task::class.java) {
            it.publishedGroupId = extension.s3GroupId.get()
            it.moduleName = extension.s3ModuleName.get()
        }

        project.afterEvaluate { p ->
            val publishing = project.getExtensions().getByType(PublishingExtension::class.java)
            publishing.repositories.maven {
                it.name = "s3"
                it.url = URI("s3://a8c-libs.s3.amazonaws.com/android/temp-plugin")
                it.credentials(AwsCredentials::class.java) {
                    it.setAccessKey(System.getenv("AWS_ACCESS_KEY"))
                    it.setSecretKey(System.getenv("AWS_SECRET_KEY"))
                }
            }

            val publication = publishing.publications.create("S3", MavenPublication::class.java)
            publication.from(p.getComponents().getByName("release"))

            println("after evaluate")
        }
        println("before evaluate")
    }
}
