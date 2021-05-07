package com.automattic.android.publish

import com.automattic.android.publish.CheckS3Version

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

abstract class CheckS3VersionTask : DefaultTask() {
    @Internal
    override fun getDescription(): String = "Checks if a version is published to S3"

    @get:Input
    @set:Option(option = "published-group-id", description = "Published group id")
    abstract var publishedGroupId: String

    @get:Input
    @set:Option(option = "published-artifact-id", description = "Published artifact id")
    abstract var artifactId: String

    @get:Input
    @set:Option(option = "version-name", description = "Version name to be checked")
    abstract var versionName: String

    @TaskAction
    fun process() {
        val result = CheckS3Version(publishedGroupId, artifactId, versionName).check()
        println("$result")
    }
}
