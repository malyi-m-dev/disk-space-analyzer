package st.service.feature_disk_analyzer.impl.presentation.screen.results.mvi

import st.service.core_base_feature.BaseScreenModelV2
import st.service.core_platform_windows.WindowsDeleteMode
import st.service.data_filesystem.FileSystemNode
import st.service.data_filesystem.FileSystemScanSnapshot
import st.service.feature_disk_analyzer.impl.domain.interactor.DeleteEntriesInteractor
import st.service.feature_disk_analyzer.impl.domain.interactor.DirectoryBreakdownInteractor
import st.service.feature_disk_analyzer.impl.domain.interactor.ScanFileSystemInteractor
import st.service.feature_disk_analyzer.impl.domain.interactor.TopLargeFilesInteractor
import st.service.feature_disk_analyzer.impl.presentation.navigation.DiskAnalyzerSessionStore

internal class DiskAnalyzerResultsScreenModel(
    private val sessionStore: DiskAnalyzerSessionStore,
    private val scanFileSystemInteractor: ScanFileSystemInteractor,
    private val deleteEntriesInteractor: DeleteEntriesInteractor,
    private val topLargeFilesInteractor: TopLargeFilesInteractor,
    private val directoryBreakdownInteractor: DirectoryBreakdownInteractor,
) : BaseScreenModelV2<DiskAnalyzerResultsState, DiskAnalyzerResultsSideEffect, DiskAnalyzerResultsEvent>(
    initialState = DiskAnalyzerResultsState(),
) {
    private var loaded = false
    private val directoryHistory = mutableListOf<String>()
    private var directoryHistoryIndex = -1

    override fun dispatch(event: DiskAnalyzerResultsEvent) {
        when (event) {
            DiskAnalyzerResultsEvent.OnLoad -> onLoad()
            DiskAnalyzerResultsEvent.OnRescanClicked -> onRescanClicked()
            DiskAnalyzerResultsEvent.OnNavigateBackClicked -> onNavigateBackClicked()
            DiskAnalyzerResultsEvent.OnNavigateForwardClicked -> onNavigateForwardClicked()
            DiskAnalyzerResultsEvent.OnNavigateUpClicked -> onNavigateUpClicked()
            is DiskAnalyzerResultsEvent.OnOpenDirectory -> showDirectory(event.path, trackHistory = true)
            is DiskAnalyzerResultsEvent.OnBreadcrumbClicked -> showDirectory(event.path, trackHistory = true)
            is DiskAnalyzerResultsEvent.OnOpenInExplorerClicked -> screenModelScope {
                postSideEffect(DiskAnalyzerResultsSideEffect.OpenInExplorer(event.path))
            }
            is DiskAnalyzerResultsEvent.OnFocusFileParent -> onFocusFileParent(event.path)
            is DiskAnalyzerResultsEvent.OnDeleteClicked -> onDeleteClicked(event.path, event.mode)
            DiskAnalyzerResultsEvent.OnBackToSetupClicked -> screenModelScope {
                postSideEffect(DiskAnalyzerResultsSideEffect.NavigateToSetup)
            }
        }
    }

    private fun onLoad() {
        if (loaded) return
        loaded = true
        renderFromCurrentSnapshot(preferredPath = null, historyMode = HistoryMode.Reset)
    }

    private fun onDeleteClicked(path: String, mode: WindowsDeleteMode) {
        screenModelScope {
            updateState { it.copy(isBusy = true, statusMessage = "Удаление...", errorMessage = null) }
            val result = deleteEntriesInteractor.delete(paths = listOf(path), mode = mode)
            val failed = result.firstOrNull { !it.success }
            if (failed != null) {
                updateState {
                    it.copy(
                        isBusy = false,
                        errorMessage = failed.message ?: "Не удалось удалить ${failed.path}",
                        statusMessage = null,
                    )
                }
                return@screenModelScope
            }

            runCatching {
                val updated = applyLocalDeletion(path)
                if (updated) {
                    renderFromCurrentSnapshot(preferredPath = currentState.titlePath, historyMode = HistoryMode.KeepCurrent)
                }
                updateState {
                    it.copy(
                        isBusy = false,
                        statusMessage = when (mode) {
                            WindowsDeleteMode.RECYCLE_BIN -> "Файл/папка перемещены в корзину"
                            WindowsDeleteMode.PERMANENT -> "Файл/папка удалены навсегда"
                        },
                    )
                }
            }.onFailure { error ->
                updateState {
                    it.copy(
                        isBusy = false,
                        errorMessage = error.message ?: "Ошибка обновления результата",
                        statusMessage = null,
                    )
                }
            }
        }
    }

    private fun onRescanClicked() {
        val request = sessionStore.request.value ?: return
        val preferredPath = currentState.titlePath.takeIf { it.isNotBlank() }
        screenModelScope {
            updateState {
                it.copy(
                    isBusy = true,
                    errorMessage = null,
                    statusMessage = "Ресканирование...",
                )
            }
            runCatching {
                val snapshot = scanFileSystemInteractor.scan(request) { progress ->
                    sessionStore.setProgress(progress)
                }
                sessionStore.setSnapshot(snapshot)
                renderFromCurrentSnapshot(preferredPath = preferredPath, historyMode = HistoryMode.KeepCurrent)
                updateState {
                    it.copy(
                        isBusy = false,
                        statusMessage = "Рескан завершен",
                    )
                }
            }.onFailure { error ->
                updateState {
                    it.copy(
                        isBusy = false,
                        errorMessage = error.message ?: "Ошибка рескана",
                        statusMessage = null,
                    )
                }
            }
        }
    }

    private fun onNavigateBackClicked() {
        val target = if (directoryHistoryIndex > 0) directoryHistory[directoryHistoryIndex - 1] else null
        target?.let {
            directoryHistoryIndex -= 1
            showDirectory(it, trackHistory = false)
        }
    }

    private fun onNavigateForwardClicked() {
        val target = if (directoryHistoryIndex >= 0 && directoryHistoryIndex < directoryHistory.lastIndex) {
            directoryHistory[directoryHistoryIndex + 1]
        } else {
            null
        }
        target?.let {
            directoryHistoryIndex += 1
            showDirectory(it, trackHistory = false)
        }
    }

    private fun onNavigateUpClicked() {
        val snapshot = sessionStore.snapshot.value ?: return
        val currentPath = currentState.titlePath.takeIf { it.isNotBlank() } ?: return
        val parentPath = snapshot.nodes[currentPath]?.parentPath ?: return
        showDirectory(parentPath, trackHistory = true)
    }

    private fun renderFromCurrentSnapshot(preferredPath: String?, historyMode: HistoryMode) {
        val snapshot = sessionStore.snapshot.value
        if (snapshot == null || snapshot.roots.isEmpty()) {
            updateState { it.copy(isLoading = false, errorMessage = "Нет данных сканирования") }
            return
        }
        renderSnapshot(snapshot, preferredPath, historyMode = historyMode)
    }

    private fun renderSnapshot(
        snapshot: FileSystemScanSnapshot,
        preferredPath: String?,
        historyMode: HistoryMode,
    ) {
        val topFiles = topLargeFilesInteractor.getTop(snapshot)
        val targetDirectory = resolveTargetDirectory(snapshot, preferredPath, topFiles.firstOrNull()?.parentPath)

        if (targetDirectory == null) {
            updateState {
                it.copy(
                    isLoading = false,
                    canNavigateBack = directoryHistoryIndex > 0,
                    canNavigateForward = directoryHistoryIndex >= 0 && directoryHistoryIndex < directoryHistory.lastIndex,
                    canNavigateUp = false,
                    topFiles = topFiles,
                    issuesCount = snapshot.issues.size,
                    errorMessage = "Не удалось определить стартовую папку",
                )
            }
            return
        }

        val breakdown = directoryBreakdownInteractor.build(snapshot, targetDirectory)
        syncHistory(breakdown.currentPath, historyMode)
        val currentNode = snapshot.nodes[breakdown.currentPath]
        updateState {
            it.copy(
                isLoading = false,
                canNavigateBack = directoryHistoryIndex > 0,
                canNavigateForward = directoryHistoryIndex >= 0 && directoryHistoryIndex < directoryHistory.lastIndex,
                canNavigateUp = currentNode?.parentPath != null,
                titlePath = breakdown.currentPath,
                breadcrumbs = breakdown.breadcrumbs,
                topFiles = topFiles,
                currentEntries = breakdown.entries,
                pieSlices = breakdown.pieSlices,
                issuesCount = snapshot.issues.size,
                errorMessage = null,
            )
        }
    }

    private fun resolveTargetDirectory(
        snapshot: FileSystemScanSnapshot,
        preferredPath: String?,
        fallbackFromTop: String?,
    ): String? {
        if (preferredPath != null) {
            var cursor: String? = preferredPath
            while (cursor != null) {
                if (snapshot.nodes[cursor] is FileSystemNode.DirectoryNode) return cursor
                cursor = snapshot.nodes[cursor]?.parentPath
            }
        }
        return snapshot.roots.firstOrNull { snapshot.nodes[it] is FileSystemNode.DirectoryNode }
            ?: fallbackFromTop
    }

    private fun showDirectory(path: String, trackHistory: Boolean) {
        val snapshot = sessionStore.snapshot.value ?: return
        if (snapshot.nodes[path] !is FileSystemNode.DirectoryNode) return
        val breakdown = directoryBreakdownInteractor.build(snapshot, path)
        if (trackHistory) {
            syncHistory(path, HistoryMode.Push)
        }
        updateState {
            it.copy(
                canNavigateBack = directoryHistoryIndex > 0,
                canNavigateForward = directoryHistoryIndex >= 0 && directoryHistoryIndex < directoryHistory.lastIndex,
                canNavigateUp = snapshot.nodes[path]?.parentPath != null,
                titlePath = breakdown.currentPath,
                breadcrumbs = breakdown.breadcrumbs,
                currentEntries = breakdown.entries,
                pieSlices = breakdown.pieSlices,
                errorMessage = null,
            )
        }
    }

    private fun onFocusFileParent(path: String) {
        val snapshot = sessionStore.snapshot.value ?: return
        val file = snapshot.nodes[path] as? FileSystemNode.FileNode ?: return
        file.parentPath?.let { showDirectory(it, trackHistory = true) }
    }

    private fun applyLocalDeletion(path: String): Boolean {
        val snapshot = sessionStore.snapshot.value ?: return false
        val targetNode = snapshot.nodes[path] ?: return false

        val removedPaths = collectSubtreePaths(snapshot, path)
        val mutableNodes = snapshot.nodes.toMutableMap().apply {
            removedPaths.forEach { remove(it) }
        }

        val ancestors = generateSequence(targetNode.parentPath) { parent ->
            mutableNodes[parent]?.parentPath
        }.toList()

        for (ancestorPath in ancestors) {
            val ancestor = mutableNodes[ancestorPath] as? FileSystemNode.DirectoryNode ?: continue
            val newChildren = ancestor.children.filter { childPath -> childPath !in removedPaths && mutableNodes.containsKey(childPath) }
            val newSize = newChildren.sumOf { childPath -> mutableNodes[childPath]?.sizeBytes ?: 0L }
            mutableNodes[ancestorPath] = ancestor.copy(
                children = newChildren,
                sizeBytes = newSize,
            )
        }

        val newRoots = snapshot.roots.filter { it !in removedPaths && mutableNodes.containsKey(it) }
        val filesCount = mutableNodes.values.count { it is FileSystemNode.FileNode }.toLong()
        val dirsCount = mutableNodes.values.count { it is FileSystemNode.DirectoryNode }.toLong()
        val bytesCount = mutableNodes.values.filterIsInstance<FileSystemNode.FileNode>().sumOf { it.sizeBytes }

        sessionStore.setSnapshot(
            FileSystemScanSnapshot(
                roots = newRoots,
                nodes = mutableNodes,
                issues = snapshot.issues.filterNot { issue -> issue.path in removedPaths || issue.path.startsWith("$path\\") },
                filesScanned = filesCount,
                directoriesScanned = dirsCount,
                bytesObserved = bytesCount,
            )
        )
        return true
    }

    private fun collectSubtreePaths(snapshot: FileSystemScanSnapshot, rootPath: String): Set<String> {
        val result = linkedSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(rootPath)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (!result.add(current)) continue
            val node = snapshot.nodes[current] as? FileSystemNode.DirectoryNode ?: continue
            node.children.forEach(queue::addLast)
        }
        return result
    }

    private fun syncHistory(path: String, mode: HistoryMode) {
        when (mode) {
            HistoryMode.Reset -> {
                directoryHistory.clear()
                directoryHistory += path
                directoryHistoryIndex = 0
            }
            HistoryMode.Push -> {
                if (directoryHistoryIndex >= 0 && directoryHistory[directoryHistoryIndex] == path) return
                if (directoryHistoryIndex < directoryHistory.lastIndex) {
                    directoryHistory.subList(directoryHistoryIndex + 1, directoryHistory.size).clear()
                }
                directoryHistory += path
                directoryHistoryIndex = directoryHistory.lastIndex
            }
            HistoryMode.KeepCurrent -> {
                if (directoryHistory.isEmpty()) {
                    directoryHistory += path
                    directoryHistoryIndex = 0
                    return
                }
                if (directoryHistoryIndex !in directoryHistory.indices) {
                    directoryHistoryIndex = directoryHistory.lastIndex
                }
                directoryHistory[directoryHistoryIndex] = path
            }
        }
    }

    private enum class HistoryMode {
        Reset,
        Push,
        KeepCurrent,
    }
}
