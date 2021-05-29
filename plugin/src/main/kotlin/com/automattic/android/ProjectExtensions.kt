package com.automattic.android.publish

import java.net.URI
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.credentials.AwsCredentials

private const val EXTRA_VERSION_NAME = "extra_version_name"

fun Project.setExtraVersionName(versionName: String) {
    project.extraProperties.set(EXTRA_VERSION_NAME, versionName)
}

fun Project.getExtraVersionName() {
    project.extraProperties.get(EXTRA_VERSION_NAME)
}

fun Project.setVersionForAllMavenPublications(versionName: String) {
    project.getExtensions().getByType(PublishingExtension::class.java)
        .publications.withType(MavenPublication::class.java).forEach {
            it.version = versionName
        }
}

fun Project.setArtifactIdForAllMavenPublications(artifactId: String?) {
    if (artifactId.isNullOrEmpty()) return
    project.getExtensions().getByType(PublishingExtension::class.java)
        .publications.withType(MavenPublication::class.java).forEach {
            it.artifactId = artifactId
        }
}

fun Project.setupS3Repository() {
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

fun Project.addFilterAndFinalizerToPublishToMavenRepositoryTasks(filterTask: String) {
    project.tasks.withType(PublishToMavenRepository::class.java) { task ->
        task.onlyIf {
            val state = project.tasks.getByName(filterTask).state
            if (!state.executed) {
                throw IllegalStateException("Publish tasks shouldn't be directly called." +
                    " Please use '$filterTask' task instead.")
            }
            state.failure == null
        }

        task.doLast {
            println("'${project.getExtraVersionName()}' is succesfully published.")
        }
    }
}

