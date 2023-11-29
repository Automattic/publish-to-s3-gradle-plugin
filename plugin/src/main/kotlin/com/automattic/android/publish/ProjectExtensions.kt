package com.automattic.android.publish

import org.gradle.api.Project
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.w3c.dom.Element
import java.io.File
import java.net.URI

private const val EXTRA_VERSION_NAME = "extra_version_name"

fun Project.setExtraVersionName(versionName: String) {
    project.extraProperties.set(EXTRA_VERSION_NAME, versionName)
}

fun Project.getExtraVersionName() = project.extraProperties.get(EXTRA_VERSION_NAME)

fun Project.setVersionForAllMavenPublications(versionName: String) {
    project.allMavenPublications().forEach {
        it.version = versionName
        if (it.name.endsWith("PluginMarkerMaven")) {
            it.updatePluginMarkerPom(versionName)
        }
    }
}

private fun MavenPublication.updatePluginMarkerPom(
    versionName: String
) {
    pom.withXml { xmlProvider ->
        val root: Element = xmlProvider.asElement()
        val versionTags = root.getElementsByTagName("version")

        for (i in 0..versionTags.length - 1) {
            versionTags.item(i).textContent = versionName
        }
    }
}

fun Project.allMavenPublications() =
    project.extensions.getByType(PublishingExtension::class.java)
        .publications.withType(MavenPublication::class.java)

fun Project.addS3MavenRepository() {
    project.extensions.configure(PublishingExtension::class.java) { publishing ->
        publishing.repositories.maven { mavenRepo ->
            mavenRepo.name = "s3"
            mavenRepo.url = URI("s3://a8c-libs.s3.amazonaws.com/android")
            mavenRepo.credentials(AwsCredentials::class.java) {
                it.accessKey = System.getenv("AWS_ACCESS_KEY")
                it.secretKey = System.getenv("AWS_SECRET_KEY")
            }
        }
    }
}

fun Project.printPublishedVersionNameAfterPublishTasks() {
    project.tasks.withType(PublishToMavenRepository::class.java) { task ->
        task.doLast {
            File(project.buildDir, "published-version.txt").writeText("${project.getExtraVersionName()}")

            println("Version '${project.getExtraVersionName()}' has been published successfully.")
        }
    }
}
