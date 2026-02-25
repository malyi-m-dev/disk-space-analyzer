package st.service.app_desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import org.koin.dsl.module
import st.service.core_platform_windows.WindowsExplorerService
import st.service.core_platform_windows.WindowsExplorerServiceImpl
import st.service.core_platform_windows.WindowsDeleteService
import st.service.core_platform_windows.WindowsDeleteServiceImpl
import st.service.core_platform_windows.WindowsDriveService
import st.service.core_platform_windows.WindowsDriveServiceImpl
import st.service.core_platform_windows.WindowsFolderPickerService
import st.service.core_platform_windows.WindowsFolderPickerServiceImpl
import st.service.core_platform_windows.WindowsPrivilegeService
import st.service.core_platform_windows.WindowsPrivilegeServiceImpl
import st.service.data_filesystem.FileSystemScannerRepository
import st.service.data_filesystem.impl.JvmFileSystemScannerRepository
import st.service.feature_disk_analyzer.DiskAnalyzerComponent
import st.service.feature_disk_analyzer.impl.di.FeatureDiskAnalyzerModule

fun main() {
    val featureModule = FeatureDiskAnalyzerModule()
    startKoin {
        modules(
            module {
                single<FileSystemScannerRepository> { JvmFileSystemScannerRepository() }
                single<WindowsExplorerService> { WindowsExplorerServiceImpl() }
                single<WindowsDriveService> { WindowsDriveServiceImpl() }
                single<WindowsFolderPickerService> { WindowsFolderPickerServiceImpl() }
                single<WindowsDeleteService> { WindowsDeleteServiceImpl() }
                single<WindowsPrivilegeService> { WindowsPrivilegeServiceImpl() }
            },
            featureModule.module,
        )
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Disk Space Analyzer",
        ) {
            MaterialTheme {
                App()
            }
        }
    }
}

@Composable
private fun App() {
    val component = org.koin.compose.koinInject<DiskAnalyzerComponent>()
    component.DiskAnalyzerScreen()
}
