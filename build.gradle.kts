plugins {
    idea
    `kotlin-dsl`
    `maven-publish`
}

group = "info.hytalemodding"
version = providers.environmentVariable("PLUGIN_VERSION").orNull ?: "0.0.0-development"

// Kotlin does not support building with Java 25 yet.
val javaVersion = 21

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    implementation("net.harawata:appdirs:1.5.0")
    implementation("org.jetbrains.gradle.plugin.idea-ext:org.jetbrains.gradle.plugin.idea-ext.gradle.plugin:1.3")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
    compilerOptions {
        freeCompilerArgs = listOf("-Werror")
    }
}

idea.module {
    isDownloadSources = true
    isDownloadJavadoc = true
}

gradlePlugin {
    plugins {
        create("hytale-mod") {
            id = "hytale-mod"
            implementationClass = "info.hytalemodding.gradle.hytalemod.HytaleModPlugin"
            displayName = "Hytale-Mod"
        }
    }
}

publishing {
    repositories {
        maven("https://maven.hytale-modding.info/releases") {
            name = "HytaleModdingReleases"
            credentials {
                username = providers.environmentVariable("HM_RELEASES_MAVEN_USER").orNull
                password = providers.environmentVariable("HM_RELEASES_MAVEN_TOKEN").orNull
            }
        }
    }
}
