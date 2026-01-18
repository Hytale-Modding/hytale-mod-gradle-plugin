package info.hytalemodding.gradle.hytalemod.decompile

import info.hytalemodding.gradle.hytalemod.HytaleExtension
import info.hytalemodding.gradle.hytalemod.util.hytale
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.dependencies

private val partialDecompilePrefixes: List<String> = listOf(
    "com.hypixel.fastutil",
    "com.hypixel.hytale"
)

fun Project.registerDecompileTask() {
    // TODO: register sources automatically
    //  for this to work we would need to register an afterSync task that modifies
    //  the iml file to add the dependency. I've given up on it for now.
//    val ideaModel = rootProject.extensions.ideaExt
//    ideaModel.project.settings.generateImlFiles = true

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
            buildList {
                if (project.extensions.hytale.decompilePartialOnly.get()) {
                    addAll(partialDecompilePrefixes.map { "--only=${it.replace('.', '/')}" })
                }

                add(serverJar.get().asFile.absolutePath)
                add(sourcesFile.get().absolutePath)
            }
        }

        inputs.file(serverJar)
        outputs.file(sourcesFile)
    }
}
