package st.service.feature_disk_analyzer.impl.presentation.screen.setup.mvi

internal data class DiskAnalyzerSetupDriveItemState(
    val path: String,
    val label: String,
    val isSelected: Boolean,
    val totalSpaceBytes: Long?,
    val freeSpaceBytes: Long?,
)

internal data class DiskAnalyzerSetupState(
    val path: String = "C:\\Users",
    val isValid: Boolean = true,
    val message: String? = null,
    val drives: List<DiskAnalyzerSetupDriveItemState> = emptyList(),
)
