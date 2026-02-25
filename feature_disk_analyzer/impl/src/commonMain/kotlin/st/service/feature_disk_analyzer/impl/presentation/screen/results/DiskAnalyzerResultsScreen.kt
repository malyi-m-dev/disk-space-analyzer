package st.service.feature_disk_analyzer.impl.presentation.screen.results

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import org.koin.compose.koinInject
import st.service.core_platform_windows.WindowsExplorerService
import st.service.feature_disk_analyzer.impl.presentation.screen.results.mvi.DiskAnalyzerResultsEvent
import st.service.feature_disk_analyzer.impl.presentation.screen.results.mvi.DiskAnalyzerResultsScreenModel
import st.service.feature_disk_analyzer.impl.presentation.screen.results.mvi.DiskAnalyzerResultsSideEffect
import st.service.feature_disk_analyzer.impl.presentation.screen.results.ui.DiskAnalyzerResultsContent

@Composable
internal fun DiskAnalyzerResultsScreen(
    onNavigateToSetup: () -> Unit,
) {
    val screenModel = koinInject<DiskAnalyzerResultsScreenModel>()
    val explorerService = koinInject<WindowsExplorerService>()
    val state = screenModel.state.collectAsState()

    LaunchedEffect(screenModel) {
        screenModel.dispatch(DiskAnalyzerResultsEvent.OnLoad)
    }

    LaunchedEffect(screenModel) {
        screenModel.sideEffect.collect { effect ->
            when (effect) {
                is DiskAnalyzerResultsSideEffect.OpenInExplorer -> explorerService.open(effect.path)
                DiskAnalyzerResultsSideEffect.NavigateToSetup -> onNavigateToSetup()
            }
        }
    }

    DisposableEffect(screenModel) {
        onDispose { screenModel.dispose() }
    }

    DiskAnalyzerResultsContent(
        state = state.value,
        onNavigateBackClicked = { screenModel.dispatch(DiskAnalyzerResultsEvent.OnNavigateBackClicked) },
        onNavigateForwardClicked = { screenModel.dispatch(DiskAnalyzerResultsEvent.OnNavigateForwardClicked) },
        onNavigateUpClicked = { screenModel.dispatch(DiskAnalyzerResultsEvent.OnNavigateUpClicked) },
        onOpenDirectory = { screenModel.dispatch(DiskAnalyzerResultsEvent.OnOpenDirectory(it)) },
        onBreadcrumbClicked = { screenModel.dispatch(DiskAnalyzerResultsEvent.OnBreadcrumbClicked(it)) },
        onFocusFileParent = { screenModel.dispatch(DiskAnalyzerResultsEvent.OnFocusFileParent(it)) },
        onOpenInExplorerClicked = { screenModel.dispatch(DiskAnalyzerResultsEvent.OnOpenInExplorerClicked(it)) },
        onDeleteClicked = { path, mode -> screenModel.dispatch(DiskAnalyzerResultsEvent.OnDeleteClicked(path, mode)) },
        onRescanClicked = { screenModel.dispatch(DiskAnalyzerResultsEvent.OnRescanClicked) },
        onBackToSetupClicked = { screenModel.dispatch(DiskAnalyzerResultsEvent.OnBackToSetupClicked) },
    )
}
