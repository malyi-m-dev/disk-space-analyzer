package st.service.feature_disk_analyzer.impl.domain.entity

enum class RecommendationTagEntity {
    CACHE,
    TEMP,
    LOG,
    ARCHIVE,
    MEDIA,
    OLD,
}

data class LargeFileItemEntity(
    val path: String,
    val name: String,
    val parentPath: String?,
    val sizeBytes: Long,
    val lastModifiedEpochMs: Long?,
    val tags: List<RecommendationTagEntity>,
)

data class DirectoryEntryEntity(
    val path: String,
    val name: String,
    val parentPath: String?,
    val sizeBytes: Long,
    val isDirectory: Boolean,
    val lastModifiedEpochMs: Long?,
    val tags: List<RecommendationTagEntity>,
)

data class PieSliceEntity(
    val label: String,
    val value: Long,
    val path: String?,
)

data class DirectoryBreakdownEntity(
    val currentPath: String,
    val breadcrumbs: List<String>,
    val entries: List<DirectoryEntryEntity>,
    val pieSlices: List<PieSliceEntity>,
)
