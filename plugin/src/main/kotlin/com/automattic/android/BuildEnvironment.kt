package com.automattic.android.publish

const val BRANCH_NAME_ARGUMENT_NAME = "branch-name"
const val SHA1_ARGUMENT_NAME = "sha1"

private val requiredArgumentsErrorMessage: String by lazy {
    "--$BRANCH_NAME_ARGUMENT_NAME={branch-name} --$SHA1_ARGUMENT_NAME={sha1-commit-hash}" +
        " command line arguments are required"
}

private const val DONT_PUBLISH_ERROR_MESSAGE = "Since this build is not from a tag and it's on a " +
    "branch without a pull request url, it shouldn't be published to S3."

data class BuildEnvironment(
    val tagName: String?,
    val branchName: String,
    val sha1: String,
    val pullRequestUrl: String
) {
    init {
        require(branchName.isNotEmpty() && sha1.isNotEmpty()) {
            requiredArgumentsErrorMessage
        }
    }

    fun calculateVersionName(): String =
        if (!tagName.isNullOrEmpty()) {
            tagName
        } else if (branchName == "develop" || branchName == "trunk") {
            "$branchName-$sha1"
        } else if (pullRequestUrl.isNotEmpty()) {
            "${pullRequestUrl.substringAfterLast("/")}-$sha1"
        } else {
            throw IllegalStateException(DONT_PUBLISH_ERROR_MESSAGE)
        }
}

