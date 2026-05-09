package com.easymd.core.model

import java.io.File
import java.time.Instant

data class Document(
    val file: File,
    val content: String,
    val modifiedAt: Instant = Instant.now(),
    val isDirty: Boolean = false
) {
    val title: String get() = file.nameWithoutExtension.ifBlank { "无标题" }
    val wordCount: WordCount get() = WordCount.of(content)
}

data class WordCount(val words: Int, val characters: Int) {
    val readingMinutes: Int get() = maxOf(1, (words / 300.0).toInt() + if (words % 300 > 0) 1 else 0)

    companion object {
        fun of(text: String): WordCount {
            if (text.isBlank()) return WordCount(0, 0)
            val chars = text.replace("\n", "").length
            // Count CJK characters as individual words, split remaining by whitespace
            val cjkCount = text.count { it.code in 0x4E00..0x9FFF || it.code in 0x3040..0x30FF }
            val latinWords = text.replace(Regex("[\\u4E00-\\u9FFF\\u3040-\\u30FF]"), " ")
                .trim().split(Regex("\\s+")).count { it.isNotBlank() }
            return WordCount(cjkCount + latinWords, chars)
        }
    }
}
