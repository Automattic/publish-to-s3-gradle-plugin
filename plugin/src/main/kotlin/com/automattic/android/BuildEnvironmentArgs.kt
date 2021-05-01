package com.automattic.android.publish

import com.automattic.android.publish.BuildEnvironment

private val requiredArgumentsErrorMessage: String by lazy {
    "Either tagName or both branch name and sha1 command line arguments needs to be provided!\n" +
    "Example usages:\n" +
    "--tagName={tag-name}\n" +
    "--pull-request-url={pull-request-url} --branch-name={branch-name} --sha1={sha1-commit-hash}\n" +
    "--branch-name={branch-name} --sha1={sha1-commit-hash}"
}

data class BuildEnvironmentArgs(
    val tagName: String?,
    val branchName: String?,
    val sha1: String?,
    val pullRequestUrl: String?
) {
    fun process(): BuildEnvironment =
        if (!tagName.isNullOrEmpty()) {
            BuildEnvironment.FromTag(tagName)
        } else {
            val sha1 = requireNotNullOrEmpty(sha1)

            if (!pullRequestUrl.isNullOrEmpty()) {
                BuildEnvironment.FromPR(pullRequestNumber(pullRequestUrl), sha1)
            } else {
                val branchName = sanitizeBranchName(requireNotNullOrEmpty(branchName))
                BuildEnvironment.FromBranch(branchName, sha1)
            }
        } 

    private fun pullRequestNumber(pullRequestUrl: String): String =
        pullRequestUrl.substringAfterLast("/")

    private fun sanitizeBranchName(branchName: String): String =
        branchName.replace("/", "_")
}

private fun requireNotNullOrEmpty(value: String?, errorMessage: String = requiredArgumentsErrorMessage): String {
    require(!value.isNullOrEmpty()) { errorMessage }
    return value
}

