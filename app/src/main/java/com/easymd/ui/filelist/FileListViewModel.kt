package com.easymd.ui.filelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easymd.core.data.FileRepository
import com.easymd.core.model.FileNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class FileListUiState(
    val nodes: List<FileNode> = emptyList(),
    val isLoading: Boolean    = true,
    val error: String?        = null,
    val searchQuery: String   = "",
    val showDeleteDialog: FileNode.Doc? = null,
    val showRenameDialog: FileNode.Doc? = null,
    val renameInput: String   = ""
)

@HiltViewModel
class FileListViewModel @Inject constructor(
    private val repo: FileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FileListUiState())
    val state: StateFlow<FileListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeFiles(repo.rootDir)
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { nodes -> _state.update { it.copy(nodes = nodes, isLoading = false) } }
        }
    }

    fun onSearchQuery(q: String) = _state.update { it.copy(searchQuery = q) }

    fun createNewFile() = viewModelScope.launch {
        runCatching { repo.createFile(repo.rootDir, "无标题") }
            .onFailure { e -> _state.update { it.copy(error = e.message) } }
    }

    fun createFolder(name: String) = viewModelScope.launch {
        runCatching { repo.createFolder(repo.rootDir, name) }
            .onFailure { e -> _state.update { it.copy(error = e.message) } }
    }

    fun requestDelete(node: FileNode.Doc) = _state.update { it.copy(showDeleteDialog = node) }
    fun dismissDelete()                   = _state.update { it.copy(showDeleteDialog = null) }

    fun confirmDelete() = viewModelScope.launch {
        val target = _state.value.showDeleteDialog ?: return@launch
        _state.update { it.copy(showDeleteDialog = null) }
        runCatching { repo.deleteFile(target.file) }
            .onFailure { e -> _state.update { it.copy(error = e.message) } }
    }

    fun requestRename(node: FileNode.Doc) = _state.update {
        it.copy(showRenameDialog = node, renameInput = node.name)
    }
    fun onRenameInput(s: String) = _state.update { it.copy(renameInput = s) }
    fun dismissRename()          = _state.update { it.copy(showRenameDialog = null) }

    fun confirmRename() = viewModelScope.launch {
        val target = _state.value.showRenameDialog ?: return@launch
        val name   = _state.value.renameInput.trim()
        _state.update { it.copy(showRenameDialog = null) }
        if (name.isBlank()) return@launch
        runCatching { repo.renameFile(target.file, name) }
            .onFailure { e -> _state.update { it.copy(error = e.message) } }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    /** Filtered flat list for search results. */
    val filteredNodes: StateFlow<List<FileNode>> = state.map { s ->
        if (s.searchQuery.isBlank()) s.nodes
        else flattenNodes(s.nodes).filter { node ->
            node.name.contains(s.searchQuery, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun flattenNodes(nodes: List<FileNode>): List<FileNode> = nodes.flatMap { node ->
        when (node) {
            is FileNode.Doc    -> listOf(node)
            is FileNode.Folder -> flattenNodes(node.children)
        }
    }
}
