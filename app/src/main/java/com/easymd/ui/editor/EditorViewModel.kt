package com.easymd.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easymd.core.data.FileRepository
import com.easymd.core.model.Document
import com.easymd.core.model.WordCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject

enum class SaveStatus { SAVED, SAVING, UNSAVED }

data class EditorUiState(
    val document: Document?         = null,
    val textField: TextFieldValue   = TextFieldValue(""),
    val saveStatus: SaveStatus      = SaveStatus.SAVED,
    val wordCount: WordCount        = WordCount(0, 0),
    val isLoading: Boolean          = false,
    val error: String?              = null
)

/** Pure toolbar actions */
enum class FormatAction {
    BOLD, ITALIC, STRIKETHROUGH, INLINE_CODE,
    HEADING1, HEADING2, HEADING3,
    UNORDERED_LIST, ORDERED_LIST, TASK_LIST,
    QUOTE, HR, LINK, IMAGE, TABLE
}

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repo: FileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    private var autoSaveJob: Job? = null

    // ── Open ─────────────────────────────────────────────────────────────

    fun openFile(file: File) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repo.readDocument(file) }
                .onSuccess { doc ->
                    _state.update {
                        it.copy(
                            document = doc,
                            textField = TextFieldValue(doc.content),
                            wordCount = doc.wordCount,
                            isLoading = false,
                            saveStatus = SaveStatus.SAVED
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    // ── Edit ─────────────────────────────────────────────────────────────

    fun onTextChanged(value: TextFieldValue) {
        val doc = _state.value.document ?: return
        _state.update {
            it.copy(
                textField  = value,
                document   = doc.copy(content = value.text, isDirty = true),
                wordCount  = WordCount.of(value.text),
                saveStatus = SaveStatus.UNSAVED
            )
        }
        scheduleAutoSave()
    }

    // ── Format ───────────────────────────────────────────────────────────

    fun applyFormat(action: FormatAction) {
        val tf  = _state.value.textField
        val sel = tf.selection
        val text = tf.text
        val selected = if (sel.collapsed) "" else text.substring(sel.min, sel.max)

        val (newText, newRange) = when (action) {
            FormatAction.BOLD          -> wrapInline(text, sel, "**", selected.ifBlank { "粗体" })
            FormatAction.ITALIC        -> wrapInline(text, sel, "*", selected.ifBlank { "斜体" })
            FormatAction.STRIKETHROUGH -> wrapInline(text, sel, "~~", selected.ifBlank { "删除线" })
            FormatAction.INLINE_CODE   -> wrapInline(text, sel, "`", selected.ifBlank { "代码" })
            FormatAction.HEADING1      -> insertLinePrefix(text, sel, "# ")
            FormatAction.HEADING2      -> insertLinePrefix(text, sel, "## ")
            FormatAction.HEADING3      -> insertLinePrefix(text, sel, "### ")
            FormatAction.UNORDERED_LIST -> insertLinePrefix(text, sel, "- ")
            FormatAction.ORDERED_LIST  -> insertLinePrefix(text, sel, "1. ")
            FormatAction.TASK_LIST     -> insertLinePrefix(text, sel, "- [ ] ")
            FormatAction.QUOTE         -> insertLinePrefix(text, sel, "> ")
            FormatAction.HR            -> insertSnippet(text, sel, "\n---\n")
            FormatAction.LINK          -> wrapInline(text, sel, "[", selected.ifBlank { "链接文字" }, "](url)")
            FormatAction.IMAGE         -> insertSnippet(text, sel, "![描述](url)")
            FormatAction.TABLE         -> insertSnippet(text, sel,
                "\n| 列1 | 列2 | 列3 |\n| --- | --- | --- |\n| 内容 | 内容 | 内容 |\n")
        }
        val newTf = tf.copy(text = newText, selection = newRange)
        onTextChanged(newTf)
    }

    // ── Save ─────────────────────────────────────────────────────────────

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1_000)
            saveNow()
        }
    }

    fun saveNow() {
        val doc = _state.value.document ?: return
        viewModelScope.launch {
            _state.update { it.copy(saveStatus = SaveStatus.SAVING) }
            runCatching { repo.saveDocument(doc.copy(content = _state.value.textField.text)) }
                .onSuccess { _state.update { it.copy(saveStatus = SaveStatus.SAVED) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, saveStatus = SaveStatus.UNSAVED) } }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun wrapInline(
        text: String, sel: TextRange, marker: String,
        inner: String, suffix: String = marker
    ): Pair<String, TextRange> {
        val before = text.substring(0, sel.min)
        val after  = text.substring(sel.max)
        // Toggle off if already wrapped
        if (before.endsWith(marker) && after.startsWith(suffix)) {
            val newText = before.dropLast(marker.length) + inner + after.drop(suffix.length)
            val start = sel.min - marker.length
            return newText to TextRange(start, start + inner.length)
        }
        val newText = "$before$marker$inner$suffix$after"
        val start = sel.min + marker.length
        return newText to TextRange(start, start + inner.length)
    }

    private fun insertLinePrefix(
        text: String, sel: TextRange, prefix: String
    ): Pair<String, TextRange> {
        val lineStart = text.lastIndexOf('\n', sel.min - 1) + 1
        // Toggle off
        if (text.substring(lineStart).startsWith(prefix)) {
            val newText = text.substring(0, lineStart) + text.substring(lineStart + prefix.length)
            return newText to TextRange(maxOf(lineStart, sel.min - prefix.length))
        }
        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        return newText to TextRange(sel.min + prefix.length)
    }

    private fun insertSnippet(
        text: String, sel: TextRange, snippet: String
    ): Pair<String, TextRange> {
        val newText = text.substring(0, sel.min) + snippet + text.substring(sel.max)
        val cursor = sel.min + snippet.length
        return newText to TextRange(cursor)
    }
}
