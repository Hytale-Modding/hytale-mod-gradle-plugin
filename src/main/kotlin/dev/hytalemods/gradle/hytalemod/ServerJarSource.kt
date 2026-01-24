package dev.hytalemods.gradle.hytalemod

import dev.hytalemods.gradle.hytalemod.util.ci
import org.gradle.api.Project

enum class ServerJarSource {
    MAVEN_FATJAR,
    MAVEN_SQUASHED,
    GAME_FILES;


    companion object {
        fun fromString(value: String): ServerJarSource? {
            return entries.firstOrNull { it.name.lowercase() == value }
        }

        fun defaultFor(project: Project): ServerJarSource {
            return if (project.ci.get()) MAVEN_FATJAR else GAME_FILES
        }
    }
}
