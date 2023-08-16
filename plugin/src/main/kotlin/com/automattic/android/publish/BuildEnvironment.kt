package com.automattic.android.publish

sealed class BuildEnvironment {
    data class FromTag(val tagName: String): BuildEnvironment()
    data class FromPR(val pullRequestNumber: String, val sha1: String): BuildEnvironment()
    data class FromBranch(val branchName: String, val sha1: String): BuildEnvironment()

    val versionName: String by lazy {
        when (this) {
            is FromTag -> tagName
            is FromPR -> "$pullRequestNumber-$sha1"
            is FromBranch -> "$branchName-$sha1"
        }
    }
}
