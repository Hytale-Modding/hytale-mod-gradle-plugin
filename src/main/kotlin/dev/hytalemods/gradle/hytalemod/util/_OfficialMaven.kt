package dev.hytalemods.gradle.hytalemod.util

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven

fun RepositoryHandler.withHytaleMaven(patchline: String) {
    exclusiveContent {
        forRepository {
            maven("https://maven.hytale.com/${patchline}")
        }
        filter {
            includeModule("com.hypixel.hytale", "Server")
        }
    }
}
