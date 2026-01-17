package info.hytalemodding.gradle.hytalemod

import net.harawata.appdirs.AppDirs
import net.harawata.appdirs.AppDirsFactory
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.io.File
import javax.inject.Inject

const val defaultUpdateChannel = "release"

abstract class HytaleExtension @Inject constructor(factory: ProviderFactory, private val project: Project) {

    companion object {
        const val EXTENSION_NAME = "hytale"
        const val TASK_GROUP = "hytale"
    }

    abstract val gameDir: DirectoryProperty

    abstract val assetsFile: RegularFileProperty

    abstract val serverDir: DirectoryProperty

    val serverJar: Provider<RegularFile>
        get() = serverDir.file("HytaleServer.jar")

    abstract val hytaleUserDir: DirectoryProperty

    abstract val updateChannel: Property<String>

    abstract val runConfigName: Property<String>

    abstract val runDir: Property<String>

    abstract val syncTask: Property<Task>

    abstract val allowOp: Property<Boolean>

    abstract val disableSentry: Property<Boolean>

    abstract val disableFileWatcher: Property<Boolean>

    //TODO make enum
    /**
     * authenticated|offline|insecure
     */
    abstract val authMode: Property<String>

    abstract val programArgs: ListProperty<String>

    abstract val jvmArgs: ListProperty<String>

    abstract val addServerDependency: Property<Boolean>

    abstract val addAssetsDependency: Property<Boolean>

    init {
        updateChannel.convention(defaultUpdateChannel)

        gameDir.convention(project.layout.dir(factory.provider {
            // FIXME kinda a hack, figure out whether there's a better way to do this
            val appDirs: AppDirs = AppDirsFactory.getInstance()
            val dir =
            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                appDirs.getUserConfigDir("Hytale", null, null, true);
            }
            else if (Os.isFamily(Os.FAMILY_MAC)) {
                appDirs.getUserDataDir("Hytale", null, null, true);
            }
            else { // linux is special and ships as flatpak
                "${System.getProperty("user.home")}/.var/app/com.hypixel.HytaleLauncher/data/Hytale"
            }

            return@provider File(dir)
        }))
        assetsFile.convention(gameDir.file(updateChannel.map { channel -> "install/${channel}/package/game/latest/Assets.zip" }))
        serverDir.convention(gameDir.dir(updateChannel.map { channel -> "install/${channel}/package/game/latest/Server" }))
        hytaleUserDir.convention(gameDir.dir("UserData"))

        runConfigName.convention(factory.provider {
            var name = "HytaleServer"
            if (project != project.rootProject) {
                name += " (${project.name})"
            }

            name
        })

        runDir.convention(factory.provider { project.layout.projectDirectory.file("run").asFile.absolutePath })

        allowOp.convention(true)
        disableSentry.convention(true)
        disableFileWatcher.convention(false)
        authMode.convention("authenticated")

        addServerDependency.convention(true)
        addAssetsDependency.convention(false)
    }
}
