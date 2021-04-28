package com.automattic.android.publish

import org.gradle.api.provider.Property
import org.gradle.api.component.SoftwareComponent

abstract class PublishToS3PluginExtension {
    abstract val groupId: Property<String>
    abstract val artifactId: Property<String>
    abstract val from: Property<String>
}
