package com.automattic.android.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Zip

private const val AI_DOCS_CLASSIFIER = "ai-docs"
private const val NOTATION_WITHOUT_VERSION_PARTS = 2
private const val NOTATION_WITH_VERSION_PARTS = 3

/**
 * Consumer-only plugin for resolving AI docs from dependency artifacts.
 * Does NOT apply maven-publish.
 *
 * Resolution uses the consuming project's existing repositories (e.g. the repo declared in
 * `settings.gradle` / `dependencyResolutionManagement`). The plugin intentionally does not
 * register its own repository, since that would fail in builds using
 * `RepositoriesMode.FAIL_ON_PROJECT_REPOS`.
 */
class AiDocsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.getOrCreateAiDocsExtension()
        project.configureAiDocsResolving(extension)
    }
}

// Both this consumer plugin and `com.automattic.android.publish-to-s3` register an `aiDocs`
// extension; reuse the existing one so applying both plugins doesn't fail with a name clash.
internal fun Project.getOrCreateAiDocsExtension(): AiDocsExtension =
    extensions.findByType(AiDocsExtension::class.java)
        ?: extensions.create("aiDocs", AiDocsExtension::class.java)

internal fun Project.configureAiDocsPublishing(extension: AiDocsExtension) {
    afterEvaluate {
        if (!extension.sourceDirectory.isPresent) return@afterEvaluate

        // Gradle's Zip task gives a reproducible, cross-platform archive for free (forward-slash
        // entries, stable order, fixed timestamps) and wires `builtBy` into the published artifact.
        val zipTask = tasks.register("zipAiDocs", Zip::class.java) { task ->
            task.from(extension.sourceDirectory)
            task.destinationDirectory.set(layout.buildDirectory.dir("ai-docs"))
            task.archiveFileName.set("ai-docs.zip")
            task.isReproducibleFileOrder = true
            task.isPreserveFileTimestamps = false
        }

        extensions.findByType(PublishingExtension::class.java)?.let { publishing ->
            publishing.publications.withType(MavenPublication::class.java).configureEach { publication ->
                if (!publication.name.endsWith("PluginMarkerMaven")) {
                    publication.artifact(zipTask.flatMap { it.archiveFile }) {
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

        val aiDocsConfig = configurations.maybeCreate("aiDocs").apply {
            isTransitive = false
            isCanBeConsumed = false
        }

        val resolvedNotations = deps.map { notation ->
            val parts = notation.split(":")
            require(parts.size == NOTATION_WITHOUT_VERSION_PARTS || parts.size == NOTATION_WITH_VERSION_PARTS) {
                "Invalid AI docs dependency notation '$notation'; " +
                    "expected 'group:artifact' or 'group:artifact:version'"
            }
            val group = parts[0]
            val artifact = parts[1]
            val version = if (parts.size == NOTATION_WITH_VERSION_PARTS) {
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
            // Use this project's build dir (not rootProject) so applying the plugin to multiple
            // subprojects doesn't race on a shared output directory.
            task.outputDirectory.set(layout.buildDirectory.dir("ai-docs"))
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
