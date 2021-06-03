# Publish-to-s3 Gradle Plugin

This plugin aims to makes it easier to publish Android library artifacts to our S3 repo.
We used to store our artifacts in JCenter, but since [Bintray was shutdown](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/) we decided to store the artifacts on S3 instead.

### Installation

In the library's main module's `build.gradle` file:

```groovy
buildscript {
    repositories {
        maven { url 'https://a8c-libs.s3.amazonaws.com/android' }
    }
    dependencies {
        classpath 'com.automattic.android:publish-to-s3:0.4.1'
    }
}

apply plugin: 'com.automattic.android.publish-library-to-s3'

s3PublishLibrary {
    groupId = "{groupId}"
    artifactId = "{artifactId}"
    from = "release" // component name
}
```

The configuration is similar to [maven-publish](https://docs.gradle.org/current/userguide/publishing_maven.html#sec:identity_values_in_the_generated_pom) plugin, but provides a smaller set of options. We'll most likely add new options as they become necessary. There are 2 main differences to know about:

1. `version` is calculated within the plugin
2. `from` accepts a string instead of a component. This is because android library components are only available in `afterEvaluate` stage which means we don't have access to them at the time of plugin application. Using a string value lets us get around this issue and keep the client build configurations cleaner. More information about components can be found in the following documentations:

https://docs.gradle.org/current/userguide/publishing_customization.html

https://developer.android.com/studio/build/maven-publish-plugin

### Usage

The plugin applies & configures the `maven-publish` plugin by adding the S3 repository as well as a `MavenPublication` that uses the calculated version name. It provides 3 Gradle tasks:

* `publishLibraryToS3` can take a mix of the following command line arguments: `--branch-name`, `--sha1`, `--tag-name`, `--pull-request-url`. Depending on the arguments passed, it uses the information to calculate a version name (see below), verify that this version is not already published to S3 and finally calls `publishS3PublicationToS3Repository` gradle task which will publish the artifacts to S3.
* `calculateVersionName` takes `--branch-name`, `--sha1`, `--tag-name` & `--pull-request-url` command line arguments and prints the calculated version name.
* `isVersionPublishedToS3` takes  `--version-name` command line argument and combines it with the `groupId` & `artifactId` values from the extension to check if it's already published to S3.

Here is how the version is calculated:

* If `--tag-name` is provided, returns `tag-name`
* Else if `--pull-request-url` is provided, returns `{pull-request-number}-{sha1}`
* Else, returns `{branch-name}-{sha1}`

### Lack of Plugin DSL support

Unfortunately, we are currently unable to utilize the [Plugin DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block) due to [this Gradle issue](https://github.com/gradle/gradle/issues/8754):

```
> Could not determine artifacts for com.automattic.android.publish-to-s3:com.automattic.android.publish-to-s3.gradle.plugin:0.1
      > Could not get resource 'https://a8c-libs.s3.amazonaws.com/android/com/automattic/android/publish-to-s3/com.automattic.android.publish-to-s3.gradle.plugin/0.1/com.automattic.android.publish-to-s3.gradle.plugin-0.1.jar'.
         > Could not HEAD 'https://a8c-libs.s3.amazonaws.com/android/com/automattic/android/publish-to-s3/com.automattic.android.publish-to-s3.gradle.plugin/0.1/com.automattic.android.publish-to-s3.gradle.plugin-0.1.jar'. Received status code 403 from server: Forbidden
```

The gist of the issue is that Gradle is looking for a JAR file for the [Plugin Marker Artifact](https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers) even though it should only utilize its dependencies.
[As explained by one of Gradle developers](https://github.com/gradle/gradle/issues/8754#issuecomment-474011765), there is currently no way to get around these extra requests.
Since S3 returns 403 status code for nonexistent files, the build results in failure.
