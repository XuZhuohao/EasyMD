package com.easymd.ui.filelist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easymd.R
import com.easymd.core.model.FileNode
import com.easymd.core.model.SyncState
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileListScreen(
    onOpenFile: (File) -> Unit,
    viewModel: FileListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filtered by viewModel.filteredNodes.collectAsStateWithLifecycle()
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var contextMenuNode by remember { mutableStateOf<FileNode.Doc?>(null) }

    Scaffold(
        topBar = {
            Column {
                // Search bar
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::onSearchQuery,
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text("搜索文档…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("H", color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {}
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::createNewFile,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(R.string.new_document)) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filtered.isEmpty() && !state.isLoading) {
            EmptyState(Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                // Section: Recent (flat docs from root)
                val rootDocs = filtered.filterIsInstance<FileNode.Doc>()
                val folders  = filtered.filterIsInstance<FileNode.Folder>()

                if (rootDocs.isNotEmpty()) {
                    item {
                        SectionHeader("最近使用")
                    }
                    items(rootDocs, key = { it.file.path }) { doc ->
                        FileDocRow(
                            doc = doc,
                            onOpen = { onOpenFile(doc.file) },
                            onLongPress = { contextMenuNode = doc },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }

                if (folders.isNotEmpty()) {
                    item { SectionHeader("文件夹") }
                    items(folders, key = { it.file.path }) { folder ->
                        FolderRow(folder = folder, onOpen = { /* expand */ })
                    }
                }
            }
        }
    }

    // Context bottom sheet
    contextMenuNode?.let { node ->
        FileContextSheet(
            node = node,
            onDismiss = { contextMenuNode = null },
            onRename  = { viewModel.requestRename(node); contextMenuNode = null },
            onDelete  = { viewModel.requestDelete(node); contextMenuNode = null },
            onShare   = { /* share intent */ contextMenuNode = null }
        )
    }

    // Delete dialog
    state.showDeleteDialog?.let { node ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title   = { Text("删除文件") },
            text    = { Text("确认删除「${node.name}」？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) { Text("取消") }
            }
        )
    }

    // Rename dialog
    state.showRenameDialog?.let { _ ->
        AlertDialog(
            onDismissRequest = viewModel::dismissRename,
            title   = { Text("重命名") },
            text    = {
                OutlinedTextField(
                    value = state.renameInput,
                    onValueChange = viewModel::onRenameInput,
                    singleLine = true,
                    label = { Text("新名称") }
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmRename) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRename) { Text("取消") }
            }
        )
    }

    // Error snackbar
    state.error?.let { msg ->
        LaunchedEffect(msg) {
            viewModel.clearError()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileDocRow(
    doc: FileNode.Doc,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onOpen, onLongClick = onLongPress)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = doc.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${formatRelativeTime(doc.modifiedAt)} · ${formatSize(doc.sizeBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SyncIcon(doc.syncState)
    }
}

@Composable
private fun FolderRow(folder: FileNode.Folder, onOpen: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onOpen)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(folder.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("${folder.childCount} 个文档", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SyncIcon(state: SyncState) {
    when (state) {
        SyncState.SYNCED   -> Icon(Icons.Outlined.CloudDone, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        SyncState.SYNCING  -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        SyncState.CONFLICT -> Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
        SyncState.LOCAL    -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileContextSheet(
    node: FileNode.Doc,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) {
                Icon(Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(node.name, style = MaterialTheme.typography.titleMedium)
                Text("${formatSize(node.sizeBytes)}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        // Actions
        listOf(
            Triple(Icons.Outlined.DriveFileRenameOutline, "重命名", onRename),
            Triple(Icons.Outlined.Share, "分享", onShare),
            Triple(Icons.Outlined.FileDownload, "导出 PDF", {}),
        ).forEach { (icon, label, action) ->
            ListItem(
                headlineContent = { Text(label) },
                leadingContent  = { Icon(icon, null) },
                modifier = Modifier.combinedClickable(onClick = action)
            )
        }
        ListItem(
            headlineContent = { Text("删除", color = MaterialTheme.colorScheme.error) },
            leadingContent  = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
            modifier = Modifier.combinedClickable(onClick = onDelete)
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Description, null, modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.no_document_title), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.no_document_sub), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatRelativeTime(instant: Instant): String {
    val now = Instant.now()
    val minutes = ChronoUnit.MINUTES.between(instant, now)
    return when {
        minutes < 1    -> "刚刚"
        minutes < 60   -> "${minutes}分钟前"
        minutes < 1440 -> "${minutes / 60}小时前"
        minutes < 2880 -> "昨天"
        else           -> DateTimeFormatter.ofPattern("M月d日")
            .withZone(ZoneId.systemDefault()).format(instant)
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024       -> "${bytes} B"
    bytes < 1048576    -> "${bytes / 1024} KB"
    else               -> String.format("%.1f MB", bytes / 1048576.0)
}
