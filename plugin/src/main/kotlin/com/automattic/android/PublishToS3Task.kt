package com.automattic.android.publish

import com.automattic.android.publish.CheckS3Version

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

abstract class PublishToS3Task : DefaultTask() {
    @Internal
    override fun getDescription(): String = "Calculates a version name and calls the publish task"

    @get:Input
    abstract var publishedGroupId: String

    @get:Input
    abstract var moduleName: String

    @Input
    @Option(option = "tag-name",
        description = "The name of the git tag, if the current build is tagged")
    var tagName: String = ""

    @Input
    @Option(option = BRANCH_NAME_ARGUMENT_NAME,
        description = "The name of the Git branch currently being built")
    var branchName: String = ""

    @Input
    @Option(option = SHA1_ARGUMENT_NAME,
        description = "The SHA1 hash of the last commit of the current build")
    var sha1: String = ""

    @Input
    @Option(option = "pull-request-url", description = "The URL of the associated pull request")
    var pullRequestUrl: String = ""

    @TaskAction
    fun process() {
        val versionName = BuildEnvironment(tagName, branchName, sha1, pullRequestUrl).calculateVersionName()
        project.extraProperties.set(EXTRA_VERSION_NAME, versionName)
        val isPublished = CheckS3Version(publishedGroupId, moduleName, versionName).check()

        if (isPublished) {
            throw IllegalStateException("'$versionName' is already published to S3!")
        }

        updateMavenPublicationVersions(versionName)
    }

    private fun updateMavenPublicationVersions(versionName: String) {
        project.getExtensions().getByType(PublishingExtension::class.java)
            .publications.withType(MavenPublication::class.java).forEach {
                it.version = versionName
            }
    }
}
