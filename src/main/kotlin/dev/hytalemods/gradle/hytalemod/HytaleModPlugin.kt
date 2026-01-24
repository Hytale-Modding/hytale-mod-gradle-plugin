package dev.hytalemods.gradle.hytalemod

import dev.hytalemods.gradle.hytalemod.decompile.registerDecompileTask
import dev.hytalemods.gradle.hytalemod.util.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.repositories
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

@Suppress("unused")
abstract class HytaleModPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply("java-library")
        target.pluginManager.apply("idea")
        target.pluginManager.apply("org.jetbrains.gradle.plugin.idea-ext")

        val ci = target.providers.environmentVariable("CI").map { it.toBoolean() }.orElse(false)

        with(target) {
            val hytaleExtension = extensions.create(HytaleExtension.EXTENSION_NAME, HytaleExtension::class)
            val ideaModel = rootProject.extensions.ideaExt

            hytaleExtension.beforeRunTask.orNull?.let { it: Task ->
                tasks.named<ProcessResources>("processResources").configure {
                    dependsOn(it)
                }
            }

            afterEvaluate {
                @Suppress("DEPRECATION")
                if(ci.get() && hytaleExtension.serverJarSource.get() == ServerJarSource.GAME_FILES) {
                    logger.warn("Running on CI and trying to get the server Jar from game files. This is most likely not going to work!")
                }

                @Suppress("DEPRECATION")
                when(hytaleExtension.serverJarSource.get()) {
                    ServerJarSource.MAVEN_FATJAR -> {
                        repositories {
                            withHytaleMaven(hytaleExtension.updateChannel.get())
                        }

                        dependencies {
                            "compileOnly"("com.hypixel.hytale:Server:${hytaleExtension.version.get()}")
                        }
                    }
                    ServerJarSource.MAVEN_SQUASHED -> {
                        repositories {
                            withCodeMCMaven()
                        }

                        dependencies {
                            "compileOnly"("com.hypixel.hytale:Server-squashed:${hytaleExtension.version.get()}")
                        }
                    }
                    ServerJarSource.GAME_FILES -> {
                        if(hytaleExtension.addServerDependency.get()) {
                            if(ci.get()) {
                                logger.warn("Running on CI and trying to get the server Jar from game files. This is most likely not going to work!")
                            }

                            dependencies {
                                "implementation"(files(hytaleExtension.serverJar))
                            }
                        }
                    }
                }

                if(hytaleExtension.addAssetsDependency.get()) {
                    dependencies {
                        "compileOnly"(files(hytaleExtension.assetsFile))
                    }
                }

                mkdir(hytaleExtension.runDir)

                // TODO make server properties configurable
                val programArgs = mutableListOf(
                    "--assets=${hytaleExtension.assetsFile.get().asFile.absolutePath}"
                )

                if(hytaleExtension.allowOp.get()) {
                    programArgs.add("--allow-op")
                }

                if(hytaleExtension.disableSentry.get()) {
                    programArgs.add("--disable-sentry")
                }

                if(hytaleExtension.disableFileWatcher.get()) {
                    programArgs.add("--disable-file-watcher")
                }

                hytaleExtension.authMode.orNull?.let {
                    programArgs.add("--auth-mode=${it}")
                }

                hytaleExtension.programArgs.orNull?.let { programArgs.addAll(it) }

                val aotFile = hytaleExtension.serverDir.file("HytaleServer.aot").get().asFile
                val aotArg = if (aotFile.exists()) "-XX:AOTCache=${aotFile.absolutePath}" else ""

                val javaArgs = mutableListOf(aotArg)
                hytaleExtension.jvmArgs.orNull?.let { javaArgs.addAll(it) }
                javaArgs.add("--enable-native-access=ALL-UNNAMED")

                // FIXME IDEA bug: need to somehow get the run configs to *run* with the project's JDK not the root project's JDK version.
//    val projectModuleName = if (project == rootProject) {
//        "${ideaModel.project.name}.main"
//    } else {
//        "${rootProject.name}.${project.name}.main"
//    }
//
//    ideaModel.project.settings {
//        runConfigurations {
//            create<Application>(hytaleExtension.runConfigName.get()) {
//                mainClass = "com.hypixel.hytale.Main"
//                moduleName = projectModuleName
//                programParameters = programArgs.joinToString(" ")
//                jvmArgs = javaArgs.joinToString(" ")
//                workingDirectory = hytaleExtension.runDir.get()
//
//                beforeRun {
//                    mkdir(hytaleExtension.runDir)
//                }
//
//                hytaleExtension.syncTask.orNull?.let {
//                    beforeRun.register<GradleTask>("prepareTask") {
//                        task = it
//                    }
//                }
//            }
//        }
//    }

                val runTask = tasks.register("runServer", JavaExec::class.java) {
                    group = HytaleExtension.TASK_GROUP

                    mainClass.set("com.hypixel.hytale.Main")
                    modularity.inferModulePath.set(true)
                    classpath = sourceSets.main.runtimeClasspath
                    args = programArgs
                    jvmArgs = javaArgs
                    standardInput = System.`in`

                    workingDir(hytaleExtension.runDir)

                    hytaleExtension.beforeRunTask.orNull?.let {
                        dependsOn(it)
                    }
                }

                // Task#path but we cant access that because it's a TaskProvider
                val taskPath = buildString {
                    if (project.path != rootProject.path) {
                        append(project.path)
                    }

                    append(":${runTask.name}")
                }

                ideaModel.project.settings {
                    runConfigurations {
                        create<Gradle>(hytaleExtension.runConfigName.get()) {
                            taskNames = listOf(taskPath)
                        }
                    }
                }
            }

            project.registerDecompileTask()
        }
    }
}
