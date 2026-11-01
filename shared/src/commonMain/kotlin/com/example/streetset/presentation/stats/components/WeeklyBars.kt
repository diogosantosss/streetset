package com.example.streetset.presentation.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.presentation.stats.BarWeeks
import com.example.streetset.presentation.stats.WeekCount
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun WeeksSection(weeks: List<WeekCount>) {
    val colors = LocalAppColors.current
    val max = (weeks.maxOfOrNull { it.sessions } ?: 0).coerceAtLeast(1)

    Column {
        SectionCaption("PER WEEK")
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .padding(top = 12.dp),
        ) {
            weeks.forEachIndexed { index, week ->
                val isCurrent = index == weeks.lastIndex
                val barHeight = (64.dp * (week.sessions.toFloat() / max)).coerceAtLeast(3.dp)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    if (week.sessions > 0) {
                        Text(
                            text = "${week.sessions}",
                            fontSize = 10.sp,
                            color = colors.textTertiary,
                            modifier = Modifier.padding(bottom = 3.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    week.sessions == 0 -> colors.textTertiary.copy(alpha = 0.12f)
                                    isCurrent -> colors.accent
                                    else -> colors.accent.copy(alpha = 0.55f)
                                },
                            ),
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            Text("$BarWeeks weeks ago", fontSize = 10.sp, color = colors.textTertiary)
            Spacer(modifier = Modifier.weight(1f))
            Text("This week", fontSize = 10.sp, color = colors.textTertiary)
        }
    }
}