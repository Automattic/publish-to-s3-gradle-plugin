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
                androidLibrary.sourceSets.findByName("main")?.let { mainSourceSet ->
                    project.tasks.register("androidSourcesJar", Jar::class.java) { task ->
                        task.description = "Creates a jar from android library main source set " +
                            "to be used to publish the '-sources.jar' file with the library artifacts"
                        task.from(mainSourceSet.java.srcDirs)
                        task.archiveClassifier.set("sources")
                    }
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
