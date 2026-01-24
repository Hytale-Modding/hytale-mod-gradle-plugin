package dev.hytalemods.gradle.hytalemod

enum class ServerJarSource {
    MAVEN_FATJAR,
    MAVEN_SQUASHED,
    GAME_FILES;


    companion object {
        fun fromString(value: String): ServerJarSource? {
            return entries.firstOrNull { it.name.lowercase() == value }
        }
    }
}
