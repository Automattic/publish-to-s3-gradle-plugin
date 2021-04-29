plugins {
    id("com.automattic.android.publish-to-s3")
}

s3PublishPlugin {
    groupId = "com.automattic.android"
    artifactId = "publish-to-s3"
    suppressWarnings = true
}

