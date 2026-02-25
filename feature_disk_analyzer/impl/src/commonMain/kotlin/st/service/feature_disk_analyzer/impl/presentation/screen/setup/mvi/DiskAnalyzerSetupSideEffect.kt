package st.service.feature_disk_analyzer.impl.presentation.screen.setup.mvi

internal sealed interface DiskAnalyzerSetupSideEffect {
    data object NavigateToProgress : DiskAnalyzerSetupSideEffect
    data class OpenFolderPicker(val initialPath: String?) : DiskAnalyzerSetupSideEffect
}
