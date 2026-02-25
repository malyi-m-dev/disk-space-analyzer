package st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import st.service.core_utils.SizeFormatter
import st.service.feature_disk_analyzer.impl.presentation.screen.scan_progress.mvi.ScanProgressState

@Composable
internal fun ScanProgressContent(
    state: ScanProgressState,
    onCancelClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Сканирование", style = MaterialTheme.typography.headlineMedium)
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text("Файлов: ${state.filesScanned}")
        Text("Папок: ${state.directoriesScanned}")
        Text("Просканировано: ${SizeFormatter.format(state.bytesObserved)}")
        Text("Ошибок/пропусков: ${state.issueCount}")
        state.currentPath?.let { Text("Текущий путь: $it", style = MaterialTheme.typography.bodySmall) }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.weight(1f))
        Button(onClick = onCancelClicked) { Text("Отмена") }
    }
}
