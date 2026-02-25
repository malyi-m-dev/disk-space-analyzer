package st.service.feature_disk_analyzer.impl.domain.interactor

import st.service.data_filesystem.FileSystemNode
import st.service.data_filesystem.FileSystemScanSnapshot
import st.service.feature_disk_analyzer.impl.domain.entity.DirectoryBreakdownEntity
import st.service.feature_disk_analyzer.impl.domain.entity.DirectoryEntryEntity
import st.service.feature_disk_analyzer.impl.domain.entity.PieSliceEntity

class DirectoryBreakdownInteractor(
    private val recommendationsInteractor: RecommendationsInteractor,
) {
    fun build(snapshot: FileSystemScanSnapshot, currentPath: String, pieLimit: Int = 10): DirectoryBreakdownEntity {
        val currentNode = snapshot.nodes[currentPath] as? FileSystemNode.DirectoryNode
            ?: error("Directory not found: $currentPath")

        val entries = currentNode.children.mapNotNull { childPath ->
            when (val child = snapshot.nodes[childPath]) {
                is FileSystemNode.DirectoryNode -> DirectoryEntryEntity(
                    path = child.path,
                    name = child.name,
                    parentPath = child.parentPath,
                    sizeBytes = child.sizeBytes,
                    isDirectory = true,
                    lastModifiedEpochMs = child.lastModifiedEpochMs,
                    tags = recommendationsInteractor.tagsFor(child.path, child.sizeBytes, child.lastModifiedEpochMs),
                )
                is FileSystemNode.FileNode -> DirectoryEntryEntity(
                    path = child.path,
                    name = child.name,
                    parentPath = child.parentPath,
                    sizeBytes = child.sizeBytes,
                    isDirectory = false,
                    lastModifiedEpochMs = child.lastModifiedEpochMs,
                    tags = recommendationsInteractor.tagsFor(child.path, child.sizeBytes, child.lastModifiedEpochMs),
                )
                else -> null
            }
        }.sortedByDescending { it.sizeBytes }

        val slices = entries.take(pieLimit).map { PieSliceEntity(it.name, it.sizeBytes, it.path) }.toMutableList()
        val otherSize = entries.drop(pieLimit).sumOf { it.sizeBytes }
        if (otherSize > 0) slices += PieSliceEntity("Other", otherSize, null)

        return DirectoryBreakdownEntity(
            currentPath = currentPath,
            breadcrumbs = buildBreadcrumbs(snapshot, currentPath),
            entries = entries,
            pieSlices = slices,
        )
    }

    private fun buildBreadcrumbs(snapshot: FileSystemScanSnapshot, currentPath: String): List<String> {
        val result = mutableListOf<String>()
        var cursor: String? = currentPath
        while (cursor != null) {
            result += cursor
            cursor = snapshot.nodes[cursor]?.parentPath
        }
        return result.asReversed()
    }
}
