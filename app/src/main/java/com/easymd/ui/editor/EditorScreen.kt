package com.easymd.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easymd.R
import com.easymd.core.markdown.SyntaxHighlighter
import com.easymd.ui.components.LinkDialog
import com.easymd.ui.components.TableDialog
import com.easymd.ui.theme.LocalHighlightColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    file: File?,
    onPreview: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hlColors = LocalHighlightColors.current

    var showLinkDialog  by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }

    // Load file when it changes
    LaunchedEffect(file) {
        file?.let { viewModel.openFile(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.document?.title ?: stringResource(R.string.untitled),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = state.wordCount.let {
                                "${it.characters} 字 · ${it.readingMinutes} 分钟"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Save status indicator
                    val (icon, tint) = when (state.saveStatus) {
                        SaveStatus.SAVED   -> Icons.Outlined.CloudDone to MaterialTheme.colorScheme.secondary
                        SaveStatus.SAVING  -> Icons.Outlined.Sync to MaterialTheme.colorScheme.onSurfaceVariant
                        SaveStatus.UNSAVED -> Icons.Outlined.Edit to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp).padding(end = 8.dp))

                    IconButton(onClick = { viewModel.saveNow() }) {
                        Icon(Icons.Outlined.Save, contentDescription = "保存")
                    }
                    IconButton(onClick = onPreview) {
                        Icon(Icons.Outlined.Visibility, contentDescription = "预览")
                    }
                    IconButton(onClick = { /* share */ }) {
                        Icon(Icons.Outlined.Share, contentDescription = "分享")
                    }
                }
            )
        },
        bottomBar = {
            FormatToolbar(
                onAction = { action ->
                    when (action) {
                        FormatAction.LINK  -> showLinkDialog  = true
                        FormatAction.TABLE -> showTableDialog = true
                        else               -> viewModel.applyFormat(action)
                    }
                }
            )
        }
    ) { padding ->
        if (file == null || state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                if (state.isLoading) CircularProgressIndicator()
                else Text("请从文件列表选择一个文档", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        // Editor body with syntax highlighting
        val annotated = remember(state.textField.text, hlColors) {
            SyntaxHighlighter.highlight(state.textField.text, hlColors)
        }

        BasicTextField(
            value = state.textField,
            onValueChange = viewModel::onTextChanged,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            textStyle = TextStyle(
                color      = MaterialTheme.colorScheme.onBackground,
                fontSize   = 16.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 26.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (state.textField.text.isEmpty()) {
                    Text(
                        text = "开始写作…",
                        style = TextStyle(
                            color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize   = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
                innerTextField()
            }
        )
    }

    // Dialogs
    if (showLinkDialog) {
        LinkDialog(
            initialText = "",
            onInsert = { text, url ->
                viewModel.applyFormat(FormatAction.LINK)
                // Override with actual values in real impl
                showLinkDialog = false
            },
            onDismiss = { showLinkDialog = false }
        )
    }

    if (showTableDialog) {
        TableDialog(
            onInsert = { rows, cols ->
                val header = (1..cols).joinToString(" | ") { "列$it" }
                val sep    = (1..cols).joinToString(" | ") { "---" }
                val body   = (1 until rows).joinToString("\n") {
                    (1..cols).joinToString(" | ") { "  " }
                }
                val md = "\n| $header |\n| $sep |\n| $body |\n"
                viewModel.applyFormat(FormatAction.TABLE)
                showTableDialog = false
            },
            onDismiss = { showTableDialog = false }
        )
    }
}
