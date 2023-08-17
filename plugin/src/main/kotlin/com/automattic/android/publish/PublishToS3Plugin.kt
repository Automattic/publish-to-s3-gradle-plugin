package com.automattic.android.publish

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

class PublishToS3Plugin : Plugin<Project> {
    override fun apply(project: Project) {
        applyInternal(project, withSources = true)
    }
}

class PublishToS3WithoutSourcesPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        applyInternal(project, withSources = false)
    }
}

private fun applyInternal(project: Project, withSources: Boolean) {
    project.plugins.apply("maven-publish")
    project.addS3MavenRepository()

    project.tasks.register("calculateVersionName", CalculateVersionNameTask::class.java)
    project.tasks.register("isVersionPublishedToS3", CheckS3VersionTask::class.java)

    project.pluginManager.withPlugin("com.android.library") {
        project.extensions.findByType(LibraryExtension::class.java)?.let { androidLibrary ->
            androidLibrary.publishing.singleVariant("release") {
                if (withSources) withSourcesJar()
                withJavadocJar()
            }
        }
    }

    val preTask = project.tasks.register("prepareToPublishToS3", PrepareToPublishToS3Task::class.java)
    project.tasks.withType(PublishToMavenRepository::class.java) { task ->
        task.dependsOn(preTask)
    }
    project.tasks.withType(GenerateMavenPom::class.java) { task ->
        task.dependsOn(preTask)
    }

    project.printPublishedVersionNameAfterPublishTasks()
}
