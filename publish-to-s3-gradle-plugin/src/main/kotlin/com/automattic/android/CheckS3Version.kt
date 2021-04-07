package com.automattic.android.publish

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input

open class CheckS3VersionTask : DefaultTask() {
    @Input
    @Option(description = "Configures the package path")
    var packagePath: String? = null

    @TaskAction
    fun process() {
        if (packagePath.isNullOrEmpty()) {
            //throw java.lang.IllegalArgumentException("packagePath can not be null or empty")
        }

        println("packagePath argument: $packagePath")
    }
}
