package com.automattic.android.publish

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckS3VersionFunctionalTest {
    @Test
    fun `verify version exists`() {
        val runner = functionalTestRunner("-q",
            "isVersionPublishedToS3", "--version-name=1.40.0",
            "--published-group-id=org.wordpress", "--published-artifact-id=utils"
        )
        val result = runner.build()
        assertTrue(result.output.trim().toBoolean())
    }

    @Test
    fun `verify version does not exist`() {
        val runner = functionalTestRunner("-q",
            "isVersionPublishedToS3", "--version-name=thisversiondoesntexist",
            "--published-group-id=org.wordpress", "--published-artifact-id=utils"
        )
        val result = runner.build()
        assertFalse(result.output.trim().toBoolean())
    }
}
