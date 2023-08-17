package com.automattic.android.publish

const val ARG_TAG_NAME = "tag-name"
const val ARG_BRANCH_NAME = "branch-name"
const val ARG_SHA1 = "sha1"
const val ARG_PR_NUMBER = "pull-request-number"

private val requiredArgumentsErrorMessage: String by lazy {
    "Either tagName or both branch name and sha1 command line arguments needs to be provided!\n" +
    "Example usages:\n" +
    "--$ARG_TAG_NAME={tag-name}\n" +
    "--$ARG_PR_NUMBER={pull-request-number} --$ARG_BRANCH_NAME={branch-name} --$ARG_SHA1={sha1-commit-hash}\n" +
    "--$ARG_BRANCH_NAME={branch-name} --$ARG_SHA1={sha1-commit-hash}"
}

data class BuildEnvironmentArgs(
    val tagName: String?,
    val branchName: String?,
    val sha1: String?,
    val pullRequestNumber: String?
) {
    fun process(): BuildEnvironment =
        if (!tagName.isNullOrEmpty()) {
            BuildEnvironment.FromTag(tagName)
        } else {
            val sha1 = requireNotNullOrEmpty(sha1)

            if (!pullRequestNumber.isNullOrEmpty()) {
                BuildEnvironment.FromPR(pullRequestNumber, sha1)
            } else {
                val branchName = sanitizeBranchName(requireNotNullOrEmpty(branchName))
                BuildEnvironment.FromBranch(branchName, sha1)
            }
        } 

    private fun sanitizeBranchName(branchName: String): String =
        branchName.replace("/", "_")
}

private fun requireNotNullOrEmpty(value: String?, errorMessage: String = requiredArgumentsErrorMessage): String {
    require(!value.isNullOrEmpty()) { errorMessage }
    return value
}
