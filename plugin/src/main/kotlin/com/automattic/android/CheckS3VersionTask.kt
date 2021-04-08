package com.automattic.android.publish

import java.net.HttpURLConnection
import java.net.URL

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

private const val SUCCESS_STATUS_CODE = 200
private const val FAILURE_STATUS_CODE = 403

open class CheckS3VersionTask : DefaultTask() {
    @Internal
    override fun getDescription(): String = "Checks if a version is already published to S3"

    @Input
    @Option(option = "published-group-id", description = "Group id used to publish the artifacts")
    var publishedGroupId: String = ""

    @Input
    @Option(option = "module-name", description = "Module name of the published artifacts")
    var moduleName: String = ""

    @Input
    @Option(option = "version-name", description = "Version name to be checked")
    var versionName: String = ""

    private val pomUrl: String by lazy {
        "https://a8c-libs.s3.amazonaws.com/android/${publishedGroupId.replace(".", "/")}" +
            "/$versionName/$moduleName-${versionName}.pom"
    }

    @TaskAction
    fun process() {
        when (responseCodeForUrl(pomUrl)) {
            SUCCESS_STATUS_CODE -> println("true")
            FAILURE_STATUS_CODE -> println("false")
            else -> handleUnexpectedResponseCode()
        }
    }

    private fun responseCodeForUrl(url: String): Int =
        with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "HEAD"
            connect()
            responseCode
        }

    private fun handleUnexpectedResponseCode() {
        throw IllegalStateException(
            "An unexpected response code received with the following information. " +
                "Please contact Platform 9 team if this issue persists.\n\n" +

                "--Input--\n" +
                "publishedGroupId: $publishedGroupId\n" +
                "moduleName: $moduleName\n" +
                "versionName: $versionName\n\n" +

                "--Calculated--\n" +
                "POM url: $pomUrl\n"
        )
    }
}
