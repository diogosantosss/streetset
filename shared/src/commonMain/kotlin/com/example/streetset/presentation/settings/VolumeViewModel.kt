package com.example.streetset.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.audio.AppAudio
import com.example.streetset.audio.AppSound
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VolumeViewModel : ViewModel(), KoinComponent {
    private val repository: VolumeRepository by inject()

    val volume: StateFlow<Int> = repository.volume

    fun setVolume(percent: Int) = repository.set(percent)

    /** The slider was released: saves the level and plays a beep so it can be heard. */
    fun commit() {
        viewModelScope.launch { repository.save() }
        AppAudio.play(AppSound.BEEP)
    }
}