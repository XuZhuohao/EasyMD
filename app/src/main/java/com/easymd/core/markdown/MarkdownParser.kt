package com.easymd.core.markdown

import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import javax.inject.Inject
import javax.inject.Singleton

interface MarkdownParser {
    fun toHtml(markdown: String, darkTheme: Boolean = false): String
}

@Singleton
class CommonmarkParser @Inject constructor() : MarkdownParser {

    private val extensions = listOf(
        TablesExtension.create(),
        StrikethroughExtension.create(),
        TaskListItemsExtension.create(),
        AutolinkExtension.create()
    )

    private val parser: Parser = Parser.builder()
        .extensions(extensions)
        .build()

    private val renderer: HtmlRenderer = HtmlRenderer.builder()
        .extensions(extensions)
        .sanitizeUrls(false)
        .build()

    override fun toHtml(markdown: String, darkTheme: Boolean): String {
        val body = renderer.render(parser.parse(markdown))
        val theme = if (darkTheme) "dark" else "light"
        return buildHtmlPage(body, theme)
    }

    private fun buildHtmlPage(body: String, theme: String): String = """
<!DOCTYPE html>
<html data-theme="$theme">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
:root {
  --bg: #ffffff; --fg: #191C20; --fg2: #41484D;
  --accent: #1A73E8; --code-bg: #E4E9F4; --code-fg: #C62828;
  --quote-border: #1A73E8; --quote-bg: rgba(26,115,232,0.06);
  --table-border: #DDE3EA; --table-head-bg: #ECF0F9;
  --hr-color: #DDE3EA; --link: #1A73E8;
}
[data-theme=dark] {
  --bg: #111418; --fg: #E2E2E6; --fg2: #8B9197;
  --accent: #9ECBFF; --code-bg: #1E2330; --code-fg: #F28B82;
  --quote-border: #9ECBFF; --quote-bg: rgba(158,203,255,0.08);
  --table-border: #41484D; --table-head-bg: #1A1E27;
  --hr-color: #41484D; --link: #9ECBFF;
}
* { box-sizing: border-box; margin: 0; padding: 0; }
body { background: var(--bg); color: var(--fg); font-family: 'Google Sans', Roboto, sans-serif; font-size: 16px; line-height: 1.7; padding: 20px 20px 40px; }
h1,h2,h3,h4,h5,h6 { color: var(--fg); font-weight: 600; margin: 1.2em 0 0.5em; line-height: 1.3; }
h1 { font-size: 1.75em; border-bottom: 1px solid var(--hr-color); padding-bottom: 0.3em; }
h2 { font-size: 1.35em; }
h3 { font-size: 1.15em; }
p  { margin-bottom: 0.9em; }
a  { color: var(--link); text-decoration: none; }
a:hover { text-decoration: underline; }
strong { font-weight: 700; }
em     { font-style: italic; }
del    { text-decoration: line-through; color: var(--fg2); }
code {
  background: var(--code-bg); color: var(--code-fg);
  font-family: 'Roboto Mono', monospace; font-size: 0.875em;
  padding: 1px 6px; border-radius: 4px;
}
pre {
  background: var(--code-bg); border-radius: 10px;
  padding: 16px; overflow-x: auto; margin: 1em 0;
}
pre code { background: none; padding: 0; color: var(--fg); font-size: 0.875em; }
blockquote {
  border-left: 3px solid var(--quote-border);
  background: var(--quote-bg);
  padding: 10px 14px; margin: 1em 0;
  border-radius: 0 8px 8px 0; color: var(--accent);
  font-style: italic;
}
ul,ol { padding-left: 1.5em; margin-bottom: 0.9em; }
li { margin-bottom: 0.25em; }
li input[type=checkbox] { margin-right: 0.4em; accent-color: var(--accent); }
hr { border: none; border-top: 1px solid var(--hr-color); margin: 1.5em 0; }
table { border-collapse: collapse; width: 100%; margin: 1em 0; }
th,td { border: 1px solid var(--table-border); padding: 8px 12px; text-align: left; }
th { background: var(--table-head-bg); font-weight: 600; }
img { max-width: 100%; border-radius: 8px; }
</style>
</head>
<body>$body</body>
</html>
    """.trimIndent()
}
