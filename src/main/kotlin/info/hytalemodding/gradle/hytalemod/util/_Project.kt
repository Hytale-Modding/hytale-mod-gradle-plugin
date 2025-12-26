package info.hytalemodding.gradle.hytalemod.util

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer

internal val Project.sourceSets: SourceSetContainer get() = (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer
