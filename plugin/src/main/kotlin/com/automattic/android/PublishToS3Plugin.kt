package com.automattic.android.publish

import com.automattic.android.publish.CalculateVersionNameTask
import com.automattic.android.publish.PublishToS3BasePlugin
import com.automattic.android.publish.PublishToS3PluginExtension
import com.automattic.android.publish.PublishToS3PluginBaseExtension
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

const val EXTRA_VERSION_NAME = "extra_version_name"

class PublishToS3Plugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("s3PublishPlugin", PublishToS3PluginExtension::class.java)
        project.plugins.apply(PublishToS3BasePlugin::class.java)

        project.tasks.register("publishToS3", PublishToS3Task::class.java) {
            it.publishedGroupId = extension.groupId
            it.moduleName = extension.artifactId
            it.finalizedBy(project.tasks.named("publishS3PublicationToS3Repository"))
        }

        project.afterEvaluate { p ->
            project.extensions.configure(PublishToS3PluginBaseExtension::class.java) {
                it.groupId = extension.groupId
                it.artifactId = extension.artifactId
            }

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

            p.tasks.withType(PublishToMavenRepository::class.java) { task ->
                task.onlyIf {
                    val state = p.tasks.getByName("publishToS3").state
                    if (!state.executed) {
                        throw IllegalStateException("Publish tasks shouldn't be directly called." +
                            " Please use 'publishToS3' task instead.")
                    }
                    state.failure == null
                }

                task.doLast {
                    println("'${project.extraProperties.get(EXTRA_VERSION_NAME)}' is succesfully published.")
                }
            }
        }
    }
}
