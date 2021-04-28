package com.automattic.android.publish

import org.gradle.api.provider.Property
import org.gradle.api.component.SoftwareComponent

abstract class PublishToS3PluginExtension {
    abstract var groupId: String
    abstract var artifactId: String
    abstract var from: String
}
