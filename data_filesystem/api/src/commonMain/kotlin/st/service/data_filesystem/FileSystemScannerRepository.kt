package st.service.data_filesystem

sealed interface FileSystemScanRoot {
    data class Directory(val path: String) : FileSystemScanRoot
    data object AllDrives : FileSystemScanRoot
}

data class FileSystemScanRequest(
    val roots: List<FileSystemScanRoot>,
    val includeHidden: Boolean = true,
    val skipSymlinks: Boolean = true,
)

data class FileSystemScanProgress(
    val currentPath: String?,
    val filesScanned: Long,
    val directoriesScanned: Long,
    val bytesObserved: Long,
    val issueCount: Long,
)

data class FileSystemScanIssue(
    val path: String,
    val kind: FileSystemScanIssueKind,
    val message: String? = null,
)

enum class FileSystemScanIssueKind {
    ACCESS_DENIED,
    IO_ERROR,
    CANCELLED,
}

sealed interface FileSystemNode {
    val path: String
    val name: String
    val parentPath: String?
    val sizeBytes: Long
    val lastModifiedEpochMs: Long?
    val isHidden: Boolean

    data class FileNode(
        override val path: String,
        override val name: String,
        override val parentPath: String?,
        override val sizeBytes: Long,
        override val lastModifiedEpochMs: Long?,
        override val isHidden: Boolean,
    ) : FileSystemNode

    data class DirectoryNode(
        override val path: String,
        override val name: String,
        override val parentPath: String?,
        override val sizeBytes: Long,
        override val lastModifiedEpochMs: Long?,
        override val isHidden: Boolean,
        val children: List<String>,
        val isPartial: Boolean,
    ) : FileSystemNode
}

data class FileSystemScanSnapshot(
    val roots: List<String>,
    val nodes: Map<String, FileSystemNode>,
    val issues: List<FileSystemScanIssue>,
    val filesScanned: Long,
    val directoriesScanned: Long,
    val bytesObserved: Long,
)

interface FileSystemScannerRepository {
    suspend fun scan(
        request: FileSystemScanRequest,
        onProgress: (FileSystemScanProgress) -> Unit,
    ): FileSystemScanSnapshot
}
