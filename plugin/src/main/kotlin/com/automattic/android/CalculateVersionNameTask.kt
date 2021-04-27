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
        println("${versionName}")
    }
}

