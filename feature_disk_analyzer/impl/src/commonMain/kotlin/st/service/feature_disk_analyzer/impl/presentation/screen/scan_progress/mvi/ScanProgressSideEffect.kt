package st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.mvi

internal sealed interface ScanProgressSideEffect {
    data object NavigateToResults : ScanProgressSideEffect
    data object NavigateBack : ScanProgressSideEffect
}
