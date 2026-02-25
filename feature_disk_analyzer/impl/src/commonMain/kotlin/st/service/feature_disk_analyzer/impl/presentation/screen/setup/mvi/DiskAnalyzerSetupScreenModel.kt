package st.service.feature_disk_analyzer.impl.presentation.screen.setup.mvi

import st.service.core_base_feature.BaseScreenModelV2
import st.service.data_filesystem.FileSystemScanRequest
import st.service.data_filesystem.FileSystemScanRoot
import st.service.feature_disk_analyzer.impl.domain.interactor.DriveDiscoveryInteractor
import st.service.feature_disk_analyzer.impl.presentation.navigation.DiskAnalyzerSessionStore

internal class DiskAnalyzerSetupScreenModel(
    private val sessionStore: DiskAnalyzerSessionStore,
    private val driveDiscoveryInteractor: DriveDiscoveryInteractor,
) : BaseScreenModelV2<DiskAnalyzerSetupState, DiskAnalyzerSetupSideEffect, DiskAnalyzerSetupEvent>(
    initialState = DiskAnalyzerSetupState(),
) {
    private var isLoaded = false

    override fun dispatch(event: DiskAnalyzerSetupEvent) {
        when (event) {
            DiskAnalyzerSetupEvent.OnLoad -> onLoad()
            is DiskAnalyzerSetupEvent.OnPathChanged -> updateState { it.copy(path = event.value, isValid = true, message = null) }
            DiskAnalyzerSetupEvent.OnPickFolderClicked -> screenModelScope {
                postSideEffect(DiskAnalyzerSetupSideEffect.OpenFolderPicker(currentState.path))
            }
            is DiskAnalyzerSetupEvent.OnFolderPicked -> onFolderPicked(event.path)
            is DiskAnalyzerSetupEvent.OnDriveToggled -> onDriveToggled(event.path)
            DiskAnalyzerSetupEvent.OnScanPathClicked -> onScanPathClicked()
            DiskAnalyzerSetupEvent.OnScanSelectedDrivesClicked -> onScanSelectedDrivesClicked()
            DiskAnalyzerSetupEvent.OnScanAllDisksClicked -> onScanAllDisksClicked()
        }
    }

    private fun onLoad() {
        if (isLoaded) return
        isLoaded = true
        val drives = driveDiscoveryInteractor.getAvailableDrives().map { drive ->
            DiskAnalyzerSetupDriveItemState(
                path = drive.path,
                label = drive.label,
                isSelected = false,
                totalSpaceBytes = drive.totalSpaceBytes,
                freeSpaceBytes = drive.freeSpaceBytes,
            )
        }
        updateState { it.copy(drives = drives) }
    }

    private fun onFolderPicked(path: String?) {
        if (path == null) return
        updateState { it.copy(path = path, isValid = true, message = null) }
    }

    private fun onDriveToggled(path: String) {
        updateState { state ->
            state.copy(
                drives = state.drives.map { item ->
                    if (item.path == path) item.copy(isSelected = !item.isSelected) else item
                }
            )
        }
    }

    private fun onScanPathClicked() {
        val path = currentState.path.trim()
        if (path.isBlank()) {
            updateState { it.copy(isValid = false, message = "Укажите путь для сканирования") }
            return
        }
        screenModelScope {
            sessionStore.setRequest(FileSystemScanRequest(roots = listOf(FileSystemScanRoot.Directory(path))))
            postSideEffect(DiskAnalyzerSetupSideEffect.NavigateToProgress)
        }
    }

    private fun onScanSelectedDrivesClicked() {
        val selected = currentState.drives.filter { it.isSelected }
        if (selected.isEmpty()) {
            updateState { it.copy(message = "Выберите хотя бы один диск") }
            return
        }
        screenModelScope {
            sessionStore.setRequest(
                FileSystemScanRequest(
                    roots = selected.map { FileSystemScanRoot.Directory(it.path) },
                )
            )
            postSideEffect(DiskAnalyzerSetupSideEffect.NavigateToProgress)
        }
    }

    private fun onScanAllDisksClicked() {
        screenModelScope {
            sessionStore.setRequest(FileSystemScanRequest(roots = listOf(FileSystemScanRoot.AllDrives)))
            postSideEffect(DiskAnalyzerSetupSideEffect.NavigateToProgress)
        }
    }
}
