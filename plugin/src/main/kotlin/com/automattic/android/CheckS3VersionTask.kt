package com.automattic.android.publish

import com.automattic.android.publish.CheckS3Version

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

abstract class CheckS3VersionTask : DefaultTask() {
    @Internal
    override fun getDescription(): String = "Checks if a version is already published to S3"

    @get:Input
    abstract var publishedGroupId: String

    @get:Input
    abstract var moduleName: String

    @get:Input
    abstract var versionName: String

    @TaskAction
    fun process() {
        val result = CheckS3Version(publishedGroupId, moduleName, versionName).check()
        println("$result")
    }
}
