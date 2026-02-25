package st.service.feature_disk_analyzer.impl.presentation.screen.results.mvi

internal sealed interface DiskAnalyzerResultsSideEffect {
    data class OpenInExplorer(val path: String) : DiskAnalyzerResultsSideEffect
    data object NavigateToSetup : DiskAnalyzerResultsSideEffect
}
