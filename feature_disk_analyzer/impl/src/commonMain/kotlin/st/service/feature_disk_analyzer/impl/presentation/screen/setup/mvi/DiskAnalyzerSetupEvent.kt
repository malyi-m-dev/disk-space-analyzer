package st.service.feature_disk_analyzer.impl.presentation.screen.setup.mvi

internal sealed interface DiskAnalyzerSetupEvent {
    data object OnLoad : DiskAnalyzerSetupEvent
    data class OnPathChanged(val value: String) : DiskAnalyzerSetupEvent
    data object OnPickFolderClicked : DiskAnalyzerSetupEvent
    data class OnFolderPicked(val path: String?) : DiskAnalyzerSetupEvent
    data class OnDriveToggled(val path: String) : DiskAnalyzerSetupEvent
    data object OnScanPathClicked : DiskAnalyzerSetupEvent
    data object OnScanSelectedDrivesClicked : DiskAnalyzerSetupEvent
    data object OnScanAllDisksClicked : DiskAnalyzerSetupEvent
}
