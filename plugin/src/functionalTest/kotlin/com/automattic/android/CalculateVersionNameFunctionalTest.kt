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

class CalculateVersionNameFunctionalTest {
    @Test
    fun `fails for missing branch name`() {
        val runner = publishToS3PluginFunctionalTestRunnerWithArguments("-q",
            "calculateVersionName", "--sha1=$randomSha1")
        runner.buildAndFail()
    }

    @Test
    fun `fails for missing sha1`() {
        val runner = publishToS3PluginFunctionalTestRunnerWithArguments("-q",
            "calculateVersionName", "--branch-name=$randomBranchName")
        runner.buildAndFail()
    }

    @Test
    fun `fails for random branch for missing pull request url`() {
        val runner = publishToS3PluginFunctionalTestRunnerWithArguments("-q",
            "calculateVersionName", "--branch-name=$randomBranchName", "--sha1=$randomSha1")
        runner.buildAndFail()
    }

    @Test
    fun `succeeds for develop branch for missing pull request url`() {
        val runner = publishToS3PluginFunctionalTestRunnerWithArguments("-q",
            "calculateVersionName", "--branch-name=$developBranchName", "--sha1=$randomSha1")
        runner.build()
    }

    @Test
    fun `succeeds for trunk branch for missing pull request url`() {
        val runner = publishToS3PluginFunctionalTestRunnerWithArguments("-q",
            "calculateVersionName", "--branch-name=$trunkBranchName", "--sha1=$randomSha1")
        runner.build()
    }

    @Test
    fun `returns tag name if given`() {
        val runner = publishToS3PluginFunctionalTestRunnerWithArguments("-q",
            "calculateVersionName", "--branch-name=$developBranchName", "--sha1=$randomSha1",
            "--tag-name=$randomTagName")
        val result = runner.build()
        assertEquals(randomTagName, result.output.trim())
    }
}
