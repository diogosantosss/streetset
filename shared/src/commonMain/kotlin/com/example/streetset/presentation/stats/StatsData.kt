package com.example.streetset.presentation.stats

import com.example.streetset.data.dao.SessionLogDao
import com.example.streetset.data.entities.SessionLog
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus

const val HeatmapWeeks = 53
const val BarWeeks = 12

data class DayActivity(val sessions: Int, val seconds: Int)

data class WeekCount(val weekStart: LocalDate, val sessions: Int)

data class MixSlice(val protocol: TrainingProtocol, val sets: Int, val fraction: Float)

data class StatsData(
    val today: LocalDate,
    /** Monday of the first column of the heatmap. */
    val heatmapStart: LocalDate,
    val days: Map<LocalDate, DayActivity>,
    val streakWeeks: Int,
    val totalSessions: Int,
    val totalSeconds: Long,
    val weeks: List<WeekCount>,
    val mix: List<MixSlice>,
    val topExercises: List<SessionLogDao.ExerciseSets>,
)

/** Monday of the week this date is in. */
fun LocalDate.weekStart(): LocalDate = minus(DatePeriod(days = dayOfWeek.isoDayNumber - 1))

fun heatmapStart(today: LocalDate): LocalDate =
    today.weekStart().minus(DatePeriod(days = (HeatmapWeeks - 1) * 7))

fun buildStats(
    today: LocalDate,
    logs: List<SessionLog>,
    protocolSets: List<SessionLogDao.ProtocolSets>,
    topExercises: List<SessionLogDao.ExerciseSets>,
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

    val setsPerProtocol = protocolSets
        .groupBy { row -> TrainingProtocol.entries.firstOrNull { it.name == row.protocol } ?: TrainingProtocol.NORMAL }
        .mapValues { (_, rows) -> rows.sumOf { it.sets } }
    val totalSets = setsPerProtocol.values.sum()
    val mix = setsPerProtocol.entries
        .sortedByDescending { it.value }
        .map { MixSlice(it.key, it.value, if (totalSets > 0) it.value.toFloat() / totalSets else 0f) }

    return StatsData(
        today = today,
        heatmapStart = heatmapStart(today),
        days = days,
        streakWeeks = streak,
        totalSessions = logs.size,
        totalSeconds = logs.sumOf { it.durationSeconds.toLong() },
        weeks = weeks,
        mix = mix,
        topExercises = topExercises,
    )
}