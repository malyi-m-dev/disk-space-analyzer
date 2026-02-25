package st.service.core_platform_windows

data class WindowsDriveInfo(
    val path: String,
    val label: String,
    val totalSpaceBytes: Long?,
    val freeSpaceBytes: Long?,
)

enum class WindowsDeleteMode {
    RECYCLE_BIN,
    PERMANENT,
}

data class WindowsDeleteItemResult(
    val path: String,
    val success: Boolean,
    val message: String? = null,
)

interface WindowsExplorerService {
    fun open(path: String)
}

interface WindowsPrivilegeService {
    fun canElevate(): Boolean
    fun relaunchAsAdmin(): Boolean
}

interface WindowsDriveService {
    fun getAvailableDrives(): List<WindowsDriveInfo>
}

interface WindowsFolderPickerService {
    fun pickDirectory(initialPath: String?): String?
}

interface WindowsDeleteService {
    suspend fun delete(
        paths: List<String>,
        mode: WindowsDeleteMode,
    ): List<WindowsDeleteItemResult>
}
