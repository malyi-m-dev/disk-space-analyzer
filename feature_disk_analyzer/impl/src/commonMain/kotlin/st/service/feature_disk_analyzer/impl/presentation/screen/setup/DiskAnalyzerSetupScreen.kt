package st.service.feature_disk_analyzer.impl.presentation.screen.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import org.koin.compose.koinInject
import st.service.core_platform_windows.WindowsFolderPickerService
import st.service.feature_disk_analyzer.impl.presentation.screen.setup.mvi.DiskAnalyzerSetupEvent
import st.service.feature_disk_analyzer.impl.presentation.screen.setup.mvi.DiskAnalyzerSetupScreenModel
import st.service.feature_disk_analyzer.impl.presentation.screen.setup.mvi.DiskAnalyzerSetupSideEffect
import st.service.feature_disk_analyzer.impl.presentation.screen.setup.ui.DiskAnalyzerSetupContent

@Composable
internal fun DiskAnalyzerSetupScreen(
    onNavigateToProgress: () -> Unit,
) {
    val screenModel = koinInject<DiskAnalyzerSetupScreenModel>()
    val folderPickerService = koinInject<WindowsFolderPickerService>()
    val state = screenModel.state.collectAsState()

    LaunchedEffect(screenModel) {
        screenModel.dispatch(DiskAnalyzerSetupEvent.OnLoad)
    }

    LaunchedEffect(screenModel) {
        screenModel.sideEffect.collect { effect ->
            when (effect) {
                DiskAnalyzerSetupSideEffect.NavigateToProgress -> onNavigateToProgress()
                is DiskAnalyzerSetupSideEffect.OpenFolderPicker -> {
                    val path = folderPickerService.pickDirectory(effect.initialPath)
                    screenModel.dispatch(DiskAnalyzerSetupEvent.OnFolderPicked(path))
                }
            }
        }
    }

    DisposableEffect(screenModel) {
        onDispose { screenModel.dispose() }
    }

    DiskAnalyzerSetupContent(
        state = state.value,
        onPathChanged = { screenModel.dispatch(DiskAnalyzerSetupEvent.OnPathChanged(it)) },
        onPickFolderClicked = { screenModel.dispatch(DiskAnalyzerSetupEvent.OnPickFolderClicked) },
        onDriveToggled = { screenModel.dispatch(DiskAnalyzerSetupEvent.OnDriveToggled(it)) },
        onScanPathClicked = { screenModel.dispatch(DiskAnalyzerSetupEvent.OnScanPathClicked) },
        onScanSelectedDrivesClicked = { screenModel.dispatch(DiskAnalyzerSetupEvent.OnScanSelectedDrivesClicked) },
        onScanAllDisksClicked = { screenModel.dispatch(DiskAnalyzerSetupEvent.OnScanAllDisksClicked) },
    )
}
