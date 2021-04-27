package com.automattic.android.publish

import org.gradle.api.provider.Property
import org.gradle.api.component.SoftwareComponent

abstract class PublishToS3PluginExtension {
    abstract val s3GroupId: Property<String>
    abstract val s3ModuleName: Property<String>
    abstract val s3VersionName: Property<String>
    //abstract val from: Property<SoftwareComponent>
}
