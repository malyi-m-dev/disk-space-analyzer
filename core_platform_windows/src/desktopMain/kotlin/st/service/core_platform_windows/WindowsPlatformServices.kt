package st.service.core_platform_windows

import com.sun.jna.platform.FileUtils
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

class WindowsExplorerServiceImpl : WindowsExplorerService {
    override fun open(path: String) {
        val target = File(path)
        val openPath = if (target.isFile) target.parentFile?.absolutePath ?: path else path
        ProcessBuilder("explorer.exe", openPath).start()
    }
}

class WindowsPrivilegeServiceImpl : WindowsPrivilegeService {
    override fun canElevate(): Boolean = true
    override fun relaunchAsAdmin(): Boolean = false
}

class WindowsDriveServiceImpl : WindowsDriveService {
    override fun getAvailableDrives(): List<WindowsDriveInfo> {
        return File.listRoots()
            .map { root ->
                WindowsDriveInfo(
                    path = root.absolutePath,
                    label = root.absolutePath,
                    totalSpaceBytes = root.totalSpace.takeIf { it > 0L },
                    freeSpaceBytes = root.freeSpace.takeIf { it >= 0L },
                )
            }
            .sortedBy { it.path }
    }
}

class WindowsFolderPickerServiceImpl : WindowsFolderPickerService {
    override fun pickDirectory(initialPath: String?): String? {
        var result: String? = null
        val action = Runnable {
            val chooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                isAcceptAllFileFilterUsed = false
                dialogTitle = "Выбор папки для сканирования"
                initialPath
                    ?.takeIf { it.isNotBlank() }
                    ?.let(::File)
                    ?.takeIf { it.exists() }
                    ?.let { currentDirectory = it }
            }
            val status = chooser.showOpenDialog(null)
            if (status == JFileChooser.APPROVE_OPTION) {
                result = chooser.selectedFile?.absolutePath
            }
        }
        if (SwingUtilities.isEventDispatchThread()) {
            action.run()
        } else {
            SwingUtilities.invokeAndWait(action)
        }
        return result
    }
}

class WindowsDeleteServiceImpl : WindowsDeleteService {
    override suspend fun delete(
        paths: List<String>,
        mode: WindowsDeleteMode,
    ): List<WindowsDeleteItemResult> {
        return when (mode) {
            WindowsDeleteMode.RECYCLE_BIN -> recycle(paths)
            WindowsDeleteMode.PERMANENT -> permanent(paths)
        }
    }

    private fun recycle(paths: List<String>): List<WindowsDeleteItemResult> {
        if (paths.isEmpty()) return emptyList()
        return try {
            FileUtils.getInstance().moveToTrash(*paths.map(::File).toTypedArray())
            paths.map { WindowsDeleteItemResult(path = it, success = true) }
        } catch (error: Exception) {
            paths.map {
                WindowsDeleteItemResult(
                    path = it,
                    success = false,
                    message = error.message ?: "Recycle Bin delete failed",
                )
            }
        }
    }

    private fun permanent(paths: List<String>): List<WindowsDeleteItemResult> {
        return paths.map { path ->
            runCatching {
                deleteRecursively(File(path))
                WindowsDeleteItemResult(path = path, success = true)
            }.getOrElse { error ->
                WindowsDeleteItemResult(
                    path = path,
                    success = false,
                    message = error.message ?: "Delete failed",
                )
            }
        }
    }

    private fun deleteRecursively(file: File) {
        if (!file.exists()) return
        if (file.isDirectory) {
            file.listFiles()?.forEach(::deleteRecursively)
        }
        if (!file.delete()) {
            error("Cannot delete ${file.absolutePath}")
        }
    }
}
