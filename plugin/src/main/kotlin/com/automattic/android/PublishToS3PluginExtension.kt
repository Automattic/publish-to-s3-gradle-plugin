package com.automattic.android.publish

interface PublishToS3PluginExtension {
    var groupId: String
    var artifactId: String
    var from: String?
}
