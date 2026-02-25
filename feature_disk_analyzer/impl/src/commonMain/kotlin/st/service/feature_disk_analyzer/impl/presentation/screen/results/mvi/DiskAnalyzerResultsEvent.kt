package st.service.feature_disk_analyzer.impl.presentation.screen.results.mvi

import st.service.core_platform_windows.WindowsDeleteMode

internal sealed interface DiskAnalyzerResultsEvent {
    data object OnLoad : DiskAnalyzerResultsEvent
    data object OnRescanClicked : DiskAnalyzerResultsEvent
    data object OnNavigateBackClicked : DiskAnalyzerResultsEvent
    data object OnNavigateForwardClicked : DiskAnalyzerResultsEvent
    data object OnNavigateUpClicked : DiskAnalyzerResultsEvent
    data class OnOpenDirectory(val path: String) : DiskAnalyzerResultsEvent
    data class OnBreadcrumbClicked(val path: String) : DiskAnalyzerResultsEvent
    data class OnOpenInExplorerClicked(val path: String) : DiskAnalyzerResultsEvent
    data class OnFocusFileParent(val path: String) : DiskAnalyzerResultsEvent
    data class OnDeleteClicked(val path: String, val mode: WindowsDeleteMode) : DiskAnalyzerResultsEvent
    data object OnBackToSetupClicked : DiskAnalyzerResultsEvent
}
