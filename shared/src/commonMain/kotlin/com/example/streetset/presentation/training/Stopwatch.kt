package com.example.streetset.presentation.training

import com.example.streetset.util.Time
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

class Stopwatch {
    private var running = false
    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L

    fun start(): Flow<Pair<Long, Long>> = flow {
        running = true
        startTime = Time.nowMillis() - elapsedTime
        while (running) {
            val currentTime = Time.nowMillis() - startTime
            val totalSeconds = currentTime / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            emit(Pair(minutes, seconds))
            delay(1000.milliseconds)
        }
    }

    fun stop() {
        if (!running) return
        running = false
        elapsedTime = Time.nowMillis() - startTime
    }

    /** Elapsed time in milliseconds, whether the stopwatch is running or not. */
    fun elapsedMillis(): Long =
        if (running) Time.nowMillis() - startTime else elapsedTime

    fun reset() {
        running = false
        elapsedTime = 0L
    }
}