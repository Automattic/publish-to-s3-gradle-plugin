package com.automattic.android.publish

import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.ExtensionContainer

val Project.extraProperties: ExtraPropertiesExtension
    get() = project.getExtensions().getByType(ExtraPropertiesExtension::class.java)
