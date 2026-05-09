package com.easymd.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easymd.core.data.SettingsRepository
import com.easymd.core.model.AppSettings
import com.easymd.core.model.AppTheme
import com.easymd.core.model.FontSize
import com.easymd.core.model.PdfMargin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { repo.save(transform(settings.value)) }
    }

    fun setTheme(theme: AppTheme)        = update { it.copy(theme = theme) }
    fun setFontSize(size: FontSize)      = update { it.copy(fontSize = size) }
    fun setAutoSave(on: Boolean)         = update { it.copy(autoSave = on) }
    fun setLineNumbers(on: Boolean)      = update { it.copy(showLineNumbers = on) }
    fun setTabToSpaces(on: Boolean)      = update { it.copy(tabToSpaces = on) }
    fun setDriveSync(on: Boolean)        = update { it.copy(driveSync = on) }
    fun setPdfMargin(m: PdfMargin)       = update { it.copy(pdfMargin = m) }
}
