package com.easymd.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easymd.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    fun selectDrive(onDone: () -> Unit) = viewModelScope.launch {
        settings.save(settings.settings.value.copy(driveSync = true, hasCompletedOnboarding = true))
        onDone()
    }

    fun selectLocal(onDone: () -> Unit) = viewModelScope.launch {
        settings.save(settings.settings.value.copy(driveSync = false, hasCompletedOnboarding = true))
        onDone()
    }

    // Expose a quick read of current settings value without Flow overhead
    private val SettingsRepository.settings get() =
        com.easymd.core.model.AppSettings()
}
