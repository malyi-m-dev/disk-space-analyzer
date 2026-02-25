package st.service.data_filesystem.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import st.service.data_filesystem.FileSystemNode
import st.service.data_filesystem.FileSystemScanIssue
import st.service.data_filesystem.FileSystemScanIssueKind
import st.service.data_filesystem.FileSystemScanProgress
import st.service.data_filesystem.FileSystemScanRequest
import st.service.data_filesystem.FileSystemScanRoot
import st.service.data_filesystem.FileSystemScanSnapshot
import st.service.data_filesystem.FileSystemScannerRepository
import java.io.File
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.coroutineContext
import kotlin.io.path.name

class JvmFileSystemScannerRepository : FileSystemScannerRepository {
    override suspend fun scan(
        request: FileSystemScanRequest,
        onProgress: (FileSystemScanProgress) -> Unit,
    ): FileSystemScanSnapshot = withContext(Dispatchers.IO) {
        val nodes = ConcurrentHashMap<String, FileSystemNode>()
        val issues = ConcurrentLinkedQueue<FileSystemScanIssue>()
        val filesScanned = AtomicLong(0L)
        val directoriesScanned = AtomicLong(0L)
        val bytesObserved = AtomicLong(0L)
        val progressCounter = AtomicLong(0L)
        val lastProgressEmitMs = AtomicLong(0L)
        val parallelism = 6
        val parallelDispatcher = Dispatchers.IO.limitedParallelism(parallelism)

        fun emitProgress(currentPath: String?) {
            val counter = progressCounter.incrementAndGet()
            val now = System.currentTimeMillis()
            val shouldEmit = currentPath == null ||
                counter % 500L == 0L ||
                now - lastProgressEmitMs.get() >= 200L
            if (shouldEmit && (currentPath == null || lastProgressEmitMs.getAndSet(now) != now)) {
                onProgress(
                    FileSystemScanProgress(
                        currentPath = currentPath,
                        filesScanned = filesScanned.get(),
                        directoriesScanned = directoriesScanned.get(),
                        bytesObserved = bytesObserved.get(),
                        issueCount = issues.size.toLong(),
                    )
                )
            }
        }

        suspend fun scanPath(path: Path, parentPath: String?): String? {
            coroutineContext.ensureActive()

            val normalizedPath = path.toAbsolutePath().normalize().toString()
            if (nodes.containsKey(normalizedPath)) return normalizedPath

            if (request.skipSymlinks && Files.isSymbolicLink(path)) {
                return null
            }

            val attrs = try {
                Files.readAttributes(path, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
            } catch (e: AccessDeniedException) {
                issues += FileSystemScanIssue(
                    path = normalizedPath,
                    kind = FileSystemScanIssueKind.ACCESS_DENIED,
                    message = e.message,
                )
                return null
            } catch (e: Exception) {
                issues += FileSystemScanIssue(
                    path = normalizedPath,
                    kind = FileSystemScanIssueKind.IO_ERROR,
                    message = e.message,
                )
                return null
            }

            val hidden = try {
                Files.isHidden(path)
            } catch (_: Exception) {
                false
            }
            if (!request.includeHidden && hidden) return null

            if (attrs.isRegularFile) {
                val fileNode = FileSystemNode.FileNode(
                    path = normalizedPath,
                    name = path.name.ifBlank { normalizedPath },
                    parentPath = parentPath,
                    sizeBytes = attrs.size(),
                    lastModifiedEpochMs = attrs.lastModifiedTime()?.toMillis(),
                    isHidden = hidden,
                )
                nodes[normalizedPath] = fileNode
                filesScanned.incrementAndGet()
                bytesObserved.addAndGet(fileNode.sizeBytes)
                emitProgress(normalizedPath)
                return normalizedPath
            }

            if (!attrs.isDirectory) return null

            directoriesScanned.incrementAndGet()
            val childIds = mutableListOf<String>()
            var isPartial = false
            var totalSize = 0L

            try {
                Files.newDirectoryStream(path).use { stream ->
                    coroutineScope {
                        val deferredDirectories = mutableListOf<kotlinx.coroutines.Deferred<String?>>()

                        for (child in stream) {
                            val shouldParallelize = try {
                                Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)
                            } catch (_: Exception) {
                                false
                            }
                            if (shouldParallelize) {
                                deferredDirectories += async(parallelDispatcher) {
                                    scanPath(child, normalizedPath)
                                }
                                continue
                            }

                            val childId = try {
                                scanPath(child, normalizedPath)
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                null
                            }
                            if (childId != null) {
                                childIds += childId
                                totalSize += nodes[childId]?.sizeBytes ?: 0L
                            }
                        }

                        deferredDirectories.awaitAll().forEach { childId ->
                            if (childId != null) {
                                childIds += childId
                                totalSize += nodes[childId]?.sizeBytes ?: 0L
                            }
                        }
                    }
                }
            } catch (e: AccessDeniedException) {
                isPartial = true
                issues += FileSystemScanIssue(normalizedPath, FileSystemScanIssueKind.ACCESS_DENIED, e.message)
            } catch (e: Exception) {
                isPartial = true
                issues += FileSystemScanIssue(normalizedPath, FileSystemScanIssueKind.IO_ERROR, e.message)
            }

            val directoryNode = FileSystemNode.DirectoryNode(
                path = normalizedPath,
                name = path.name.ifBlank { normalizedPath },
                parentPath = parentPath,
                sizeBytes = totalSize,
                lastModifiedEpochMs = attrs.lastModifiedTime()?.toMillis(),
                isHidden = hidden,
                children = childIds,
                isPartial = isPartial,
            )
            nodes[normalizedPath] = directoryNode
            emitProgress(normalizedPath)
            return normalizedPath
        }

        val rootPaths = resolveRoots(request)
        val rootIds = coroutineScope {
            rootPaths.map { rootPath ->
                async(parallelDispatcher) {
                    try {
                        scanPath(rootPath, parentPath = null)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        issues += FileSystemScanIssue(
                            path = rootPath.toString(),
                            kind = FileSystemScanIssueKind.IO_ERROR,
                            message = e.message,
                        )
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

        onProgress(
            FileSystemScanProgress(
                currentPath = null,
                filesScanned = filesScanned.get(),
                directoriesScanned = directoriesScanned.get(),
                bytesObserved = bytesObserved.get(),
                issueCount = issues.size.toLong(),
            )
        )

        FileSystemScanSnapshot(
            roots = rootIds,
            nodes = nodes.toMap(),
            issues = issues.toList(),
            filesScanned = filesScanned.get(),
            directoriesScanned = directoriesScanned.get(),
            bytesObserved = bytesObserved.get(),
        )
    }

    private fun resolveRoots(request: FileSystemScanRequest): List<Path> {
        return request.roots.flatMap { root ->
            when (root) {
                is FileSystemScanRoot.Directory -> listOf(Paths.get(root.path))
                FileSystemScanRoot.AllDrives -> File.listRoots().map { it.toPath() }
            }
        }.distinctBy { it.toAbsolutePath().normalize().toString() }
    }
}
