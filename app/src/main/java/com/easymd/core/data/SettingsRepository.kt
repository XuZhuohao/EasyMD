package com.easymd.core.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.easymd.core.model.AppSettings
import com.easymd.core.model.AppTheme
import com.easymd.core.model.FontSize
import com.easymd.core.model.PdfMargin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "easymd_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME            = stringPreferencesKey("theme")
        val FONT_SIZE        = stringPreferencesKey("font_size")
        val AUTO_SAVE        = booleanPreferencesKey("auto_save")
        val LINE_NUMBERS     = booleanPreferencesKey("line_numbers")
        val TAB_SPACES       = booleanPreferencesKey("tab_spaces")
        val TAB_WIDTH        = intPreferencesKey("tab_width")
        val DRIVE_SYNC       = booleanPreferencesKey("drive_sync")
        val PDF_MARGIN       = stringPreferencesKey("pdf_margin")
        val ONBOARDING_DONE  = booleanPreferencesKey("onboarding_done")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            theme           = AppTheme.valueOf(prefs[Keys.THEME]       ?: AppTheme.SYSTEM.name),
            fontSize        = FontSize.valueOf(prefs[Keys.FONT_SIZE]   ?: FontSize.NORMAL.name),
            autoSave        = prefs[Keys.AUTO_SAVE]  ?: true,
            showLineNumbers = prefs[Keys.LINE_NUMBERS] ?: true,
            tabToSpaces     = prefs[Keys.TAB_SPACES] ?: true,
            tabWidth        = prefs[Keys.TAB_WIDTH]  ?: 4,
            driveSync       = prefs[Keys.DRIVE_SYNC] ?: false,
            pdfMargin       = PdfMargin.valueOf(prefs[Keys.PDF_MARGIN] ?: PdfMargin.STANDARD.name),
            hasCompletedOnboarding = prefs[Keys.ONBOARDING_DONE] ?: false
        )
    }

    suspend fun save(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME]           = settings.theme.name
            prefs[Keys.FONT_SIZE]       = settings.fontSize.name
            prefs[Keys.AUTO_SAVE]       = settings.autoSave
            prefs[Keys.LINE_NUMBERS]    = settings.showLineNumbers
            prefs[Keys.TAB_SPACES]      = settings.tabToSpaces
            prefs[Keys.TAB_WIDTH]       = settings.tabWidth
            prefs[Keys.DRIVE_SYNC]      = settings.driveSync
            prefs[Keys.PDF_MARGIN]      = settings.pdfMargin.name
            prefs[Keys.ONBOARDING_DONE] = settings.hasCompletedOnboarding
        }
    }
}
