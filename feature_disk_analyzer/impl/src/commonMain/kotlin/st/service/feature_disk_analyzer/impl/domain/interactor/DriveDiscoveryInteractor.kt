package st.service.feature_disk_analyzer.impl.domain.interactor

import st.service.core_platform_windows.WindowsDriveInfo
import st.service.core_platform_windows.WindowsDriveService

class DriveDiscoveryInteractor(
    private val windowsDriveService: WindowsDriveService,
) {
    fun getAvailableDrives(): List<WindowsDriveInfo> {
        return windowsDriveService.getAvailableDrives()
    }
}
