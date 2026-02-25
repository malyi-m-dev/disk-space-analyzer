package st.service.feature_disk_analyzer.impl.presentation.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import st.service.data_filesystem.FileSystemScanProgress
import st.service.data_filesystem.FileSystemScanRequest
import st.service.data_filesystem.FileSystemScanSnapshot

class DiskAnalyzerSessionStore {
    private val _request = MutableStateFlow<FileSystemScanRequest?>(null)
    val request: StateFlow<FileSystemScanRequest?> = _request.asStateFlow()

    private val _snapshot = MutableStateFlow<FileSystemScanSnapshot?>(null)
    val snapshot: StateFlow<FileSystemScanSnapshot?> = _snapshot.asStateFlow()

    private val _progress = MutableStateFlow<FileSystemScanProgress?>(null)
    val progress: StateFlow<FileSystemScanProgress?> = _progress.asStateFlow()

    fun setRequest(value: FileSystemScanRequest) {
        _request.value = value
        _snapshot.value = null
        _progress.value = null
    }

    fun setProgress(value: FileSystemScanProgress) {
        _progress.value = value
    }

    fun setSnapshot(value: FileSystemScanSnapshot) {
        _snapshot.value = value
    }
}

object DiskAnalyzerRoutes {
    const val Setup = "setup"
    const val ScanProgress = "scan_progress"
    const val Results = "results"
}
