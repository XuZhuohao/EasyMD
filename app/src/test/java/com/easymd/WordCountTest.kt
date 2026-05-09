package com.easymd

import com.easymd.core.model.WordCount
import org.junit.Assert.assertEquals
import org.junit.Test

class WordCountTest {

    @Test fun `english word count`() {
        val wc = WordCount.of("Hello world foo bar")
        assertEquals(4, wc.words)
        assertEquals(15, wc.characters)
    }

    @Test fun `chinese character count`() {
        val wc = WordCount.of("你好世界")
        assertEquals(4, wc.words)
    }

    @Test fun `mixed content`() {
        val wc = WordCount.of("你好 hello 世界 world")
        // 2 CJK + 2 latin
        assertEquals(4, wc.words)
    }

    @Test fun `empty string`() {
        val wc = WordCount.of("")
        assertEquals(0, wc.words)
        assertEquals(0, wc.characters)
    }

    @Test fun `reading time minimum 1 min`() {
        val wc = WordCount.of("hi")
        assertEquals(1, wc.readingMinutes)
    }

    @Test fun `300 words is 1 minute`() {
        val text = (1..300).joinToString(" ") { "word$it" }
        val wc = WordCount.of(text)
        assertEquals(1, wc.readingMinutes)
    }
}
