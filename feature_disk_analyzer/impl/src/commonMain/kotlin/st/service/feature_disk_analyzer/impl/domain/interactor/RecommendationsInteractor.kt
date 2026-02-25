package st.service.feature_disk_analyzer.impl.domain.interactor

import st.service.feature_disk_analyzer.impl.domain.entity.RecommendationTagEntity

class RecommendationsInteractor {
    fun tagsFor(path: String, sizeBytes: Long, lastModifiedEpochMs: Long?): List<RecommendationTagEntity> {
        val normalized = path.lowercase()
        val tags = linkedSetOf<RecommendationTagEntity>()

        if ("\\temp\\" in normalized || normalized.endsWith("\\temp")) tags += RecommendationTagEntity.TEMP
        if ("cache" in normalized) tags += RecommendationTagEntity.CACHE
        if (normalized.endsWith(".log") || normalized.endsWith(".dmp")) tags += RecommendationTagEntity.LOG
        if (normalized.endsWith(".zip") || normalized.endsWith(".7z") || normalized.endsWith(".rar") || normalized.endsWith(".iso") || normalized.endsWith(".msi")) tags += RecommendationTagEntity.ARCHIVE
        if (normalized.endsWith(".mp4") || normalized.endsWith(".mkv") || normalized.endsWith(".mov") || normalized.endsWith(".vhd") || normalized.endsWith(".vhdx")) tags += RecommendationTagEntity.MEDIA

        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val ageDays = lastModifiedEpochMs?.let { (now - it) / 86_400_000L } ?: 0L
        if (sizeBytes >= 500L * 1024 * 1024 && ageDays >= 180) tags += RecommendationTagEntity.OLD

        return tags.toList()
    }
}
