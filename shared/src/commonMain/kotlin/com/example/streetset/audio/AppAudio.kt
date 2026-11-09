package com.example.streetset.audio

enum class AppSound(val fileName: String) {
    BEEP("beep.wav"),
    BEEP_HIGH("beep_high.wav"),
}

expect object AppAudio {
    fun init()
    fun play(sound: AppSound)

    /** App level from 0f to 1f, applied on top of the system volume. */
    var volume: Float
}