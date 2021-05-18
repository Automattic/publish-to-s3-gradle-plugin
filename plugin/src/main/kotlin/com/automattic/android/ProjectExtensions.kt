package com.automattic.android.publish

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

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

