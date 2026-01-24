package dev.hytalemods.gradle.hytalemod.util

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer

internal val Project.sourceSets: SourceSetContainer get() = (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer

internal val Project.ci: Provider<Boolean> get() = providers.environmentVariable("CI").map { it.toBoolean() }.orElse(false)
