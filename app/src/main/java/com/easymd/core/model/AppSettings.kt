package com.easymd.core.model

enum class AppTheme { SYSTEM, LIGHT, DARK }
enum class FontSize(val sp: Int) { SMALL(13), NORMAL(15), LARGE(17), XLARGE(19), XXLARGE(21) }
enum class PdfMargin { NARROW, STANDARD, WIDE }
enum class EditorTab { FILES, EDITOR, PREVIEW, SETTINGS }

data class AppSettings(
    val theme: AppTheme    = AppTheme.SYSTEM,
    val fontSize: FontSize = FontSize.NORMAL,
    val autoSave: Boolean  = true,
    val showLineNumbers: Boolean = true,
    val tabToSpaces: Boolean = true,
    val tabWidth: Int      = 4,
    val driveSync: Boolean = false,
    val pdfMargin: PdfMargin = PdfMargin.STANDARD,
    val hasCompletedOnboarding: Boolean = false
)
