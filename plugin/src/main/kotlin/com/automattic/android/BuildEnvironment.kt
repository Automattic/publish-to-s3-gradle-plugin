package com.automattic.android.publish

data class BuildEnvironment(
    val tagName: String?,
    val branchName: String,
    val sha1: String,
    val pullRequestUrl: String
) {
    fun calculateVersionName(): String? =
        if (!tagName.isNullOrEmpty()) {
            tagName
        } else if (branchName == "develop" || branchName == "trunk") {
            "$branchName-$sha1"
        } else if (pullRequestUrl.isNotEmpty()) {
            "${pullRequestUrl.substringAfterLast("/")}-$sha1"
        } else {
            null
        }
}
