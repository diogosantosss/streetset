package com.example.streetset.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.streetset.data.preferences.AppInfo
import com.example.streetset.ui.theme.AppTheme
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    val appInfo: AppInfo,
) : ViewModel() {

    val theme: StateFlow<AppTheme> = themeRepository.theme

    fun selectTheme(theme: AppTheme) {
        themeRepository.setTheme(theme)
    }
}
