package com.automattic.android.publish

import java.net.HttpURLConnection
import java.net.URL

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input

open class CheckS3VersionTask : DefaultTask() {
    @Input
    @Option(description = "Configures the package path")
    lateinit var packagePath: String

    @Input
    @Option(description = "")
    lateinit var moduleName: String

    @Input
    @Option(description = "")
    lateinit var versionName: String

    private val pomUrl: String by lazy {
        "https://a8c-libs.s3.amazonaws.com/android/${packagePath.replace(".", "/")}/$versionName/$moduleName-${versionName}.pom"
    }

    @TaskAction
    fun process() {
        when (responseCodeForUrl(pomUrl)) {
            200 -> println("true")
            403 -> println("false")
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
"""An unexpected response code received with the following information. Please contact Platform 9 team if this issue persists.

--Input--
packagePath: $packagePath
moduleName: $moduleName
versionName: $versionName

--Calculated--
POM url: $pomUrl
""")
    }
}
