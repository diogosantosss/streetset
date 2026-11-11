package com.example.streetset.presentation.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Lucide
import com.example.streetset.presentation.calendar.displayName
import com.example.streetset.presentation.calendar.fullTitle
import com.example.streetset.presentation.stats.DayActivity
import com.example.streetset.presentation.stats.HeatmapWeeks
import com.example.streetset.presentation.stats.StatsData
import com.example.streetset.ui.theme.AppColors
import com.example.streetset.ui.theme.LocalAppColors
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private val CellSize = 18.dp
private val CellGap = 4.dp
private val MonthLabelHeight = 20.dp
private val WeekdayLabelWidth = 34.dp

/** Seconds per heatmap step: 20, 40 and 60 minutes, then anything above. */
private const val LevelStepSeconds = 20 * 60

@Composable
internal fun ActivitySection(stats: StatsData) {
    var selected by remember { mutableStateOf<LocalDate?>(null) }

    Column {
        SectionCaption("ACTIVITY")
        Spacer(modifier = Modifier.height(12.dp))

        Heatmap(
            stats = stats,
            selected = selected,
            onSelect = { date -> selected = if (selected == date) null else date },
        )

        Spacer(modifier = Modifier.height(10.dp))
        HeatmapLegend()

        selected?.let { date ->
            SelectedDay(date = date, activity = stats.days[date])
        }
    }
}

@Composable
private fun Heatmap(
    stats: StatsData,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
) {
    val colors = LocalAppColors.current
    val scrollState = rememberScrollState()
    var positioned by remember { mutableStateOf(false) }

    // Opens on the most recent weeks
    LaunchedEffect(scrollState.maxValue) {
        if (!positioned && scrollState.maxValue > 0) {
            scrollState.scrollTo(scrollState.maxValue)
            positioned = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val pitch = CellSize + CellGap
        // Only whole columns fit in the viewport, so the leftmost one is never cut in half
        val visibleColumns = ((maxWidth - WeekdayLabelWidth) / pitch).toInt().coerceIn(1, HeatmapWeeks)
        val gridWidth = pitch * visibleColumns

        Row {
            // Fixed weekday labels, with the same row height and gap as the cells
            Column(
                verticalArrangement = Arrangement.spacedBy(CellGap),
                modifier = Modifier
                    .width(WeekdayLabelWidth)
                    .padding(top = MonthLabelHeight),
            ) {
                listOf("Mon", "", "Wed", "", "Fri", "", "").forEach { label ->
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.height(CellSize),
                    ) {
                        Text(label, fontSize = 11.sp, color = colors.textTertiary, maxLines = 1)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .width(gridWidth)
                    .horizontalScroll(scrollState),
            ) {
                // Month labels, where the month changes
                Row(modifier = Modifier.height(MonthLabelHeight)) {
                    repeat(HeatmapWeeks) { week ->
                        val start = stats.heatmapStart.plus(DatePeriod(days = week * 7))
                        val previous = if (week == 0) null else stats.heatmapStart.plus(DatePeriod(days = (week - 1) * 7))
                        val changed = if (previous == null) start.day <= 7 else start.month != previous.month
                        // The last two columns have no room for the name to the right
                        val showLabel = changed && week < HeatmapWeeks - 2

                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .width(pitch)
                                .height(MonthLabelHeight),
                        ) {
                            if (showLabel) {
                                Text(
                                    text = start.month.displayName().take(3),
                                    fontSize = 11.sp,
                                    color = colors.textTertiary,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.wrapContentWidth(align = Alignment.Start, unbounded = true),
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(CellGap)) {
                    repeat(HeatmapWeeks) { week ->
                        Column(verticalArrangement = Arrangement.spacedBy(CellGap)) {
                            repeat(7) { day ->
                                val date = stats.heatmapStart.plus(DatePeriod(days = week * 7 + day))
                                HeatCell(
                                    date = date,
                                    today = stats.today,
                                    activity = stats.days[date],
                                    isSelected = date == selected,
                                    onClick = { onSelect(date) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatCell(
    date: LocalDate,
    today: LocalDate,
    activity: DayActivity?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val isFuture = date > today
    val shape = RoundedCornerShape(4.dp)
    val level = levelFor(activity?.seconds ?: 0)

    val fill = when {
        isFuture -> Color.Transparent
        else -> levelFill(level, colors)
    }
    val border = when {
        isSelected -> Modifier.border(1.5.dp, colors.textPrimary, shape)
        date == today -> Modifier.border(1.dp, colors.textSecondary, shape)
        else -> Modifier
    }

    Box(
        modifier = Modifier
            .size(CellSize)
            .clip(shape)
            .background(fill)
            .then(border)
            .clickable(enabled = !isFuture, onClick = onClick),
    )
}

/** 0 when there was no training, otherwise 1..4 for the 20/40/60 minute buckets. */
private fun levelFor(seconds: Int): Int = when {
    seconds <= 0 -> 0
    seconds <= LevelStepSeconds -> 1
    seconds <= LevelStepSeconds * 2 -> 2
    seconds <= LevelStepSeconds * 3 -> 3
    else -> 4
}

private fun levelFill(level: Int, colors: AppColors): Color =
    if (level <= 0) colors.textTertiary.copy(alpha = 0.12f)
    else colors.accent.copy(alpha = 0.25f + 0.25f * (level - 1))

private val LevelLabels = listOf("0", "20m", "40m", "60m", "60m+")

@Composable
private fun HeatmapLegend() {
    val colors = LocalAppColors.current

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LevelLabels.forEachIndexed { level, label ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(44.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(levelFill(level, colors)),
                )
                Text(
                    text = label,
                    fontSize = 9.sp,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun SelectedDay(date: LocalDate, activity: DayActivity?) {
    val colors = LocalAppColors.current

    Column(modifier = Modifier.padding(start = 4.dp, top = 16.dp)) {
        Text(
            text = date.fullTitle(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
        )
        if (activity == null) {
            Text(
                text = "No sessions",
                fontSize = 13.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                IconLabel(
                    icon = Lucide.Dumbbell,
                    text = "${activity.sessions} ${if (activity.sessions == 1) "session" else "sessions"}",
                )
                IconLabel(
                    icon = Lucide.Clock,
                    text = formatDuration(activity.seconds.toLong()),
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 360, name = "Stats - Activity")
@Composable
private fun StatsActivityPreview() {
    StatsPreviewSurface { ActivitySection(previewStats()) }
}