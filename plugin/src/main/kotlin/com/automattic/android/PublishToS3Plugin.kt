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
            it.publishedGroupId = extension.groupId.get()
            it.moduleName = extension.artifactId.get()
            it.versionName = extension.versionName.get()
        }
        project.tasks.register("publishToS3", PublishToS3Task::class.java) {
            it.publishedGroupId = extension.groupId.get()
            it.moduleName = extension.artifactId.get()
        }

        project.afterEvaluate { p ->
            val publishing = p.getExtensions().getByType(PublishingExtension::class.java)

            publishing.publications.create("s3", MavenPublication::class.java) { mavenPublication ->
                mavenPublication.setGroupId(extension.groupId.get())
                mavenPublication.setArtifactId(extension.artifactId.get())
                mavenPublication.setVersion(extension.versionName.get())
                mavenPublication.from(p.getComponents().getByName(extension.from.get()))
            }

            publishing.repositories.maven { mavenRepo ->
                mavenRepo.name = "s3"
                mavenRepo.url = URI("s3://a8c-libs.s3.amazonaws.com/android/temp-plugin")
                mavenRepo.credentials(AwsCredentials::class.java) {
                    it.setAccessKey(System.getenv("AWS_ACCESS_KEY"))
                    it.setSecretKey(System.getenv("AWS_SECRET_KEY"))
                }
            }
        }
    }
}
