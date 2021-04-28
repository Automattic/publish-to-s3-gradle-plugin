plugins {
    id("com.automattic.android.publish-to-s3")
}

configureS3PublishPlugin {
    groupId.set("com.automattic.android")
    artifactId.set("publish-to-s3")
}

