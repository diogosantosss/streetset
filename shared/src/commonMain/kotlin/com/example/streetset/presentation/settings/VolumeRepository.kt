package com.example.streetset.presentation.settings

import com.example.streetset.audio.AppAudio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VolumeRepository {
    private val _volume = MutableStateFlow(100)
    val volume: StateFlow<Int> = _volume.asStateFlow()

    init {
        // TODO: load the saved value the same way ThemeRepository does
        apply(_volume.value)
    }

    fun set(percent: Int) {
        val value = percent.coerceIn(0, 100)
        _volume.value = value
        apply(value)
    }

    /** Called when the slider is released. */
    suspend fun save() {
        // TODO: persist _volume.value the same way ThemeRepository does
    }

    private fun apply(percent: Int) {
        AppAudio.volume = percent / 100f
    }
}