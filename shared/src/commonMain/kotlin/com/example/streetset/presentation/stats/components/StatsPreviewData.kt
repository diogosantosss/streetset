package com.example.streetset.presentation.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.streetset.data.dao.SessionLogDao
import com.example.streetset.presentation.stats.BarWeeks
import com.example.streetset.presentation.stats.ConsistencyStats
import com.example.streetset.presentation.stats.DayActivity
import com.example.streetset.presentation.stats.HeatmapWeeks
import com.example.streetset.presentation.stats.StatsData
import com.example.streetset.presentation.stats.WeekCount
import com.example.streetset.presentation.stats.heatmapStart
import com.example.streetset.presentation.stats.weekStart
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@Composable
fun StatsPreviewSurface(content: @Composable () -> Unit) {
    AppTheme(theme = AppTheme.Dark) {
        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .background(LocalAppColors.current.background)
                .padding(16.dp),
        ) {
            content()
        }
    }
}

fun previewStats(empty: Boolean = false): StatsData {
    val today = LocalDate(2026, 11, 1)
    val start = heatmapStart(today)

    val days = if (empty) {
        emptyMap()
    } else {
        buildMap<LocalDate, DayActivity> {
            repeat(HeatmapWeeks * 7) { index ->
                val date = start.plus(DatePeriod(days = index))
                if (date > today) return@repeat

                // A made-up but stable pattern: some days with one session, a few with two
                val sessions = when ((index * 37) % 11) {
                    0, 1, 2 -> 1
                    3 -> 2
                    else -> 0
                }
                if (sessions > 0) {
                    // 15 to 55 minutes per session, so the heatmap shows every level
                    val perSessionSeconds = 900 + (index % 5) * 600
                    put(date, DayActivity(sessions = sessions, seconds = sessions * perSessionSeconds))
                }
            }
        }
    }

    val thisWeek = today.weekStart()
    val sessionsPerWeek = days.entries
        .groupBy { it.key.weekStart() }
        .mapValues { (_, entries) -> entries.sumOf { it.value.sessions } }
    val weeks = (BarWeeks - 1 downTo 0).map { back ->
        val weekStart = thisWeek.minus(DatePeriod(days = back * 7))
        WeekCount(weekStart, sessionsPerWeek[weekStart] ?: 0)
    }

    return StatsData(
        today = today,
        heatmapStart = start,
        days = days,
        streakWeeks = if (empty) 0 else 6,
        totalSessions = days.values.sumOf { it.sessions },
        totalSeconds = days.values.sumOf { it.seconds.toLong() },
        weeks = if (empty) weeks.map { it.copy(sessions = 0) } else weeks,
        consistency = if (empty) {
            ConsistencyStats(planned = 0, completed = 0)
        } else {
            ConsistencyStats(planned = 48, completed = 41)
        },
        history = if (empty) {
            emptyList()
        } else {
            listOf(
                SessionLogDao.HistoryEntry(
                    logId = 1,
                    sessionName = "Push Day",
                    completedAt = LocalDateTime(2026, 11, 1, 18, 30),
                    durationSeconds = 3_720,
                    notes = "Bench felt strong, added 2.5kg on the top set.",
                ),
                SessionLogDao.HistoryEntry(
                    logId = 2,
                    sessionName = "Pull Day",
                    completedAt = LocalDateTime(2026, 10, 30, 7, 15),
                    durationSeconds = 2_940,
                    notes = null,
                ),
                SessionLogDao.HistoryEntry(
                    logId = 3,
                    sessionName = "Leg Day",
                    completedAt = LocalDateTime(2026, 10, 28, 19, 0),
                    durationSeconds = 4_500,
                    notes = "Squat depth better. Left knee a bit sore after.",
                ),
            )
        },
    )
}

@Preview(showBackground = true, heightDp = 1500, name = "Stats - Content")
@Composable
private fun StatsContentPreview() {
    val stats = previewStats()
    StatsPreviewSurface {
        SummaryRow(stats)
        ActivitySection(stats)
        WeeksSection(stats.weeks)
        ConsistencySection(stats.consistency)
        HistorySection(stats.history, onLongClick = {})
    }
}

@Preview(showBackground = true, heightDp = 560, name = "Stats - Empty")
@Composable
private fun StatsEmptyPreview() {
    val stats = previewStats(empty = true)
    StatsPreviewSurface {
        SummaryRow(stats)
        ActivitySection(stats)
        EmptyStats()
    }
}

