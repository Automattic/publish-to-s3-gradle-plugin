package com.automattic.android.publish

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import java.io.File
import javax.inject.Inject

open class AiDocsExtension @Inject constructor(objects: ObjectFactory) {
    val sourceDirectory: DirectoryProperty = objects.directoryProperty()

    val dependencies: ListProperty<String> = objects.listProperty(String::class.java)

    fun from(directory: File) {
        sourceDirectory.set(directory)
    }

    fun from(directory: Directory) {
        sourceDirectory.set(directory)
    }

    fun from(directory: Provider<Directory>) {
        sourceDirectory.set(directory)
    }

    fun resolve(dependencyNotation: String) {
        dependencies.add(dependencyNotation)
    }
}
