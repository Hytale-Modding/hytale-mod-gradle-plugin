plugins {
    idea
    `kotlin-dsl`
    `maven-publish`
}

group = "dev.hytalemods"
version = providers.environmentVariable("PLUGIN_VERSION").orNull ?: "0.0.0-development"

// Kotlin does not support building with Java 25 yet.
val javaVersion = 21

val pluginId: String by project
val pluginDisplayName: String by project
val pluginMainClass: String by project
val pluginVcsUrl: String by project
val pluginWebsite: String by project

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    implementation("net.harawata:appdirs:1.5.0")
    implementation("org.jetbrains.gradle.plugin.idea-ext:org.jetbrains.gradle.plugin.idea-ext.gradle.plugin:1.4.1")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
    compilerOptions {
        freeCompilerArgs = listOf(
            "-Werror",
            "-java-parameters",
            "-Xjvm-default=all",
            "-Xsam-conversions=class",
            "-Xjsr305=strict",
            "-Xjspecify-annotations=strict"
        )
    }
}

idea.module {
    isDownloadSources = true
    isDownloadJavadoc = true
}

gradlePlugin {
    plugins {
        create("hytale-mod", closureOf<PluginDeclaration> {
            id = pluginId
            displayName = pluginDisplayName
            implementationClass = pluginMainClass
            vcsUrl = pluginVcsUrl
            website = pluginWebsite
            // TODO description, tags
        })
    }
}

publishing {
    repositories {
        maven("https://maven.hytale-mods.dev/releases") {
            name = "HytaleModdingReleases"
            credentials {
                username = providers.environmentVariable("HM_RELEASES_MAVEN_USER").orNull
                password = providers.environmentVariable("HM_RELEASES_MAVEN_TOKEN").orNull
            }
        }
    }
}
