package dev.hytalemods.gradle.hytalemod.util

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven

internal fun RepositoryHandler.withCodeMCMaven() {
    exclusiveContent {
        forRepository {
            maven("https://repo.codemc.io/repository/hytale")
        }
        filter {
            includeModule("com.hypixel.hytale", "Server")
            includeModule("com.hypixel.hytale", "Server-squashed")
        }
    }
}
