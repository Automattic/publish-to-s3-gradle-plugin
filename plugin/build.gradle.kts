plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.4.32"
    id("io.gitlab.arturbosch.detekt").version("1.16.0")
    id("maven-publish")
}

repositories {
    jcenter()
}

group = "com.automattic.android"
version = "0.1"

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
    plugins.register("publish") {
        id = "com.automattic.android.publish-to-s3"
        implementationClass = "com.automattic.android.publish.PublishToS3Plugin"
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest")
gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

tasks.check {
    dependsOn(functionalTest)
}

detekt {
    buildUponDefaultConfig = true

    reports {
        html.enabled = true
    }
}

val awsAccessKey: String? by project
val awsSecretKey: String? by project
publishing {
    repositories {
        maven {
            url = uri("s3://a8c-libs.s3.amazonaws.com/android")
            credentials(AwsCredentials::class) {
                accessKey = awsAccessKey ?: System.getenv("AWS_ACCESS_KEY")
                secretKey = awsSecretKey ?: System.getenv("AWS_SECRET_KEY")
            }
        }
    }
}
