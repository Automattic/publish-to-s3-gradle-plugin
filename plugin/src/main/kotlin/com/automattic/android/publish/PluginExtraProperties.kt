package com.automattic.android.publish

import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension

val Project.extraProperties: ExtraPropertiesExtension
    get() = project.getExtensions().getByType(ExtraPropertiesExtension::class.java)
