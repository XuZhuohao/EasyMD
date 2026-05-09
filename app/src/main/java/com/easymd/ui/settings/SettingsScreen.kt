package com.easymd.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easymd.R
import com.easymd.core.model.AppTheme
import com.easymd.core.model.FontSize
import com.easymd.core.model.PdfMargin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("设置") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Appearance ──────────────────────────────────────────────
            SettingsSection(title = "外观") {
                // Theme
                SettingsRow(
                    title = stringResource(R.string.settings_theme),
                    control = {
                        SingleChoiceSegmentedButtonRow {
                            val options = listOf(
                                AppTheme.SYSTEM to "系统",
                                AppTheme.LIGHT  to "浅色",
                                AppTheme.DARK   to "深色"
                            )
                            options.forEachIndexed { i, (theme, label) ->
                                SegmentedButton(
                                    selected = settings.theme == theme,
                                    onClick  = { viewModel.setTheme(theme) },
                                    shape    = SegmentedButtonDefaults.itemShape(i, options.size),
                                    label    = { Text(label) }
                                )
                            }
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                // Font size
                SettingsRow(
                    title = stringResource(R.string.settings_font_size),
                    control = {
                        val sizes = FontSize.values()
                        val idx = sizes.indexOf(settings.fontSize).coerceAtLeast(0)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("A", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Slider(
                                value = idx.toFloat(),
                                onValueChange = { viewModel.setFontSize(sizes[it.toInt()]) },
                                valueRange = 0f..(sizes.size - 1).toFloat(),
                                steps = sizes.size - 2,
                                modifier = Modifier.width(120.dp)
                            )
                            Text("A", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }

            // ── Editor ───────────────────────────────────────────────────
            SettingsSection(title = "编辑器") {
                SwitchRow(
                    title = stringResource(R.string.settings_auto_save),
                    subtitle = "修改后 1 秒自动保存",
                    checked = settings.autoSave,
                    onCheckedChange = viewModel::setAutoSave
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SwitchRow(
                    title = stringResource(R.string.settings_line_numbers),
                    checked = settings.showLineNumbers,
                    onCheckedChange = viewModel::setLineNumbers
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SwitchRow(
                    title = stringResource(R.string.settings_tab_spaces),
                    subtitle = "Tab 键插入 ${settings.tabWidth} 个空格",
                    checked = settings.tabToSpaces,
                    onCheckedChange = viewModel::setTabToSpaces
                )
            }

            // ── Sync ─────────────────────────────────────────────────────
            SettingsSection(title = "同步") {
                SwitchRow(
                    title = stringResource(R.string.settings_sync),
                    subtitle = if (settings.driveSync) "已连接 Google Drive" else "未连接",
                    checked = settings.driveSync,
                    onCheckedChange = viewModel::setDriveSync
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("同步状态") },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.CloudDone, null,
                                tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Text("已同步", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                )
            }

            // ── Export ───────────────────────────────────────────────────
            SettingsSection(title = "导出") {
                SettingsRow(
                    title = stringResource(R.string.settings_pdf_margin),
                    control = {
                        SingleChoiceSegmentedButtonRow {
                            val margins = listOf(PdfMargin.NARROW to "窄", PdfMargin.STANDARD to "标准", PdfMargin.WIDE to "宽")
                            margins.forEachIndexed { i, (m, label) ->
                                SegmentedButton(
                                    selected = settings.pdfMargin == m,
                                    onClick  = { viewModel.setPdfMargin(m) },
                                    shape    = SegmentedButtonDefaults.itemShape(i, margins.size),
                                    label    = { Text(label) }
                                )
                            }
                        }
                    }
                )
            }

            // About
            Spacer(Modifier.height(16.dp))
            Text(
                "EasyMD v1.0.0 · 简墨",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp)
    )
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(title: String, control: @Composable () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = control
    )
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent    = { Text(title) },
        supportingContent  = subtitle?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
        trailingContent    = { Switch(checked = checked, onCheckedChange = onCheckedChange) }
    )
}
