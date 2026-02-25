package st.service.feature_disk_analyzer.impl.presentation.screen.results.mvi

import st.service.feature_disk_analyzer.impl.domain.entity.DirectoryEntryEntity
import st.service.feature_disk_analyzer.impl.domain.entity.LargeFileItemEntity
import st.service.feature_disk_analyzer.impl.domain.entity.PieSliceEntity

internal data class DiskAnalyzerResultsState(
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val canNavigateBack: Boolean = false,
    val canNavigateForward: Boolean = false,
    val canNavigateUp: Boolean = false,
    val titlePath: String = "",
    val breadcrumbs: List<String> = emptyList(),
    val topFiles: List<LargeFileItemEntity> = emptyList(),
    val currentEntries: List<DirectoryEntryEntity> = emptyList(),
    val pieSlices: List<PieSliceEntity> = emptyList(),
    val issuesCount: Int = 0,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
)
