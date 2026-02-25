package st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import org.koin.compose.koinInject
import st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.mvi.ScanProgressEvent
import st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.mvi.ScanProgressScreenModel
import st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.mvi.ScanProgressSideEffect
import st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.ui.ScanProgressContent

@Composable
internal fun ScanProgressScreen(
    onNavigateToResults: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val screenModel = koinInject<ScanProgressScreenModel>()
    val state = screenModel.state.collectAsState()

    LaunchedEffect(screenModel) {
        screenModel.dispatch(ScanProgressEvent.OnStart)
    }

    LaunchedEffect(screenModel) {
        screenModel.sideEffect.collect { effect ->
            when (effect) {
                ScanProgressSideEffect.NavigateBack -> onNavigateBack()
                ScanProgressSideEffect.NavigateToResults -> onNavigateToResults()
            }
        }
    }

    DisposableEffect(screenModel) {
        onDispose { screenModel.dispose() }
    }

    ScanProgressContent(
        state = state.value,
        onCancelClicked = { screenModel.dispatch(ScanProgressEvent.OnCancelClicked) },
    )
}
