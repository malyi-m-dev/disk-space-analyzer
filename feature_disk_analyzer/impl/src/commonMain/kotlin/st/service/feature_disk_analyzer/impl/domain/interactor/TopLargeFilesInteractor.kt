package st.service.feature_disk_analyzer.impl.domain.interactor

import st.service.data_filesystem.FileSystemNode
import st.service.data_filesystem.FileSystemScanSnapshot
import st.service.feature_disk_analyzer.impl.domain.entity.LargeFileItemEntity

class TopLargeFilesInteractor(
    private val recommendationsInteractor: RecommendationsInteractor,
) {
    fun getTop(snapshot: FileSystemScanSnapshot, limit: Int = 200): List<LargeFileItemEntity> {
        return snapshot.nodes.values
            .asSequence()
            .filterIsInstance<FileSystemNode.FileNode>()
            .sortedByDescending { it.sizeBytes }
            .take(limit)
            .map { node ->
                LargeFileItemEntity(
                    path = node.path,
                    name = node.name,
                    parentPath = node.parentPath,
                    sizeBytes = node.sizeBytes,
                    lastModifiedEpochMs = node.lastModifiedEpochMs,
                    tags = recommendationsInteractor.tagsFor(node.path, node.sizeBytes, node.lastModifiedEpochMs),
                )
            }
            .toList()
    }
}
