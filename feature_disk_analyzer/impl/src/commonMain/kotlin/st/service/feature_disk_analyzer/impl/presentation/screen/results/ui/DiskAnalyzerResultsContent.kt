package st.service.feature_disk_analyzer.impl.presentation.screen.results.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import st.service.core_platform_windows.WindowsDeleteMode
import st.service.core_utils.SizeFormatter
import st.service.feature_disk_analyzer.impl.domain.entity.DirectoryEntryEntity
import st.service.feature_disk_analyzer.impl.domain.entity.LargeFileItemEntity
import st.service.feature_disk_analyzer.impl.domain.entity.PieSliceEntity
import st.service.feature_disk_analyzer.impl.presentation.screen.results.mvi.DiskAnalyzerResultsState

@Composable
internal fun DiskAnalyzerResultsContent(
    state: DiskAnalyzerResultsState,
    onNavigateBackClicked: () -> Unit,
    onNavigateForwardClicked: () -> Unit,
    onNavigateUpClicked: () -> Unit,
    onOpenDirectory: (String) -> Unit,
    onBreadcrumbClicked: (String) -> Unit,
    onFocusFileParent: (String) -> Unit,
    onOpenInExplorerClicked: (String) -> Unit,
    onDeleteClicked: (String, WindowsDeleteMode) -> Unit,
    onRescanClicked: () -> Unit,
    onBackToSetupClicked: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(24.dp)) { Text("Загрузка результата...") }
            return@Box
        }
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = onRescanClicked) { Text("Рескан") }
                        Button(onClick = onBackToSetupClicked) { Text("Новое сканирование") }
                    }
                    Text("Проблем при сканировании: ${state.issuesCount}")
                    Text(
                        text = "Крупные файлы:",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (state.topFiles.isEmpty()) {
                        Text("Нет файлов")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(state.topFiles.take(100), key = { it.path }) { item ->
                                TopFileItem(
                                    item = item,
                                    onFocusFileParent = onFocusFileParent,
                                    onOpenInExplorerClicked = onOpenInExplorerClicked,
                                    onDeleteClicked = onDeleteClicked,
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1.2f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Drill-down", style = MaterialTheme.typography.titleLarge)
                        Text(state.titlePath, style = MaterialTheme.typography.bodySmall)
                        state.breadcrumbs.forEach { crumb ->
                            Text(
                                text = crumb,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable { onBreadcrumbClicked(crumb) },
                            )
                        }
                    }
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = onNavigateBackClicked,
                            enabled = state.canNavigateBack,
                        ) { Text("←") }
                        Button(
                            onClick = onNavigateForwardClicked,
                            enabled = state.canNavigateForward,
                        ) { Text("→") }
                        Button(
                            onClick = onNavigateUpClicked,
                            enabled = state.canNavigateUp,
                        ) { Text("↑") }
                    }
                }
                Card(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                    PieChartWithLegend(
                        slices = state.pieSlices,
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                    )
                }
                Card(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Содержимое", style = MaterialTheme.typography.titleMedium)
                        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(state.currentEntries, key = { it.path }) { entry ->
                                DirectoryEntryItem(
                                    entry = entry,
                                    onOpenDirectory = onOpenDirectory,
                                    onOpenInExplorerClicked = onOpenInExplorerClicked,
                                    onDeleteClicked = onDeleteClicked,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.isBusy) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0x55000000)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun TopFileItem(
    item: LargeFileItemEntity,
    onFocusFileParent: (String) -> Unit,
    onOpenInExplorerClicked: (String) -> Unit,
    onDeleteClicked: (String, WindowsDeleteMode) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.name)
            Text(item.path, style = MaterialTheme.typography.bodySmall)
            Text(SizeFormatter.format(item.sizeBytes), style = MaterialTheme.typography.labelMedium)
            TagsRow(item.tags.map { it.name })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Папка", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onFocusFileParent(item.path) })
                Text("Explorer", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onOpenInExplorerClicked(item.path) })
                Text("В корзину", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onDeleteClicked(item.path, WindowsDeleteMode.RECYCLE_BIN) })
                Text("Удалить", color = MaterialTheme.colorScheme.error, modifier = Modifier.clickable { onDeleteClicked(item.path, WindowsDeleteMode.PERMANENT) })
            }
        }
    }
}

@Composable
private fun DirectoryEntryItem(
    entry: DirectoryEntryEntity,
    onOpenDirectory: (String) -> Unit,
    onOpenInExplorerClicked: (String) -> Unit,
    onDeleteClicked: (String, WindowsDeleteMode) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(if (entry.isDirectory) "[DIR] ${entry.name}" else entry.name)
            Text(SizeFormatter.format(entry.sizeBytes), style = MaterialTheme.typography.labelMedium)
            TagsRow(entry.tags.map { it.name })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (entry.isDirectory) {
                    Text("Открыть", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onOpenDirectory(entry.path) })
                }
                Text("Explorer", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onOpenInExplorerClicked(entry.path) })
                Text("В корзину", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onDeleteClicked(entry.path, WindowsDeleteMode.RECYCLE_BIN) })
                Text("Удалить", color = MaterialTheme.colorScheme.error, modifier = Modifier.clickable { onDeleteClicked(entry.path, WindowsDeleteMode.PERMANENT) })
            }
        }
    }
}

@Composable
private fun TagsRow(tags: List<String>) {
    if (tags.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        tags.take(4).forEach { tag ->
            Box(
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer).padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(tag, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun PieChartWithLegend(
    slices: List<PieSliceEntity>,
    modifier: Modifier = Modifier,
) {
    val nonEmpty = slices.filter { it.value > 0L }
    val total = nonEmpty.sumOf { it.value }.coerceAtLeast(1L)
    val colors = listOf(
        Color(0xFF3A86FF), Color(0xFFFB5607), Color(0xFFFFBE0B), Color(0xFF2A9D8F),
        Color(0xFFE76F51), Color(0xFF8E9AAF), Color(0xFF264653), Color(0xFF06D6A0),
        Color(0xFFEF476F), Color(0xFF8338EC), Color(0xFF6D6875),
    )

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Canvas(modifier = Modifier.size(210.dp)) {
            var startAngle = -90f
            nonEmpty.forEachIndexed { index, slice ->
                val sweep = 360f * (slice.value.toFloat() / total.toFloat())
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                )
                startAngle += sweep
            }
        }
        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            nonEmpty.forEachIndexed { index, slice ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(colors[index % colors.size]))
                    Text("${slice.label}: ${SizeFormatter.format(slice.value)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
