package info.hytalemodding.gradle.hytalemod.decompile

import info.hytalemodding.gradle.hytalemod.HytaleExtension
import info.hytalemodding.gradle.hytalemod.util.hytale
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.dependencies

fun Project.registerDecompileTask() {

    val vf = configurations.create("vineFlowerDependencies") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }

    dependencies {
        "vineFlowerDependencies"("org.vineflower:vineflower:1.11.2")
    }

    afterEvaluate {
        val hytaleExtension = project.extensions.hytale

        val decompileServerTask = tasks.register("decompileServer", JavaExec::class.java) {
            group = HytaleExtension.TASK_GROUP

            val serverJarPath = hytaleExtension.serverJar
            val sourcesFilePath = hytaleExtension.serverDir.map { "${it}/HytaleServer-src.zip" }

            workingDir = temporaryDir
            mainClass.set("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler")
            classpath = vf

            args = listOf(
                file(serverJarPath).absolutePath,
                file(sourcesFilePath).absolutePath
            )

            inputs.file(serverJarPath)
            outputs.file(sourcesFilePath)
        }
    }
}
