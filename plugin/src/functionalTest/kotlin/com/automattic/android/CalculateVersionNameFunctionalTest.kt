package com.automattic.android.publish

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import kotlin.test.Test
import kotlin.test.assertEquals

private const val randomSha1: String = "random-sha1"
private const val developBranchName: String = "develop"
private const val trunkBranchName: String = "trunk"
private const val randomBranchName: String = "issue/random-branch-name"
private const val randomTagName = "randomTagName"
private const val pullRequestNumber = "63"
private const val pullRequestUrl = "https://github.com/wordpress-mobile/WordPress-Utils-Android/pull/$pullRequestNumber"

class CalculateVersionNameFunctionalTest {
    @Test
    fun `fails for missing branch name`() {
        val runner = publishToS3BasePluginFunctionalTestRunnerWithArguments("-q",
            "calculateVersionName", "--sha1=$randomSha1")
        runner.buildAndFail()
    }

    @Test
    fun `fails for missing sha1`() {
        val runner = publishToS3BasePluginFunctionalTestRunnerWithArguments("-q",
            "calculateVersionName", "--branch-name=$randomBranchName")
        runner.buildAndFail()
    }

    @Test
    fun `returns {tagName} for non-empty tag`() {
        val runner = publishToS3BasePluginFunctionalTestRunnerWithArguments("-q",
            "calculateVersionName", "--branch-name=$developBranchName", "--sha1=$randomSha1",
            "--tag-name=$randomTagName")
        val result = runner.build()
        assertEquals(randomTagName, result.output.trim())
    }

    @Test
    fun `returns {pullRequestNumber-sha1}`() {
        val runner = publishToS3BasePluginFunctionalTestRunnerWithArguments("-q",
            "calculateVersionName", "--branch-name=$developBranchName", "--sha1=$randomSha1",
            "--pull-request-url=$pullRequestUrl")
        val result = runner.build()
        assertEquals("$pullRequestNumber-$randomSha1", result.output.trim())
    }

    @Test
    fun `returns {branchName-sha1} for empty tag and empty pull request url`() {
        listOf(developBranchName, trunkBranchName, randomBranchName).forEach { branchName ->
            val runner = publishToS3BasePluginFunctionalTestRunnerWithArguments("-q",
                "calculateVersionName", "--branch-name=$branchName", "--sha1=$randomSha1")
            val result = runner.build()
            val sanitizedBranchName = branchName.replace("/", "_")
            assertEquals("$sanitizedBranchName-$randomSha1", result.output.trim())
        }
    }
}

