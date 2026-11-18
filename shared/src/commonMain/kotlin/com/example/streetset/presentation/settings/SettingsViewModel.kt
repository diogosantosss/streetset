package com.example.streetset.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.streetset.data.preferences.AppInfo
import com.example.streetset.ui.theme.AppTheme
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val keepScreenOnRepository: KeepScreenOnRepository,
    val appInfo: AppInfo,
) : ViewModel() {

    val theme: StateFlow<AppTheme> = themeRepository.theme
    val keepScreenOn: StateFlow<Boolean> = keepScreenOnRepository.enabled

    fun selectTheme(theme: AppTheme) {
        themeRepository.setTheme(theme)
    }

    fun setKeepScreenOn(enabled: Boolean) {
        keepScreenOnRepository.set(enabled)
    }
}
