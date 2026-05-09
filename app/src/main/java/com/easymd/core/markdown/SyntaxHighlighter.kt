package com.easymd.core.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

data class HighlightColors(
    val heading: Color,
    val bold: Color,
    val italic: Color,
    val code: Color,
    val codeBg: Color,
    val quote: Color,
    val link: Color,
    val marker: Color,
    val strikethrough: Color
)

object SyntaxHighlighter {

    private data class Rule(val regex: Regex, val style: (HighlightColors) -> SpanStyle)

    private val rules: List<Rule> = listOf(
        // Code block (``` ... ```)
        Rule(Regex("```[\\s\\S]*?```", RegexOption.MULTILINE)) { c ->
            SpanStyle(color = c.code, background = c.codeBg)
        },
        // Inline code
        Rule(Regex("`[^`\n]+`")) { c ->
            SpanStyle(color = c.code, background = c.codeBg)
        },
        // Headings H1–H6
        Rule(Regex("^#{1,6}\\s.+", RegexOption.MULTILINE)) { c ->
            SpanStyle(color = c.heading, fontWeight = FontWeight.Bold)
        },
        // Bold **text** or __text__
        Rule(Regex("\\*\\*[^*]+\\*\\*|__[^_]+__")) { c ->
            SpanStyle(color = c.bold, fontWeight = FontWeight.Bold)
        },
        // Italic *text* or _text_
        Rule(Regex("(?<![*_])\\*[^*\n]+\\*(?![*])|(?<![*_])_[^_\n]+_(?![_])")) { c ->
            SpanStyle(color = c.italic, fontStyle = FontStyle.Italic)
        },
        // Strikethrough ~~text~~
        Rule(Regex("~~[^~]+~~")) { c ->
            SpanStyle(color = c.strikethrough, textDecoration = TextDecoration.LineThrough)
        },
        // Link [text](url)
        Rule(Regex("\\[([^\\]]+)]\\([^)]+\\)")) { c ->
            SpanStyle(color = c.link, textDecoration = TextDecoration.Underline)
        },
        // Blockquote line
        Rule(Regex("^>\\s.+", RegexOption.MULTILINE)) { c ->
            SpanStyle(color = c.quote, fontStyle = FontStyle.Italic)
        },
        // List markers (-, *, +, numbers)
        Rule(Regex("^([-*+]|\\d+\\.)\\s", RegexOption.MULTILINE)) { c ->
            SpanStyle(color = c.marker)
        },
        // Task list checkboxes
        Rule(Regex("^- \\[[xX ]]\\s", RegexOption.MULTILINE)) { c ->
            SpanStyle(color = c.marker)
        },
        // HR ---
        Rule(Regex("^---+$", RegexOption.MULTILINE)) { c ->
            SpanStyle(color = c.marker)
        }
    )

    fun highlight(text: String, colors: HighlightColors): AnnotatedString = buildAnnotatedString {
        append(text)
        rules.forEach { rule ->
            rule.regex.findAll(text).forEach { match ->
                addStyle(rule.style(colors), match.range.first, match.range.last + 1)
            }
        }
    }
}
