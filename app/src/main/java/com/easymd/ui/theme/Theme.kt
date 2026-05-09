package com.easymd.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.easymd.core.markdown.HighlightColors
import com.easymd.core.model.AppTheme

private val LightColorScheme = lightColorScheme(
    primary             = md_light_primary,
    onPrimary           = md_light_onPrimary,
    primaryContainer    = md_light_primaryContainer,
    onPrimaryContainer  = md_light_onPrimaryContainer,
    secondary           = md_light_secondary,
    onSecondary         = md_light_onSecondary,
    secondaryContainer  = md_light_secondaryContainer,
    onSecondaryContainer= md_light_onSecondaryContainer,
    tertiary            = md_light_tertiary,
    onTertiary          = md_light_onTertiary,
    tertiaryContainer   = md_light_tertiaryContainer,
    onTertiaryContainer = md_light_onTertiaryContainer,
    error               = md_light_error,
    errorContainer      = md_light_errorContainer,
    background          = md_light_background,
    onBackground        = md_light_onBackground,
    surface             = md_light_surface,
    onSurface           = md_light_onSurface,
    surfaceVariant      = md_light_surfaceVariant,
    onSurfaceVariant    = md_light_onSurfaceVariant,
    outline             = md_light_outline,
    outlineVariant      = md_light_outlineVariant,
    inverseSurface      = md_light_inverseSurface,
    inverseOnSurface    = md_light_inverseOnSurface,
)

private val DarkColorScheme = darkColorScheme(
    primary             = md_dark_primary,
    onPrimary           = md_dark_onPrimary,
    primaryContainer    = md_dark_primaryContainer,
    onPrimaryContainer  = md_dark_onPrimaryContainer,
    secondary           = md_dark_secondary,
    onSecondary         = md_dark_onSecondary,
    secondaryContainer  = md_dark_secondaryContainer,
    onSecondaryContainer= md_dark_onSecondaryContainer,
    tertiary            = md_dark_tertiary,
    onTertiary          = md_dark_onTertiary,
    tertiaryContainer   = md_dark_tertiaryContainer,
    onTertiaryContainer = md_dark_onTertiaryContainer,
    error               = md_dark_error,
    errorContainer      = md_dark_errorContainer,
    background          = md_dark_background,
    onBackground        = md_dark_onBackground,
    surface             = md_dark_surface,
    onSurface           = md_dark_onSurface,
    surfaceVariant      = md_dark_surfaceVariant,
    onSurfaceVariant    = md_dark_onSurfaceVariant,
    outline             = md_dark_outline,
    outlineVariant      = md_dark_outlineVariant,
    inverseSurface      = md_dark_inverseSurface,
    inverseOnSurface    = md_dark_inverseOnSurface,
)

val LocalHighlightColors = compositionLocalOf {
    HighlightColors(
        heading       = SyntaxHeadingLight,
        bold          = Color(0xFF191C20),
        italic        = SyntaxItalicLight,
        code          = SyntaxCodeLight,
        codeBg        = SyntaxCodeBgLight,
        quote         = SyntaxQuoteLight,
        link          = SyntaxLinkLight,
        marker        = SyntaxMarkerLight,
        strikethrough = Color(0xFF71787E)
    )
}

@Composable
fun EasyMDTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (appTheme) {
        AppTheme.DARK   -> true
        AppTheme.LIGHT  -> false
        AppTheme.SYSTEM -> systemDark
    }

    // Dynamic color (Android 12+)
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        dark  -> DarkColorScheme
        else  -> LightColorScheme
    }

    val highlightColors = if (dark) HighlightColors(
        heading       = SyntaxHeadingDark,
        bold          = Color(0xFFE2E2E6),
        italic        = SyntaxItalicDark,
        code          = SyntaxCodeDark,
        codeBg        = SyntaxCodeBgDark,
        quote         = SyntaxQuoteDark,
        link          = SyntaxLinkDark,
        marker        = SyntaxMarkerDark,
        strikethrough = Color(0xFF8B9197)
    ) else HighlightColors(
        heading       = SyntaxHeadingLight,
        bold          = Color(0xFF191C20),
        italic        = SyntaxItalicLight,
        code          = SyntaxCodeLight,
        codeBg        = SyntaxCodeBgLight,
        quote         = SyntaxQuoteLight,
        link          = SyntaxLinkLight,
        marker        = SyntaxMarkerLight,
        strikethrough = Color(0xFF71787E)
    )

    CompositionLocalProvider(LocalHighlightColors provides highlightColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = AppTypography,
            content     = content
        )
    }
}
