package com.automattic.android.publish

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

open class CalculateVersionNameTask : DefaultTask() {
    @Input
    @Option(option = "tagName",
        description = "The name of the git tag, if the current build is tagged.")
    var tagName: String = ""

    @Input
    @Option(description = "The name of the Git branch currently being built")
    lateinit var branchName: String

    @Input
    @Option(option = "sha1", description = "The SHA1 hash of the last commit of the current build")
    lateinit var sha1: String

    @Input
    @Option(description = "The URL of the associated pull request")
    lateinit var pullRequestUrl: String

    @TaskAction
    fun process() {
        println("${calculateVersionName()}")
    }

    private fun calculateVersionName() =
        if (tagName.isNotEmpty()) {
            tagName
        } else if (branchName == "develop" || branchName == "trunk") {
            "$branchName-$sha1"
        } else if (pullRequestUrl.isNotEmpty()) {
            "${pullRequestUrl.substringAfterLast("/")}-$sha1"
        } else {
            throw IllegalStateException("Don't publish")
        }
}
