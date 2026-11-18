package com.example.streetset.presentation.settings

import com.example.streetset.data.preferences.ThemePreferences
import com.example.streetset.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepository(
    private val preferences: ThemePreferences,
) {
    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        if (_theme.value == theme) return
        _theme.value = theme
        preferences.saveTheme(theme.name)
    }

    private fun loadTheme(): AppTheme {
        val saved = preferences.readTheme() ?: return AppTheme.Graphite
        AppTheme.entries.firstOrNull { it.name == saved }?.let { return it }
        // Older builds stored System/Light/Dark, or one of the eight themed variants
        // (LightWarm, DarkGreen, ...). Fold them into the closest new palette.
        return if (saved.startsWith("Light")) AppTheme.Paper else AppTheme.Graphite
    }
}
