package com.automattic.android.publish

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.jvm.tasks.Jar
import com.android.build.gradle.LibraryExtension

class PublishToS3Plugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("maven-publish")
        project.addS3MavenRepository()

        project.tasks.register("calculateVersionName", CalculateVersionNameTask::class.java)
        project.tasks.register("isVersionPublishedToS3", CheckS3VersionTask::class.java)

        project.pluginManager.withPlugin("com.android.library") {
            project.extensions.findByType(LibraryExtension::class.java)?.let { androidLibrary ->
                androidLibrary.publishing.singleVariant("release") {
                    withSourcesJar()
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
}
