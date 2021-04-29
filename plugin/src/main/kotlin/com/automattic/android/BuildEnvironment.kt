package com.automattic.android.publish

const val BRANCH_NAME_ARGUMENT_NAME = "branch-name"
const val SHA1_ARGUMENT_NAME = "sha1"

private val requiredArgumentsErrorMessage: String by lazy {
    "--$BRANCH_NAME_ARGUMENT_NAME={branch-name} --$SHA1_ARGUMENT_NAME={sha1-commit-hash}" +
        " command line arguments are required"
}

data class BuildEnvironment(
    val tagName: String?,
    val branchName: String,
    val sha1: String,
    val pullRequestUrl: String?
) {
    init {
        require(branchName.isNotEmpty() && sha1.isNotEmpty()) {
            requiredArgumentsErrorMessage
        }
    }

    fun calculateVersionName(): String =
        if (!tagName.isNullOrEmpty()) {
            tagName
        } else if (!pullRequestUrl.isNullOrEmpty()) {
            "${pullRequestUrl.substringAfterLast("/")}-$sha1"
        } else {
            "$branchName-$sha1"
        } 
}

