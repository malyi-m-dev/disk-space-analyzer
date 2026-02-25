package st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.mvi

internal data class ScanProgressState(
    val isRunning: Boolean = false,
    val currentPath: String? = null,
    val filesScanned: Long = 0,
    val directoriesScanned: Long = 0,
    val bytesObserved: Long = 0,
    val issueCount: Long = 0,
    val errorMessage: String? = null,
    val isCompleted: Boolean = false,
)
