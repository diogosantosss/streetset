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
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.presentation.stats.BarWeeks
import com.example.streetset.presentation.stats.DayActivity
import com.example.streetset.presentation.stats.HeatmapWeeks
import com.example.streetset.presentation.stats.MixSlice
import com.example.streetset.presentation.stats.StatsData
import com.example.streetset.presentation.stats.WeekCount
import com.example.streetset.presentation.stats.heatmapStart
import com.example.streetset.presentation.stats.weekStart
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
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
                    put(date, DayActivity(sessions = sessions, seconds = sessions * 3_600 + (index % 4) * 900))
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
        mix = if (empty) {
            emptyList()
        } else {
            listOf(
                MixSlice(TrainingProtocol.NORMAL, sets = 48, fraction = 0.40f),
                MixSlice(TrainingProtocol.BACK_OFF, sets = 30, fraction = 0.25f),
                MixSlice(TrainingProtocol.TOP_SET, sets = 18, fraction = 0.15f),
                MixSlice(TrainingProtocol.WARM_UP, sets = 14, fraction = 0.12f),
                MixSlice(TrainingProtocol.CIRCUIT, sets = 10, fraction = 0.08f),
            )
        },
        topExercises = if (empty) {
            emptyList()
        } else {
            listOf(
                SessionLogDao.ExerciseSets("Back Squat", 24),
                SessionLogDao.ExerciseSets("Bench Press", 20),
                SessionLogDao.ExerciseSets("Pull-ups", 18),
                SessionLogDao.ExerciseSets("Romanian Deadlift", 12),
                SessionLogDao.ExerciseSets("Overhead Press", 9),
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
        MixSection(stats.mix)
        TopExercisesSection(stats.topExercises)
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

