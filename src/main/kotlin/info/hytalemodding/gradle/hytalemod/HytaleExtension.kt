package info.hytalemodding.gradle.hytalemod

import net.harawata.appdirs.AppDirs
import net.harawata.appdirs.AppDirsFactory
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

const val defaultUpdateChannel = "release"

abstract class HytaleExtension @Inject constructor(factory: ProviderFactory, private val project: Project) {

    companion object {
        const val EXTENSION_NAME = "hytale"
        const val TASK_GROUP = "hytale"

        const val PROPERTY_INSTALL_DIR = "hytale.install_dir"
        const val PROPERTY_UPDATE_CHANNEL = "hytale.update_channel"
    }

    @Deprecated("use installDir instead!", ReplaceWith("installDir"))
    abstract val gameDir: Property<String>

    abstract val installDir: Property<String>

    abstract val assetsFile: RegularFileProperty

    abstract val serverDir: DirectoryProperty

    abstract val serverJar: RegularFileProperty

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
        updateChannel.convention(project.providers.gradleProperty(PROPERTY_UPDATE_CHANNEL).orElse(defaultUpdateChannel))

        // if hytale.install_dir is set, default to that;
        // else use the default install location
        @Suppress("DEPRECATION")
        gameDir.convention(project.providers.gradleProperty(PROPERTY_INSTALL_DIR).orElse(factory.provider {
            val appDirs: AppDirs = AppDirsFactory.getInstance()
            val dir =
                if (Os.isFamily(Os.FAMILY_WINDOWS) || Os.isFamily(Os.FAMILY_MAC)) {
                    appDirs.getUserDataDir("Hytale", null, null, true);
                }
                else { // linux is special and ships as flatpak
                    "${System.getProperty("user.home")}/.var/app/com.hypixel.HytaleLauncher/data/Hytale"
                }

            return@provider dir
        }))

        @Suppress("DEPRECATION")
        installDir.convention(gameDir)

        assetsFile.convention { project.file("${installDir.get()}/install/${updateChannel.get()}/package/game/latest/Assets.zip") }
        serverDir.convention(project.layout.dir(updateChannel.map { channel -> project.file("${installDir.get()}/install/${channel}/package/game/latest/Server") }))
        hytaleUserDir.convention(project.layout.dir(factory.provider { project.file("${installDir.get()}/UserData") }))
        serverJar.convention(serverDir.file("HytaleServer.jar"))

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
