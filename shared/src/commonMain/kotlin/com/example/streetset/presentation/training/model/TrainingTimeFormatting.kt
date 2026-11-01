package com.example.streetset.presentation.training.model

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Formats the moment a session started as a local wall-clock time (`HH:mm`).
 */
fun formatStartedAt(epochMillis: Long): String {
    if (epochMillis <= 0L) return "--:--"

    val time = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())

    val hour = time.hour.toString().padStart(2, '0')
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

/**
 * Formats a session duration as `mm:ss`.
 *
 * Minutes are not capped at 60, so longer sessions keep counting up naturally.
 */
fun formatDuration(totalSeconds: Long): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0L)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
