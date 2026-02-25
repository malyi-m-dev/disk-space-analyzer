package st.service.feature_disk_analyzer.impl.domain.interactor

import st.service.data_filesystem.FileSystemScanProgress
import st.service.data_filesystem.FileSystemScanRequest
import st.service.data_filesystem.FileSystemScanSnapshot
import st.service.data_filesystem.FileSystemScannerRepository

class ScanFileSystemInteractor(
    private val fileSystemScannerRepository: FileSystemScannerRepository,
) {
    suspend fun scan(
        request: FileSystemScanRequest,
        onProgress: (FileSystemScanProgress) -> Unit,
    ): FileSystemScanSnapshot {
        return fileSystemScannerRepository.scan(request, onProgress)
    }
}
