package info.hytalemodding.gradle.hytalemod

import info.hytalemodding.gradle.hytalemod.util.hytale
import info.hytalemodding.gradle.hytalemod.util.ideaExt
import info.hytalemodding.gradle.hytalemod.util.main
import info.hytalemodding.gradle.hytalemod.util.sourceSets
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.*
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

        target.extensions.create(HytaleExtension.EXTENSION_NAME, HytaleExtension::class)

        target.afterEvaluate {
            val ideaModel = rootProject.extensions.ideaExt

            val hytaleExtension = extensions.hytale

            repositories {
                maven("https://maven.hytale-modding.info/releases") {
                    name = "HytaleModdingReleases"
                    // FIXME remove once maven becomes public
                    credentials {
                        username = "tmp_viewer"
                        password = "01KDD2ARZ795FTWBW0A97RP5RS"
                    }
                }
            }

            dependencies {
                // TODO should we make this configurable?
                "implementation"(files("${hytaleExtension.serverDir.get()}/HytaleServer.jar"))
            }

            hytaleExtension.syncTask.orNull?.let { it: Task ->
                tasks.named<ProcessResources>("processResources").configure {
                    dependsOn(it)
                }
            }

            mkdir(hytaleExtension.runDir)

            // TODO make server properties configurable
            val programArgs = mutableListOf(
                "--assets=${hytaleExtension.assetsDir.get()}"
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

            val aotFile = project.file("${hytaleExtension.serverDir.get()}/HytaleServer.aot")
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
                mainClass.set("com.hypixel.hytale.Main")
                modularity.inferModulePath.set(true)
                classpath = sourceSets.main.runtimeClasspath
                args = programArgs
                jvmArgs = javaArgs
                standardInput = System.`in`

                workingDir(hytaleExtension.runDir)

                hytaleExtension.syncTask.orNull?.let {
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
    }
}
