package st.service.feature_disk_analyzer.impl.presentation.screen.setup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import st.service.core_utils.SizeFormatter
import st.service.feature_disk_analyzer.impl.presentation.screen.setup.mvi.DiskAnalyzerSetupDriveItemState
import st.service.feature_disk_analyzer.impl.presentation.screen.setup.mvi.DiskAnalyzerSetupState

@Composable
internal fun DiskAnalyzerSetupContent(
    state: DiskAnalyzerSetupState,
    onPathChanged: (String) -> Unit,
    onPickFolderClicked: () -> Unit,
    onDriveToggled: (String) -> Unit,
    onScanPathClicked: () -> Unit,
    onScanSelectedDrivesClicked: () -> Unit,
    onScanAllDisksClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Disk Space Analyzer", style = MaterialTheme.typography.headlineMedium)
        Text("Snapshot сканирование с drill-down и списком крупных файлов.")

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Сканировать папку", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = state.path,
                    onValueChange = onPathChanged,
                    label = { Text("Путь") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !state.isValid,
                    supportingText = { state.message?.let { Text(it) } },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onPickFolderClicked) { Text("Выбрать папку...") }
                    Button(onClick = onScanPathClicked) { Text("Сканировать путь") }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Диски", style = MaterialTheme.typography.titleMedium)
                if (state.drives.isEmpty()) {
                    Text("Диски не найдены")
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.drives, key = { it.path }) { drive ->
                            DriveRow(drive = drive, onToggle = { onDriveToggled(drive.path) })
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onScanSelectedDrivesClicked) { Text("Сканировать выбранные диски") }
                    Button(onClick = onScanAllDisksClicked) { Text("Сканировать все диски") }
                }
            }
        }
    }
}

@Composable
private fun DriveRow(
    drive: DiskAnalyzerSetupDriveItemState,
    onToggle: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = drive.isSelected, onCheckedChange = { onToggle() })
        Column {
            Text(drive.label)
            val free = drive.freeSpaceBytes?.let { SizeFormatter.format(it) } ?: "?"
            val total = drive.totalSpaceBytes?.let { SizeFormatter.format(it) } ?: "?"
            Text("Свободно: $free / Всего: $total", style = MaterialTheme.typography.bodySmall)
        }
    }
}
