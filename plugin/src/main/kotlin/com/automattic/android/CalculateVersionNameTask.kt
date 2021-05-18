package com.automattic.android.publish

import com.automattic.android.publish.BuildEnvironment
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

abstract class CalculateVersionNameTask : DefaultTask() {
    @Internal
    override fun getDescription(): String = "Calculates the version name from the given arguments"

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
        println("${versionName}")
    }
}

