package st.service.feature_disk_analyzer.impl.di

import org.koin.dsl.module
import st.service.feature_disk_analyzer.DiskAnalyzerComponent
import st.service.feature_disk_analyzer.impl.domain.interactor.DirectoryBreakdownInteractor
import st.service.feature_disk_analyzer.impl.domain.interactor.DeleteEntriesInteractor
import st.service.feature_disk_analyzer.impl.domain.interactor.DriveDiscoveryInteractor
import st.service.feature_disk_analyzer.impl.domain.interactor.RecommendationsInteractor
import st.service.feature_disk_analyzer.impl.domain.interactor.ScanFileSystemInteractor
import st.service.feature_disk_analyzer.impl.domain.interactor.TopLargeFilesInteractor
import st.service.feature_disk_analyzer.impl.presentation.DiskAnalyzerComponentImpl
import st.service.feature_disk_analyzer.impl.presentation.navigation.DiskAnalyzerSessionStore
import st.service.feature_disk_analyzer.impl.presentation.screen.results.mvi.DiskAnalyzerResultsScreenModel
import st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.mvi.ScanProgressScreenModel
import st.service.feature_disk_analyzer.impl.presentation.screen.setup.mvi.DiskAnalyzerSetupScreenModel

class FeatureDiskAnalyzerModule {
    val module = module {
        factory<DiskAnalyzerComponent> { DiskAnalyzerComponentImpl() }

        single { DiskAnalyzerSessionStore() }
        single { RecommendationsInteractor() }
        single { DriveDiscoveryInteractor(windowsDriveService = get()) }
        single { DeleteEntriesInteractor(windowsDeleteService = get()) }
        single { ScanFileSystemInteractor(fileSystemScannerRepository = get()) }
        single { TopLargeFilesInteractor(recommendationsInteractor = get()) }
        single { DirectoryBreakdownInteractor(recommendationsInteractor = get()) }

        factory {
            DiskAnalyzerSetupScreenModel(
                sessionStore = get(),
                driveDiscoveryInteractor = get(),
            )
        }
        factory { ScanProgressScreenModel(sessionStore = get(), scanFileSystemInteractor = get()) }
        factory {
            DiskAnalyzerResultsScreenModel(
                sessionStore = get(),
                scanFileSystemInteractor = get(),
                deleteEntriesInteractor = get(),
                topLargeFilesInteractor = get(),
                directoryBreakdownInteractor = get(),
            )
        }
    }
}
