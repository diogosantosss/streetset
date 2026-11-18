package com.example.streetset.presentation.settings

import com.example.streetset.data.preferences.ScreenOnPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KeepScreenOnRepository(
    private val preferences: ScreenOnPreferences,
) {
    private val _enabled = MutableStateFlow(preferences.read())
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun set(enabled: Boolean) {
        if (_enabled.value == enabled) return
        _enabled.value = enabled
        preferences.save(enabled)
    }
}
