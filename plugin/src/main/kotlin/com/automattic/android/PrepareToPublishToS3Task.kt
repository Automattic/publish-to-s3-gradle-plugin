package com.automattic.android.publish

import com.automattic.android.publish.CheckS3Version

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

abstract class PrepareToPublishToS3Task : DefaultTask() {
    @Internal
    override fun getDescription(): String = "Calculates a version name and updates maven publication version" +
        " - It should be finalized by the proper publish task"

    @get:Input
    abstract var publishedGroupId: String

    @get:Input
    abstract var moduleName: String

    @Input
    @Option(option = ARG_TAG_NAME, description = "The name of the git tag, if the current build is tagged")
    var tagName: String = ""

    @Input
    @Option(option = ARG_BRANCH_NAME, description = "The name of the Git branch currently being built")
    var branchName: String = ""

    @Input
    @Option(option = ARG_SHA1, description = "The SHA1 hash of the last commit of the current build")
    var sha1: String = ""

    @Input
    @Option(option = ARG_PR_URL, description = "The URL of the associated pull request")
    var pullRequestUrl: String = ""

    @TaskAction
    fun process() {
        val versionName = BuildEnvironmentArgs(tagName, branchName, sha1, pullRequestUrl)
            .process().versionName
        project.setExtraVersionName(versionName)
        val isPublished = CheckS3Version(publishedGroupId, moduleName, versionName).check()

        if (isPublished) {
            throw IllegalStateException("'$versionName' is already published to S3!")
        }

        project.setVersionForAllMavenPublications(versionName)
    }
}
