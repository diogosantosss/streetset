package com.example.streetset.audio

class CountdownAudio {
    private var lastTick: Long = -1L
    private var previousLeft: Long = -1L

    fun onTick(left: Long) {
        if (left < 0) return

        if (previousLeft == 1L && left == 0L) {
            AppAudio.play(AppSound.BEEP_HIGH)
        } else if (left in 1..3 && left != lastTick) {
            lastTick = left
            AppAudio.play(AppSound.BEEP)
        }

        previousLeft = left

        if (left == 0L) {
            reset()
        }
    }

    fun reset() {
        lastTick = -1L
        previousLeft = -1L
    }
}