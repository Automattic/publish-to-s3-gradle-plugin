package com.automattic.android.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

private const val AI_DOCS_CLASSIFIER = "ai-docs"
private const val NOTATION_WITH_VERSION_PARTS = 3

/**
 * Consumer-only plugin for resolving AI docs from S3 dependencies.
 * Does NOT apply maven-publish.
 */
class AiDocsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("aiDocs", AiDocsExtension::class.java)
        project.configureAiDocsResolving(extension)
    }
}

internal fun Project.configureAiDocsPublishing(extension: AiDocsExtension) {
    afterEvaluate {
        if (!extension.sourceDirectory.isPresent) return@afterEvaluate

        val zipTask = tasks.register("zipAiDocs", ZipAiDocsTask::class.java) { task ->
            task.sourceDirectory.set(extension.sourceDirectory)
            task.outputZip.set(layout.buildDirectory.file("ai-docs/ai-docs.zip"))
        }

        extensions.findByType(PublishingExtension::class.java)?.let { publishing ->
            publishing.publications.withType(MavenPublication::class.java).configureEach { publication ->
                if (!publication.name.endsWith("PluginMarkerMaven")) {
                    publication.artifact(zipTask.flatMap { it.outputZip }) {
                        it.classifier = AI_DOCS_CLASSIFIER
                        it.extension = "zip"
                    }
                }
            }
        }
    }
}

internal fun Project.configureAiDocsResolving(extension: AiDocsExtension) {
    afterEvaluate {
        val deps = extension.dependencies.getOrElse(emptyList())
        if (deps.isEmpty()) return@afterEvaluate

        val aiDocsConfig = configurations.create("aiDocs") {
            it.isTransitive = false
            it.isCanBeConsumed = false
        }

        val resolvedNotations = deps.map { notation ->
            val parts = notation.split(":")
            val group = parts[0]
            val artifact = parts[1]
            val version = if (parts.size >= NOTATION_WITH_VERSION_PARTS) {
                parts[2]
            } else {
                resolveVersionFromDependencyGraph(group, artifact)
            }

            project.dependencies.add("aiDocs", "$group:$artifact:$version:$AI_DOCS_CLASSIFIER@zip")

            "$group:$artifact:$version"
        }

        tasks.register("resolveAiDocs", ResolveAiDocsTask::class.java) { task ->
            task.aiDocsConfiguration = aiDocsConfig
            task.requestedCoordinates.set(resolvedNotations)
            // Resolved docs are a build artifact: cleaned by `clean` and implicitly gitignored.
            task.outputDirectory.set(rootProject.layout.buildDirectory.dir("ai-docs"))
        }
    }
}

private fun Project.resolveVersionFromDependencyGraph(group: String, artifact: String): String {
    val matchingDep = configurations
        .flatMap { it.dependencies }
        .find { it.group == group && it.name == artifact }

    return matchingDep?.version
        ?: throw IllegalStateException(
            "Cannot resolve version for '$group:$artifact'. " +
                "Either add it as a dependency or specify the version explicitly: " +
                "resolve(\"$group:$artifact:VERSION\")"
        )
}
