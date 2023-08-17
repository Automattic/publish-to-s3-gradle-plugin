package com.automattic.android.publish

import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension

val Project.extraProperties: ExtraPropertiesExtension
    get() = project.extensions.getByType(ExtraPropertiesExtension::class.java)
