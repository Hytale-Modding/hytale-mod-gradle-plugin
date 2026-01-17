package info.hytalemodding.gradle.hytalemod.decompile

import info.hytalemodding.gradle.hytalemod.HytaleExtension
import info.hytalemodding.gradle.hytalemod.util.hytale
import info.hytalemodding.gradle.hytalemod.util.ideaExt
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.gradle.ext.settings

fun Project.registerDecompileTask() {
    val ideaModel = rootProject.extensions.ideaExt
    ideaModel.project.settings.generateImlFiles = true

    val vf = configurations.create("vineFlowerDependencies") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }

    dependencies {
        "vineFlowerDependencies"("org.vineflower:vineflower:1.11.2")
    }

    tasks.register("decompileServer", JavaExec::class.java) {
        group = HytaleExtension.TASK_GROUP

        val serverJar = project.extensions.hytale.serverJar
        val sourcesFile = serverJar.map { it.asFile.parentFile.resolve("${it.asFile.nameWithoutExtension}-sources.jar") }

        workingDir = temporaryDir
        mainClass.set("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler")
        classpath = vf

        argumentProviders.add {
            listOf(
                serverJar.get().asFile.absolutePath,
                sourcesFile.get().absolutePath
            )
        }

        inputs.file(serverJar)
        outputs.file(sourcesFile)
    }
}
