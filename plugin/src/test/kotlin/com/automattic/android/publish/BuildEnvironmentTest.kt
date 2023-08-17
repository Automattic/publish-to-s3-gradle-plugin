package com.automattic.android.publish

import kotlin.test.Test
import kotlin.test.assertEquals

private const val DEVELOP_BRANCH_NAME: String = "develop"
private const val TRUNK_BRANCH_NAME: String = "trunk"
private const val RANDOM_BRANCH_NAME: String = "issue/random-branch-name"

private const val RANDOM_TAG_NAME = "randomTagName"
private const val RANDOM_SHA1: String = "random-sha1"
private const val PULL_REQUEST_NUMBER = "63"

class BuildEnvironmentTest {
    @Test
    fun `returns {tagName} for non-empty tag`() {
        val buildEnv = BuildEnvironmentArgs(
            tagName = RANDOM_TAG_NAME,
            branchName = DEVELOP_BRANCH_NAME,
            sha1 = RANDOM_SHA1,
            pullRequestNumber = PULL_REQUEST_NUMBER
        ).process()
        assertEquals(RANDOM_TAG_NAME, buildEnv.versionName)
    }

    @Test
    fun `returns {pullRequestNumber-sha1} for empty tag on random branch`() {
        val buildEnv = BuildEnvironmentArgs(
            tagName = null,
            branchName = null,
            sha1 = RANDOM_SHA1,
            pullRequestNumber = PULL_REQUEST_NUMBER
        ).process()
        assertEquals("$PULL_REQUEST_NUMBER-$RANDOM_SHA1", buildEnv.versionName)
    }
    @Test
    fun `returns {branchName-sha1} for empty tag and empty pull request number`() {
        listOf(DEVELOP_BRANCH_NAME, TRUNK_BRANCH_NAME, RANDOM_BRANCH_NAME).forEach { branchName ->
            val buildEnv = BuildEnvironmentArgs(
                tagName = null,
                branchName = branchName,
                sha1 = RANDOM_SHA1,
                pullRequestNumber = null
            ).process()
            val sanitizedBranchName = branchName.replace("/", "_")
            assertEquals("$sanitizedBranchName-$RANDOM_SHA1", buildEnv.versionName)
        }
    }
}
