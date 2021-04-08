package com.automattic.android.publish

import com.automattic.android.publish.BuildEnvironment
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

private const val branchNameArgumentName = "branch-name"
private const val sha1ArgumentName = "sha1"

private val requiredArgumentsErrorMessage: String by lazy {
    "--$branchNameArgumentName={branch-name} --$sha1ArgumentName={sha1-commit-hash}" +
        " command line arguments are required"
}

open class CalculateVersionNameTask : DefaultTask() {
    @Input
    @Option(option = "tag-name",
        description = "The name of the git tag, if the current build is tagged")
    var tagName: String = ""

    @Input
    @Option(option = branchNameArgumentName, description = "The name of the Git branch currently being built")
    var branchName: String = ""

    @Input
    @Option(option = sha1ArgumentName, description = "The SHA1 hash of the last commit of the current build")
    var sha1: String = ""

    @Input
    @Option(option = "pull-request-url", description = "The URL of the associated pull request")
    var pullRequestUrl: String = ""

    @TaskAction
    fun process() {
        val buildEnvironment = validateArguments()

        // TODO: Improve this error message
        val versionName = buildEnvironment.calculateVersionName()
            ?: throw IllegalStateException("Don't publish")
        println("${versionName}")
    }

    private fun validateArguments(): BuildEnvironment {
        require(branchName.isNotEmpty() && sha1.isNotEmpty()) {
            requiredArgumentsErrorMessage
        }

        return BuildEnvironment(
            tagName = tagName,
            branchName = branchName,
            sha1 = sha1,
            pullRequestUrl = pullRequestUrl
        )
    }
}
