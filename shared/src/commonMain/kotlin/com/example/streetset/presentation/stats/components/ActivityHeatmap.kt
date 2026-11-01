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
import com.example.streetset.ui.theme.LocalAppColors
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.ceil

private val CellSize = 18.dp
private val CellGap = 4.dp
private val MonthLabelHeight = 20.dp
private val WeekdayLabelWidth = 34.dp

private enum class HeatmapMetric(val label: String) {
    Sessions("Sessions"),
    Time("Time");

    fun valueOf(day: DayActivity): Int = when (this) {
        Sessions -> day.sessions
        Time -> day.seconds
    }
}

@Composable
internal fun ActivitySection(stats: StatsData) {
    var metric by remember { mutableStateOf(HeatmapMetric.Sessions) }
    var selected by remember { mutableStateOf<LocalDate?>(null) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionCaption("ACTIVITY", modifier = Modifier.weight(1f))
            MetricSwitch(selected = metric, onSelect = { metric = it })
        }
        Spacer(modifier = Modifier.height(12.dp))

        Heatmap(
            stats = stats,
            metric = metric,
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
private fun MetricSwitch(selected: HeatmapMetric, onSelect: (HeatmapMetric) -> Unit) {
    val colors = LocalAppColors.current

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        HeatmapMetric.entries.forEach { metric ->
            val isSelected = metric == selected
            Text(
                text = metric.label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) colors.accent else colors.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) colors.accent.copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { onSelect(metric) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun Heatmap(
    stats: StatsData,
    metric: HeatmapMetric,
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

    val maxValue = remember(stats, metric) { stats.days.values.maxOfOrNull { metric.valueOf(it) } ?: 0 }

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
                                    metric = metric,
                                    maxValue = maxValue,
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
    metric: HeatmapMetric,
    maxValue: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val isFuture = date > today
    val shape = RoundedCornerShape(4.dp)
    val level = if (activity == null) 0 else levelFor(metric.valueOf(activity), maxValue)

    val fill = when {
        isFuture -> Color.Transparent
        level == 0 -> colors.textTertiary.copy(alpha = 0.12f)
        else -> colors.accent.copy(alpha = 0.25f + 0.25f * (level - 1))
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

/** 0 when there is no activity, otherwise 1 to 4 relative to the busiest day. */
private fun levelFor(value: Int, max: Int): Int {
    if (value <= 0 || max <= 0) return 0
    return ceil(value.toFloat() / max * 4).toInt().coerceIn(1, 4)
}

@Composable
private fun HeatmapLegend() {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text("Less", fontSize = 10.sp, color = colors.textTertiary)
        repeat(5) { level ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (level == 0) colors.textTertiary.copy(alpha = 0.12f)
                        else colors.accent.copy(alpha = 0.25f + 0.25f * (level - 1)),
                    ),
            )
        }
        Text("More", fontSize = 10.sp, color = colors.textTertiary)
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