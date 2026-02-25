package st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.mvi

internal sealed interface ScanProgressEvent {
    data object OnStart : ScanProgressEvent
    data object OnCancelClicked : ScanProgressEvent
}
