package com.automattic.android.publish

import com.automattic.android.publish.CalculateVersionNameTask
import com.automattic.android.publish.CheckS3VersionTask
import com.automattic.android.publish.PublishToS3PluginExtension
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import java.net.URI

// TODO: Use logger instead of println across the project

class PublishToS3Plugin: Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("configureS3PublishPlugin", PublishToS3PluginExtension::class.java)
        val extraProperties = project.getExtensions().getByType(ExtraPropertiesExtension::class.java)
        extraProperties.set("s3PublishVersion", "testingExtraProperties")

        project.plugins.apply("maven-publish")

        project.tasks.register("calculateVersionName", CalculateVersionNameTask::class.java)
        project.tasks.register("isVersionPublishedToS3", CheckS3VersionTask::class.java) {
            it.publishedGroupId = extension.groupId.get()
            it.moduleName = extension.artifactId.get()
        }
        project.tasks.register("publishToS3", PublishToS3Task::class.java) {
            it.publishedGroupId = extension.groupId.get()
            it.moduleName = extension.artifactId.get()
            it.finalizedBy(project.tasks.named("publishS3PublicationToS3Repository"))
        }

        project.afterEvaluate { p ->
            val publishing = p.getExtensions().getByType(PublishingExtension::class.java)

            publishing.publications.create("s3", MavenPublication::class.java) { mavenPublication ->
                mavenPublication.setGroupId(extension.groupId.get())
                mavenPublication.setArtifactId(extension.artifactId.get())
                try {
                    mavenPublication.from(p.getComponents().getByName(extension.from.get()))
                } catch (e: org.gradle.api.internal.provider.MissingValueException) {
                    println("WARNING: 'from' property is not set which means no components will be published for this artifact: $e")
                }
            }

            publishing.repositories.maven { mavenRepo ->
                mavenRepo.name = "s3"
                mavenRepo.url = URI("s3://a8c-libs.s3.amazonaws.com/android/temp-plugin")
                mavenRepo.credentials(AwsCredentials::class.java) {
                    it.setAccessKey(System.getenv("AWS_ACCESS_KEY"))
                    it.setSecretKey(System.getenv("AWS_SECRET_KEY"))
                }
            }

            p.tasks.withType(PublishToMavenRepository::class.java) { task ->
                task.onlyIf {
                    val state = p.tasks.getByName("publishToS3").state
                    val shouldProceed = state.executed && state.failure == null
                    if (!shouldProceed) {
                        throw IllegalStateException("Publish tasks shouldn't be directly called. Please call 'publishToS3' task instead.")
                    }
                    shouldProceed
                }
            }
        }
    }
}
