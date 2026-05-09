package com.easymd.core.data

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import com.easymd.core.model.Document
import com.easymd.core.model.FileNode
import com.easymd.core.model.SyncState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileObserver
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileRepository {

    override val rootDir: File by lazy {
        File(context.filesDir, "documents").also { it.mkdirs() }
    }

    // ── Tree loading ─────────────────────────────────────────────────────────

    override suspend fun loadTree(root: File): List<FileNode> = withContext(Dispatchers.IO) {
        buildTree(root)
    }

    override fun observeFiles(root: File): Flow<List<FileNode>> = callbackFlow {
        var lastSnapshot = listOf<FileNode>()
        val observer = object : FileObserver(root, ALL_EVENTS) {
            override fun onEvent(event: Int, path: String?) {
                trySend(Unit)
            }
        }
        observer.startWatching()
        // initial emit
        trySend(Unit)
        // collect
        for (unit in channel) {
            val tree = buildTree(root)
            if (tree != lastSnapshot) {
                lastSnapshot = tree
                send(tree)
            }
        }
        awaitClose { observer.stopWatching() }
    }.flowOn(Dispatchers.IO)

    private fun buildTree(dir: File): List<FileNode> {
        val entries = dir.listFiles() ?: return emptyList()
        return entries
            .filter { it.isDirectory || it.extension == "md" }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            .map { entry ->
                if (entry.isDirectory) {
                    FileNode.Folder(
                        file = entry,
                        children = buildTree(entry)
                    )
                } else {
                    FileNode.Doc(
                        file = entry,
                        modifiedAt = Instant.ofEpochMilli(entry.lastModified()),
                        sizeBytes = entry.length()
                    )
                }
            }
    }

    // ── Document I/O ─────────────────────────────────────────────────────────

    override suspend fun readDocument(file: File): Document = withContext(Dispatchers.IO) {
        if (!file.exists()) throw FileError.NotFound(file)
        Document(
            file = file,
            content = file.readText(),
            modifiedAt = Instant.ofEpochMilli(file.lastModified())
        )
    }

    override suspend fun saveDocument(document: Document) = withContext(Dispatchers.IO) {
        document.file.writeText(document.content)
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    override suspend fun createFile(dir: File, name: String): File = withContext(Dispatchers.IO) {
        validateName(name)
        val unique = uniqueFile(dir, name, "md")
        unique.createNewFile()
        unique
    }

    override suspend fun createFolder(parent: File, name: String): File = withContext(Dispatchers.IO) {
        validateName(name)
        val unique = uniqueFile(parent, name, null)
        unique.mkdirs()
        unique
    }

    override suspend fun deleteFile(file: File) = withContext(Dispatchers.IO) {
        if (!file.exists()) throw FileError.NotFound(file)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val sm = context.getSystemService(StorageManager::class.java)
            sm.storageVolumes.firstOrNull()?.createOpenDocumentTreeIntent()
            // For scoped storage, just delete directly from app files dir
        }
        file.deleteRecursively()
    }

    override suspend fun renameFile(file: File, newName: String): File = withContext(Dispatchers.IO) {
        validateName(newName)
        val ext = if (file.isDirectory) null else "md"
        val target = uniqueFile(file.parentFile ?: rootDir, newName, ext)
        if (!file.renameTo(target)) throw FileError.IoError(Exception("rename failed"))
        target
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun validateName(name: String) {
        if (name.isBlank()) throw FileError.InvalidName("名称不能为空")
        if (name.any { it in """/\:*?"<>|""" }) throw FileError.InvalidName("名称包含非法字符")
    }

    private fun uniqueFile(dir: File, name: String, ext: String?): File {
        val fullName = if (ext != null) "$name.$ext" else name
        var candidate = File(dir, fullName)
        var counter = 1
        while (candidate.exists()) {
            val suffixed = if (ext != null) "$name ($counter).$ext" else "$name ($counter)"
            candidate = File(dir, suffixed)
            counter++
        }
        return candidate
    }
}
