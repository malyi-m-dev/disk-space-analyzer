package st.service.feature_disk_analyzer.impl.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import st.service.feature_disk_analyzer.DiskAnalyzerComponent
import st.service.feature_disk_analyzer.impl.presentation.navigation.DiskAnalyzerRoutes
import st.service.feature_disk_analyzer.impl.presentation.screen.results.DiskAnalyzerResultsScreen
import st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.ScanProgressScreen
import st.service.feature_disk_analyzer.impl.presentation.screen.setup.DiskAnalyzerSetupScreen

class DiskAnalyzerComponentImpl : DiskAnalyzerComponent {
    @Composable
    override fun DiskAnalyzerScreen() {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = DiskAnalyzerRoutes.Setup,
        ) {
            composable(DiskAnalyzerRoutes.Setup) {
                DiskAnalyzerSetupScreen(
                    onNavigateToProgress = { navController.navigate(DiskAnalyzerRoutes.ScanProgress) }
                )
            }
            composable(DiskAnalyzerRoutes.ScanProgress) {
                ScanProgressScreen(
                    onNavigateToResults = {
                        navController.navigate(DiskAnalyzerRoutes.Results) {
                            popUpTo(DiskAnalyzerRoutes.Setup)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(DiskAnalyzerRoutes.Results) {
                DiskAnalyzerResultsScreen(
                    onNavigateToSetup = {
                        navController.navigate(DiskAnalyzerRoutes.Setup) {
                            popUpTo(DiskAnalyzerRoutes.Setup) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
