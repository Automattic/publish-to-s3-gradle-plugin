package com.automattic.android.publish

import com.automattic.android.publish.BuildEnvironment
import com.automattic.android.publish.BuildEnvironmentArgs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val developBranchName = "develop"
private const val trunkBranchName = "trunk"
private const val randomBranchName = "issue/branch-name"

private const val tagName = "tag-123"
private const val sha1 = "d40bd62a4d5566bf9681ecfd177e867f7b2ff3f9"
private const val pullRequestNumber = "63"
private const val pullRequestUrl = "https://github.com/wordpress-mobile/WordPress-Utils-Android/pull/$pullRequestNumber"

class BuildEnvironmentTest {
    @Test
    fun `returns {tagName} for non-empty tag`() {
        val tagName = "tag-123"
        val buildEnv = BuildEnvironmentArgs(
            tagName = tagName,
            branchName = developBranchName,
            sha1 = sha1,
            pullRequestUrl = pullRequestUrl
        ).process()
        assertEquals(tagName, buildEnv.versionName)
    }

    @Test
    fun `returns {pullRequestNumber-sha1} for empty tag on random branch`() {
        val buildEnv = BuildEnvironmentArgs(
            tagName = null,
            branchName = null,
            sha1 = sha1,
            pullRequestUrl = pullRequestUrl
        ).process()
        assertEquals("$pullRequestNumber-$sha1", buildEnv.versionName)
    }
    @Test
    fun `returns {branchName-sha1} for empty tag and empty pull request url`() {
        listOf(developBranchName, trunkBranchName, randomBranchName).forEach { branchName ->
            val buildEnv = BuildEnvironmentArgs(
                tagName = null,
                branchName = branchName,
                sha1 = sha1,
                pullRequestUrl = null
            ).process()
            assertEquals("$branchName-$sha1", buildEnv.versionName)
        }
    }
}
