package com.automattic.android.publish

import com.automattic.android.publish.BuildEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

const val developBranchName = "develop"
const val trunkBranchName = "trunk"
const val randomBranchName = "issue/branch-name"

const val tagName = "tag-123"
const val sha1 = "d40bd62a4d5566bf9681ecfd177e867f7b2ff3f9"
const val pullRequestNumber = "63"
const val pullRequestUrl = "https://github.com/wordpress-mobile/WordPress-Utils-Android/pull/$pullRequestNumber"

class BuildEnvironmentTest {
    @Test
    fun `returns {tagName} for non-empty tag`() {
        val tagName = "tag-123"
        val buildEnv = BuildEnvironment(
            tagName = tagName,
            branchName = developBranchName,
            sha1 = sha1,
            pullRequestUrl = pullRequestUrl
        )
        assertEquals(tagName, buildEnv.calculateVersionName())
    }

    @Test
    fun `returns {develop-sha1} for empty tag on develop branch`() {
        val tagName = "tag-123"
        val buildEnv = BuildEnvironment(
            tagName = null,
            branchName = developBranchName,
            sha1 = sha1,
            pullRequestUrl = pullRequestUrl
        )
        assertEquals("develop-$sha1", buildEnv.calculateVersionName())
    }

    @Test
    fun `returns {trunk-sha1} for empty tag on trunk branch`() {
        val tagName = "tag-123"
        val buildEnv = BuildEnvironment(
            tagName = null,
            branchName = trunkBranchName,
            sha1 = sha1,
            pullRequestUrl = pullRequestUrl
        )
        assertEquals("trunk-$sha1", buildEnv.calculateVersionName())
    }

    @Test
    fun `returns {pullRequestNumber-sha1} for empty tag on random branch`() {
        val tagName = "tag-123"
        val buildEnv = BuildEnvironment(
            tagName = null,
            branchName = randomBranchName,
            sha1 = sha1,
            pullRequestUrl = pullRequestUrl
        )
        assertEquals("$pullRequestNumber-$sha1", buildEnv.calculateVersionName())
    }

    @Test
    fun `returns null for empty tag and empty pull request url on random branch`() {
        val tagName = "tag-123"
        val buildEnv = BuildEnvironment(
            tagName = null,
            branchName = randomBranchName,
            sha1 = sha1,
            pullRequestUrl = ""
        )
        assertEquals(null, buildEnv.calculateVersionName())
    }
}
