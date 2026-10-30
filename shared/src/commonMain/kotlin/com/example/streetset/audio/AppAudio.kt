package com.example.streetset.audio

enum class AppSound(val fileName: String) {
    BEEP("beep.wav"),
    BEEP_HIGH("beep_high.wav"),
}

expect object AppAudio {
    fun init()
    fun play(sound: AppSound)
}
