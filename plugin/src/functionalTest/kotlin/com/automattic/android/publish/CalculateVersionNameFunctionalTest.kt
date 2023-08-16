package com.automattic.android.publish

import kotlin.test.Test
import kotlin.test.assertEquals

private const val DEVELOP_BRANCH_NAME: String = "develop"
private const val TRUNK_BRANCH_NAME: String = "trunk"
private const val RANDOM_BRANCH_NAME: String = "issue/random-branch-name"

private const val RANDOM_TAG_NAME = "randomTagName"
private const val RANDOM_SHA1: String = "random-sha1"
private const val PULL_REQUEST_NUMBER = "63"

class CalculateVersionNameFunctionalTest {
    @Test
    fun `fails for missing branch name`() {
        val runner = functionalTestRunner("-q",
            "calculateVersionName", "--sha1=$RANDOM_SHA1")
        runner.buildAndFail()
    }

    @Test
    fun `fails for missing sha1`() {
        val runner = functionalTestRunner("-q",
            "calculateVersionName", "--branch-name=$RANDOM_BRANCH_NAME")
        runner.buildAndFail()
    }

    @Test
    fun `returns {tagName} for non-empty tag`() {
        val runner = functionalTestRunner("-q",
            "calculateVersionName", "--branch-name=$DEVELOP_BRANCH_NAME", "--sha1=$RANDOM_SHA1",
            "--tag-name=$RANDOM_TAG_NAME")
        val result = runner.build()
        assertEquals(RANDOM_TAG_NAME, result.output.trim())
    }

    @Test
    fun `returns {pullRequestNumber-sha1}`() {
        val runner = functionalTestRunner("-q",
            "calculateVersionName", "--branch-name=$DEVELOP_BRANCH_NAME", "--sha1=$RANDOM_SHA1",
            "--pull-request-number=$PULL_REQUEST_NUMBER")
        val result = runner.build()
        assertEquals("$PULL_REQUEST_NUMBER-$RANDOM_SHA1", result.output.trim())
    }

    @Test
    fun `returns {branchName-sha1} for empty tag and empty pull request number`() {
        listOf(DEVELOP_BRANCH_NAME, TRUNK_BRANCH_NAME, RANDOM_BRANCH_NAME).forEach { branchName ->
            val runner = functionalTestRunner("-q",
                "calculateVersionName", "--branch-name=$branchName", "--sha1=$RANDOM_SHA1")
            val result = runner.build()
            val sanitizedBranchName = branchName.replace("/", "_")
            assertEquals("$sanitizedBranchName-$RANDOM_SHA1", result.output.trim())
        }
    }
}
