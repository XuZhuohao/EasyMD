package com.easymd.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easymd.core.data.FileRepository
import com.easymd.core.markdown.MarkdownParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class PreviewUiState(
    val title: String     = "",
    val html: String      = "",
    val isLoading: Boolean = false,
    val error: String?    = null
)

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val repo: FileRepository,
    private val parser: MarkdownParser
) : ViewModel() {

    private val _state = MutableStateFlow(PreviewUiState())
    val state: StateFlow<PreviewUiState> = _state.asStateFlow()

    private var lastFile: File? = null
    private var lastDark: Boolean = false

    fun loadFile(file: File, darkTheme: Boolean) {
        lastFile = file
        lastDark = darkTheme
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repo.readDocument(file) }
                .onSuccess { doc ->
                    val html = parser.toHtml(doc.content, darkTheme)
                    _state.update { it.copy(title = doc.title, html = html, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun onThemeChanged(dark: Boolean) {
        val file = lastFile ?: return
        if (dark != lastDark) loadFile(file, dark)
    }
}
