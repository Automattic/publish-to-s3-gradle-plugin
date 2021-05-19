package com.automattic.android.publish

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ListAllTasksFunctionalTest {
    @Test
    fun `lists all publish helpers tasks`() {
        helpersPluginFunctionalTestRunner("-q", "tasks", "--all").build()
    }

    @Test
    fun `lists all publish library tasks`() {
        publishLibraryFunctionalTestRunner("-q", "tasks", "--all").build()
    }

    @Test
    fun `lists all publish plugin tasks`() {
        publishPluginFunctionalTestRunner("-q", "tasks", "--all").build()
    }
}
