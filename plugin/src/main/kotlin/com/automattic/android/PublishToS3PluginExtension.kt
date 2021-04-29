package com.automattic.android.publish

import org.gradle.api.provider.Property
import org.gradle.api.component.SoftwareComponent

interface PublishToS3PluginExtension {
    var groupId: String
    var artifactId: String
    var from: String?
}
