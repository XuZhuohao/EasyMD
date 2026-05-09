package com.easymd.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easymd.core.data.SettingsRepository
import com.easymd.core.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    fun selectDrive(onDone: () -> Unit) = viewModelScope.launch {
        settingsRepo.save(AppSettings(driveSync = true, hasCompletedOnboarding = true))
        onDone()
    }

    fun selectLocal(onDone: () -> Unit) = viewModelScope.launch {
        settingsRepo.save(AppSettings(driveSync = false, hasCompletedOnboarding = true))
        onDone()
    }
}
