package com.automattic.android.publish

import org.gradle.api.provider.Property

abstract class PublishToS3PluginExtension {
    abstract val s3GroupId: Property<String>
    abstract val s3ModuleName: Property<String>
    abstract val s3VersionName: Property<String>
}
