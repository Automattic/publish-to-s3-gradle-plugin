package com.automattic.android.publish

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

private const val PUBLISH_LIBRARY_TASK_NAME = "publishLibraryToS3"

class PublishLibraryToS3Plugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("maven-publish")

        val extension = project.extensions.create("s3PublishLibrary", PublishLibraryToS3Extension::class.java)
        project.setupS3Repository()

        project.tasks.register(PUBLISH_LIBRARY_TASK_NAME, PublishToS3Task::class.java) {
            it.publishedGroupId = extension.groupId
            it.moduleName = extension.artifactId
            it.finalizedBy(project.tasks.named("publishS3PublicationToS3Repository"))
        }

        project.afterEvaluate { p ->
            p.getExtensions().configure(PublishingExtension::class.java) { publishing ->
                publishing.publications.create("s3", MavenPublication::class.java) { mavenPublication ->
                    mavenPublication.setGroupId(extension.groupId)
                    mavenPublication.setArtifactId(extension.artifactId)
                    extension.from?.let { componentName ->
                        if (componentName.isNotEmpty()) {
                            mavenPublication.from(p.getComponents().getByName(componentName))
                        }
                    }
                }
            }

            p.addFilterAndFinalizerToPublishToMavenRepositoryTasks(filterTask = PUBLISH_LIBRARY_TASK_NAME)
        }
    }
}

