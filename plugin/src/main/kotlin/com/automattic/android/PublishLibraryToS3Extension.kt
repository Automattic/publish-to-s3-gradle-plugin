package com.automattic.android.publish

interface PublishLibraryToS3Extension {
    var groupId: String
    var artifactId: String
    var from: String?
}
