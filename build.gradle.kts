plugins {
    id("com.automattic.android.publish-to-s3")
}

configureS3PublishPlugin {
    s3GroupId.set("com.automattic.android")
    s3ModuleName.set("publish-to-s3")
    s3VersionName.set("0.1")
}

