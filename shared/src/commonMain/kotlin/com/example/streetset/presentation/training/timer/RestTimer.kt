package com.example.streetset.presentation.training

import com.example.streetset.audio.CountdownAudio
import com.example.streetset.util.Time
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

class RestTimer {
    private val audio = CountdownAudio()

    fun countdown(restEndsAtMillis: Long): Flow<Long> = flow {
        audio.reset()
        while (true) {
            val remaining = restEndsAtMillis - Time.nowMillis()
            if (remaining <= 0) {
                audio.onTick(0L)
                emit(0)
                break
            }

            val secondsLeft = (remaining + 999L) / 1000L
            emit(secondsLeft)
            audio.onTick(secondsLeft)

            val delayMs = if (secondsLeft == 1L) {
                remaining.coerceAtLeast(50)
            } else {
                200L
            }
            delay(delayMs.milliseconds)
        }
    }
}