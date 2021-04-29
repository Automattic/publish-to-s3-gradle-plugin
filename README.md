# Publish-to-s3 Gradle Plugin

This plugin makes it easier for us to publish Android library artifacts to our S3 repo.
We used to store our artifacts in JCenter, but since [Bintray was shutdown](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/) we decided to store the artifacts on S3 instead.

### Installation

In the library's main module's `build.gradle` file:

```
buildscript {
    repositories {
        maven { url 'https://a8c-libs.s3.amazonaws.com/android' }
    }
    dependencies {
        classpath 'com.automattic.android:publish-to-s3:0.1'
    }
}

apply plugin: 'com.automattic.android.publish-to-s3'
```

Unfortunately, we are currently unable to utilize the [Plugin DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block) due to [this Gradle issue](https://github.com/gradle/gradle/issues/8754):

```
> Could not determine artifacts for com.automattic.android.publish-to-s3:com.automattic.android.publish-to-s3.gradle.plugin:0.1
      > Could not get resource 'https://a8c-libs.s3.amazonaws.com/android/com/automattic/android/publish-to-s3/com.automattic.android.publish-to-s3.gradle.plugin/0.1/com.automattic.android.publish-to-s3.gradle.plugin-0.1.jar'.
         > Could not HEAD 'https://a8c-libs.s3.amazonaws.com/android/com/automattic/android/publish-to-s3/com.automattic.android.publish-to-s3.gradle.plugin/0.1/com.automattic.android.publish-to-s3.gradle.plugin-0.1.jar'. Received status code 403 from server: Forbidden
```

The gist of the issue is Gradle is looking for a JAR file for the [Plugin Marker Artifact](https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers) even though it should only utilize its dependencies.
[As explained by one of the Gradle developers](https://github.com/gradle/gradle/issues/8754#issuecomment-474011765), there is currently no way to get around these extra requests.
Since S3 returns 403 status code for nonexistent files, they result in a failed build.

### Configuration & Usage

In the library's main module's `build.gradle` file:

```
s3PublishPlugin {
    groupId = "{groupId}"
    artifactId = "{artifactId}"
    from = "release" // component name
}
```

