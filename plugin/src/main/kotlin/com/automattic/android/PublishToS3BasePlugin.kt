package com.automattic.android.publish

import com.automattic.android.publish.PublishToS3BaseExtension
import com.automattic.android.publish.CalculateVersionNameTask
import com.automattic.android.publish.CheckS3VersionTask
import java.net.URI
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.credentials.AwsCredentials

class PublishToS3BasePlugin  : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("s3PublishBasePlugin", PublishToS3BaseExtension::class.java)
        project.plugins.apply("maven-publish")

        project.tasks.register("calculateVersionName", CalculateVersionNameTask::class.java)
        project.tasks.register("isVersionPublishedToS3", CheckS3VersionTask::class.java) {
            it.publishedGroupId = extension.groupId
            it.moduleName = extension.artifactId
        }

        project.getExtensions().configure(PublishingExtension::class.java) { publishing ->
            publishing.repositories.maven { mavenRepo ->
                mavenRepo.name = "s3"
                mavenRepo.url = URI("s3://a8c-libs.s3.amazonaws.com/android")
                mavenRepo.credentials(AwsCredentials::class.java) {
                    it.setAccessKey(System.getenv("AWS_ACCESS_KEY"))
                    it.setSecretKey(System.getenv("AWS_SECRET_KEY"))
                }
            }
        }
    }
}

