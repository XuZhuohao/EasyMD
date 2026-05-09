package com.easymd

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.easymd.ui.editor.EditorViewModel
import com.easymd.ui.editor.FormatAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After  fun teardown() = Dispatchers.resetMain()

    private fun makeVm(): EditorViewModel {
        // Minimal fake repo
        val repo = object : com.easymd.core.data.FileRepository {
            override val rootDir get() = java.io.File(".")
            override fun observeFiles(root: java.io.File) = kotlinx.coroutines.flow.flow<List<com.easymd.core.model.FileNode>> {}
            override suspend fun loadTree(root: java.io.File) = emptyList<com.easymd.core.model.FileNode>()
            override suspend fun readDocument(file: java.io.File) =
                com.easymd.core.model.Document(file, "")
            override suspend fun saveDocument(document: com.easymd.core.model.Document) {}
            override suspend fun createFile(dir: java.io.File, name: String) = java.io.File("$name.md")
            override suspend fun createFolder(parent: java.io.File, name: String) = java.io.File(name)
            override suspend fun deleteFile(file: java.io.File) {}
            override suspend fun renameFile(file: java.io.File, newName: String) = java.io.File("$newName.md")
        }
        return EditorViewModel(repo)
    }

    @Test fun `bold wraps selection`() = runTest {
        val vm = makeVm()
        // Simulate having a document open
        vm.onTextChanged(TextFieldValue("hello world", selection = TextRange(6, 11)))
        vm.applyFormat(FormatAction.BOLD)
        assertTrue(vm.state.value.textField.text.contains("**world**"))
    }

    @Test fun `heading1 adds prefix`() = runTest {
        val vm = makeVm()
        vm.onTextChanged(TextFieldValue("Hello", selection = TextRange(2)))
        vm.applyFormat(FormatAction.HEADING1)
        assertTrue(vm.state.value.textField.text.startsWith("# "))
    }

    @Test fun `italic wraps selection`() = runTest {
        val vm = makeVm()
        vm.onTextChanged(TextFieldValue("text", selection = TextRange(0, 4)))
        vm.applyFormat(FormatAction.ITALIC)
        assertTrue(vm.state.value.textField.text.contains("*text*"))
    }

    @Test fun `unordered list adds prefix`() = runTest {
        val vm = makeVm()
        vm.onTextChanged(TextFieldValue("item", selection = TextRange(0)))
        vm.applyFormat(FormatAction.UNORDERED_LIST)
        assertTrue(vm.state.value.textField.text.startsWith("- "))
    }

    @Test fun `task list adds checkbox`() = runTest {
        val vm = makeVm()
        vm.onTextChanged(TextFieldValue("todo", selection = TextRange(0)))
        vm.applyFormat(FormatAction.TASK_LIST)
        assertTrue(vm.state.value.textField.text.startsWith("- [ ] "))
    }
}
