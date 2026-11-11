package com.example.streetset.presentation.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.example.streetset.presentation.stats.ConsistencyStats
import com.example.streetset.ui.components.ProgressBar
import com.example.streetset.ui.theme.LocalAppColors
import kotlin.math.roundToInt

/** Same amber the calendar uses for missed sessions. */
private val MissedColor = Color(0xFFF59E0B)

@Composable
fun ConsistencySection(consistency: ConsistencyStats) {
    val colors = LocalAppColors.current
    val percent = (consistency.rate * 100).roundToInt()

    Column {
        SectionCaption("CONSISTENCY")
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$percent%",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Text(
                text = "of planned sessions done",
                fontSize = 13.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(start = 8.dp, bottom = 5.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        ProgressBar(progress = consistency.rate, height = 8.dp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconLabel(
                icon = Lucide.Check,
                text = "${consistency.completed} done",
                iconTint = colors.success,
            )
            IconLabel(
                icon = Lucide.X,
                text = "${consistency.missed} missed",
                iconTint = MissedColor,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${consistency.planned} planned",
                fontSize = 13.sp,
                color = colors.textTertiary,
            )
        }
    }
}
