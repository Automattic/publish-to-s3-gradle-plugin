# Publish-to-s3 Gradle Plugin

This plugin aims to makes it easier to publish Android artifacts to our S3 Maven repository.

### Setup

**Use `com.automattic.android.publish-to-s3-without-sources` instead of `com.automattic.android.publish-to-s3` if the sources should not be published.**

In the projects's `settings.gradle` file:

```groovy
pluginManagement {
    plugins {
        id "com.automattic.android.publish-to-s3" version {version}
    }
    repositories {
        maven {
            url 'https://a8c-libs.s3.amazonaws.com/android'
            content {
                includeGroup "com.automattic.android"
                includeGroup "com.automattic.android.publish-to-s3"
            }
        }
    }
}
```

In each module that needs to be published:

```groovy
plugins {
    id "com.automattic.android.publish-to-s3"
}

// A publication should be added following maven-publish plugin documentation: https://docs.gradle.org/current/userguide/publishing_maven.html
// For Android artifacts, Google's documentation should be used instead: https://developer.android.com/studio/build/maven-publish-plugin
// The main difference for Android artifacts is that they have access to `components.release`. However, it's only available after the project is evaluated, so the publishing block should be wrapped in `afterEvaluate`.
project.afterEvaluate {
    publishing {
        publications {
            maven(MavenPublication) {
                from components.release

                groupId = 'org.gradle.sample'
                artifactId = 'library'
                // version is set by `publish-to-s3` plugin in `prepareToPublishToS3` task, so it should be omitted
            }
        }
    }
}
```

### Usage

The plugin applies `maven-publish` plugin since it can not work without it. It'll also add our S3 repository to it, so it doesn't need to be added for each project. The main Gradle task is `prepareToPublishToS3` which will take a set of command line arguments, calculate a version name, check if that version is already published and set **all** MavenPublications' versions to the calculated value. The plugin will also create a new `published-version.txt` file in the modules `build` folder after artifacts are successfully published.

```
./gradlew :{moduleName}:prepareToPublishToS3 {command line arguments} :{moduleName}:publish
```

Here are the available command line arguments: `--tag-name`, `--pull-request-number`, `--sha1`, `--branch-name`

* If `--tag-name` is provided, version will be `tag-name`
* Else if `--pull-request-number` is provided, version will be `{pull-request-number}-{sha1}`
* Else, version will be `{branch-name}-{sha1}`

The plugin also provides the following helper tasks:

* `calculateVersionName` takes the same `--branch-name`, `--sha1`, `--tag-name` & `--pull-request-number` command line arguments and prints the calculated version name.
* `isVersionPublishedToS3` takes  `--version-name` command line argument and verifies that none of the publications defined for the module has already been published to S3.

### Notes


* Due to a [limitation in Gradle](https://docs.gradle.org/current/userguide/custom_tasks.html#limitations), even though `prepareToPublishToS3` is a dependency for `publish` task, we can not use the following notation:

```
./gradlew :{moduleName}:publish {command line arguments}
```

* `publish` task is an aggregate task which will publish all defined publications to all defined repositories.
So, if a specific behavior is required [the documentation](https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:tasks) should be consulted to find the correct name of the task. Alternatively, `./gradlew tasks --all` can be used.
