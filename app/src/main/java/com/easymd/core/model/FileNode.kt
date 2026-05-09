package com.easymd.core.model

import java.io.File
import java.time.Instant

sealed class FileNode {
    abstract val file: File
    abstract val name: String

    data class Doc(
        override val file: File,
        val modifiedAt: Instant,
        val sizeBytes: Long,
        val syncState: SyncState = SyncState.LOCAL
    ) : FileNode() {
        override val name: String get() = file.nameWithoutExtension
    }

    data class Folder(
        override val file: File,
        val children: List<FileNode>,
        val isExpanded: Boolean = false
    ) : FileNode() {
        override val name: String get() = file.name
        val childCount: Int get() = children.filterIsInstance<Doc>().size
    }
}

enum class SyncState { LOCAL, SYNCED, SYNCING, CONFLICT }
