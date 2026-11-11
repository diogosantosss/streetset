package com.example.streetset.presentation.stats

import com.example.streetset.data.dao.Consistency
import com.example.streetset.data.dao.SessionLogDao
import com.example.streetset.data.entities.SessionLog
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus

const val HeatmapWeeks = 53
const val BarWeeks = 12

data class DayActivity(val sessions: Int, val seconds: Int)

data class WeekCount(val weekStart: LocalDate, val sessions: Int)

/** How many of the sessions planned up to yesterday were actually completed. */
data class ConsistencyStats(val planned: Int, val completed: Int) {
    val missed: Int get() = (planned - completed).coerceAtLeast(0)
    val rate: Float get() = if (planned > 0) completed.toFloat() / planned else 0f
}

data class StatsData(
    val today: LocalDate,
    /** Monday of the first column of the heatmap. */
    val heatmapStart: LocalDate,
    val days: Map<LocalDate, DayActivity>,
    val streakWeeks: Int,
    val totalSessions: Int,
    val totalSeconds: Long,
    val weeks: List<WeekCount>,
    val consistency: ConsistencyStats,
    val history: List<SessionLogDao.HistoryEntry>,
)

/** Monday of the week this date is in. */
fun LocalDate.weekStart(): LocalDate = minus(DatePeriod(days = dayOfWeek.isoDayNumber - 1))

fun heatmapStart(today: LocalDate): LocalDate =
    today.weekStart().minus(DatePeriod(days = (HeatmapWeeks - 1) * 7))

fun buildStats(
    today: LocalDate,
    logs: List<SessionLog>,
    consistency: Consistency,
    history: List<SessionLogDao.HistoryEntry>,
): StatsData {
    val days = logs
        .groupBy { it.completedAt.date }
        .mapValues { (_, list) -> DayActivity(sessions = list.size, seconds = list.sumOf { it.durationSeconds }) }

    val sessionsPerWeek = days.entries
        .groupBy { it.key.weekStart() }
        .mapValues { (_, entries) -> entries.sumOf { it.value.sessions } }

    val thisWeek = today.weekStart()

    var cursor = if (thisWeek in sessionsPerWeek) thisWeek else thisWeek.minus(DatePeriod(days = 7))
    var streak = 0
    while (cursor in sessionsPerWeek) {
        streak++
        cursor = cursor.minus(DatePeriod(days = 7))
    }

    val weeks = (BarWeeks - 1 downTo 0).map { back ->
        val start = thisWeek.minus(DatePeriod(days = back * 7))
        WeekCount(start, sessionsPerWeek[start] ?: 0)
    }

    return StatsData(
        today = today,
        heatmapStart = heatmapStart(today),
        days = days,
        streakWeeks = streak,
        totalSessions = logs.size,
        totalSeconds = logs.sumOf { it.durationSeconds.toLong() },
        weeks = weeks,
        consistency = ConsistencyStats(planned = consistency.planned, completed = consistency.completed),
        history = history,
    )
}