package com.easymd.core.data

import com.easymd.core.model.Document
import com.easymd.core.model.FileNode
import java.io.File
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    /** Watch the root directory for changes. */
    fun observeFiles(root: File): Flow<List<FileNode>>

    /** Load tree once. */
    suspend fun loadTree(root: File): List<FileNode>

    /** Read a document from disk. */
    suspend fun readDocument(file: File): Document

    /** Persist document content. */
    suspend fun saveDocument(document: Document)

    /** Create a new .md file, deduplicating name if necessary. */
    suspend fun createFile(dir: File, name: String): File

    /** Create a subfolder. */
    suspend fun createFolder(parent: File, name: String): File

    /** Move file to system trash (API 30+) or delete. */
    suspend fun deleteFile(file: File)

    /** Rename, returning the new File. */
    suspend fun renameFile(file: File, newName: String): File

    /** Root documents directory. */
    val rootDir: File
}

sealed class FileError : Exception() {
    data class InvalidName(val reason: String) : FileError()
    data class AlreadyExists(val file: File) : FileError()
    data class NotFound(val file: File) : FileError()
    data class IoError(override val cause: Throwable) : FileError()
}
