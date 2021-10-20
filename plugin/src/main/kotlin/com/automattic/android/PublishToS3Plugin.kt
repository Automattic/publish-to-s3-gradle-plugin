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

        if (project.pluginManager.hasPlugin("com.android.library")) {
            project.extensions.findByType(LibraryExtension::class.java)?.let { androidLibrary ->
                androidLibrary.sourceSets.findByName("main")?.let { mainSourceSet ->
                    project.tasks.register("androidSourcesJar", Jar::class.java) { task ->
                        task.from(mainSourceSet.java.srcDirs)
                        task.archiveClassifier.set("sources")
                    }
                }
            }
        }

        val extension = project.extensions.create("publishToS3Plugin", PublishToS3Extension::class.java)
        val preTask = project.tasks.register("prepareToPublishToS3", PrepareToPublishToS3Task::class.java) { task ->
            task.publishedGroupId = extension.mavenPublishGroupId
            task.moduleName = extension.mavenPublishArtifactId
        }

        project.tasks.withType(PublishToMavenRepository::class.java) { task ->
            task.dependsOn(preTask)
        }
        project.tasks.withType(GenerateMavenPom::class.java) { task ->
            task.dependsOn(preTask)
        }

        project.printPublishedVersionNameAfterPublishTasks()
    }
}
