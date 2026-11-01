package com.example.streetset.presentation.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Lucide
import com.example.streetset.presentation.stats.StatsData
import com.example.streetset.ui.theme.LocalAppColors

@Composable
internal fun SummaryRow(stats: StatsData) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
    ) {
        SummaryStat(
            icon = Lucide.Flame,
            label = "STREAK",
            value = "${stats.streakWeeks}",
            unit = if (stats.streakWeeks == 1) "week" else "weeks",
            modifier = Modifier.weight(1f),
        )
        SummaryStat(
            icon = Lucide.Dumbbell,
            label = "SESSIONS",
            value = "${stats.totalSessions}",
            unit = null,
            modifier = Modifier.weight(1f),
        )
        SummaryStat(
            icon = Lucide.Clock,
            label = "TIME",
            value = formatDuration(stats.totalSeconds),
            unit = null,
            modifier = Modifier.weight(1.2f),
        )
    }
}

@Composable
private fun SummaryStat(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(12.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                color = colors.textTertiary,
                maxLines = 1,
            )
        }
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
            )
            unit?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 3.dp),
                )
            }
        }
    }
}