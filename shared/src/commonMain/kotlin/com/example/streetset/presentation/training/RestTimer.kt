package com.example.streetset.presentation.training

import com.example.streetset.util.Time
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

class RestTimer {
    fun countdown(restEndsAtMillis: Long): Flow<Long> = flow {
        while (true) {
            val remaining = restEndsAtMillis - Time.nowMillis()
            if (remaining <= 0) {
                emit(0)
                break
            }
            val secondsLeft = ((remaining + 999L) / 1000L)
            emit(secondsLeft)
            if (secondsLeft == 1L) {
                delay(remaining.coerceAtLeast(50).milliseconds)
            } else {
                delay(200.milliseconds)
            }
        }
    }
}
