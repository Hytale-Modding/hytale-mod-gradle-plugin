package dev.hytalemods.gradle.hytalemod.util

import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

internal val SourceSetContainer.main: SourceSet get() = this.getByName("main")
