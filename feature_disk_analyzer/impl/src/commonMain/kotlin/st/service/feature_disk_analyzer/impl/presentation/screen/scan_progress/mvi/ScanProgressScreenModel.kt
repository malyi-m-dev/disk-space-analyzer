package st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.mvi

import kotlinx.coroutines.Job
import st.service.core_base_feature.BaseScreenModelV2
import st.service.feature_disk_analyzer.impl.domain.interactor.ScanFileSystemInteractor
import st.service.feature_disk_analyzer.impl.presentation.navigation.DiskAnalyzerSessionStore

internal class ScanProgressScreenModel(
    private val sessionStore: DiskAnalyzerSessionStore,
    private val scanFileSystemInteractor: ScanFileSystemInteractor,
) : BaseScreenModelV2<ScanProgressState, ScanProgressSideEffect, ScanProgressEvent>(
    initialState = ScanProgressState(),
) {
    private var scanJob: Job? = null

    override fun dispatch(event: ScanProgressEvent) {
        when (event) {
            ScanProgressEvent.OnStart -> onStart()
            ScanProgressEvent.OnCancelClicked -> onCancelClicked()
        }
    }

    private fun onStart() {
        if (currentState.isRunning || currentState.isCompleted) return
        val request = sessionStore.request.value ?: run {
            screenModelScope { postSideEffect(ScanProgressSideEffect.NavigateBack) }
            return
        }

        updateState { it.copy(isRunning = true, errorMessage = null) }
        scanJob = screenModelScope {
            runCatching {
                val snapshot = scanFileSystemInteractor.scan(request = request) { progress ->
                    sessionStore.setProgress(progress)
                    updateState {
                        it.copy(
                            currentPath = progress.currentPath,
                            filesScanned = progress.filesScanned,
                            directoriesScanned = progress.directoriesScanned,
                            bytesObserved = progress.bytesObserved,
                            issueCount = progress.issueCount,
                        )
                    }
                }
                sessionStore.setSnapshot(snapshot)
                updateState { it.copy(isRunning = false, isCompleted = true) }
                postSideEffect(ScanProgressSideEffect.NavigateToResults)
            }.onFailure { error ->
                updateState { it.copy(isRunning = false, errorMessage = error.message ?: "Ошибка сканирования") }
            }
        }
    }

    private fun onCancelClicked() {
        scanJob?.cancel()
        screenModelScope { postSideEffect(ScanProgressSideEffect.NavigateBack) }
    }
}
