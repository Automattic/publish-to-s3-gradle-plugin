package com.automattic.android.publish

import java.net.HttpURLConnection
import java.net.URL

private const val SUCCESS_STATUS_CODE = 200
private const val FORBIDDEN_STATUS_CODE = 403
private const val FAILURE_STATUS_CODE = 404

class CheckS3Version(
    val publishedGroupId: String,
    val moduleName: String,
    val versionName: String
) {
    private val pomUrl: String by lazy {
        "https://a8c-libs.s3.amazonaws.com/android/${publishedGroupId.replace(".", "/")}/" +
            "$moduleName/$versionName/$moduleName-${versionName}.pom"
    }

    private val unexpectedStatusCodeMessage: String by lazy {
        "An unexpected response code received with the following information. " +
            "Please contact Platform 9 team if this issue persists.\n\n" +

            "--Input--\n" +
            "publishedGroupId: $publishedGroupId\n" +
            "moduleName: $moduleName\n" +
            "versionName: $versionName\n\n" +

            "--Calculated--\n" +
            "POM url: $pomUrl\n"
    }

    fun check(): Boolean =
        when (responseCodeForUrl(pomUrl)) {
            SUCCESS_STATUS_CODE -> true
            // Dependending on ACL settings S3 may return 403 or 404 for a missing file
            FAILURE_STATUS_CODE, FORBIDDEN_STATUS_CODE -> false
            else -> throw IllegalStateException(unexpectedStatusCodeMessage)
        }

    private fun responseCodeForUrl(url: String): Int =
        with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "HEAD"
            connect()
            responseCode
        }
}

