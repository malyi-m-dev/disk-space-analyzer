package st.service.feature_disk_analyzer.impl.domain.interactor

import st.service.core_platform_windows.WindowsDeleteItemResult
import st.service.core_platform_windows.WindowsDeleteMode
import st.service.core_platform_windows.WindowsDeleteService

class DeleteEntriesInteractor(
    private val windowsDeleteService: WindowsDeleteService,
) {
    suspend fun delete(
        paths: List<String>,
        mode: WindowsDeleteMode,
    ): List<WindowsDeleteItemResult> {
        return windowsDeleteService.delete(paths = paths, mode = mode)
    }
}
