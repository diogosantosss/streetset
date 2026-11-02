package com.example.streetset.presentation.training.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Repeat
import com.example.streetset.presentation.training.model.formatDuration
import com.example.streetset.presentation.training.model.formatStartedAt
import com.example.streetset.ui.components.AppTextField
import com.example.streetset.ui.components.BOTTOM_BAR_HEIGHT
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import kotlin.math.roundToInt

@Composable
fun FinishedPhase(
    sessionTitle: String,
    startedAtMillis: Long,
    finishedAtMillis: Long,
    elapsedSeconds: Long,
    setsCompleted: Int,
    totalReps: Int,
    volumeKg: Double,
    notes: String,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(top = 24.dp, bottom = BOTTOM_BAR_HEIGHT + 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Lucide.Check,
                contentDescription = null,
                tint = colors.success,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "SESSION COMPLETE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = colors.success,
                modifier = Modifier.padding(start = 6.dp),
            )
        }

        Text(
            text = sessionTitle,
            fontSize = 34.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.6).sp,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Nice work. Log how it felt while it's still fresh.",
            fontSize = 14.sp,
            color = colors.textSecondary,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 6.dp),
        )

        Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
            Text(
                text = "DURATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = colors.textTertiary,
            )
            Text(
                text = formatDuration(elapsedSeconds),
                style = TextStyle(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-1.5).sp,
                    lineHeight = 58.sp,
                    color = colors.textPrimary,
                    fontFeatureSettings = "tnum",
                ),
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "${formatStartedAt(startedAtMillis)} → ${formatStartedAt(finishedAtMillis)}",
                fontSize = 13.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .height(0.5.dp)
                .background(colors.textTertiary.copy(alpha = 0.28f)),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MetricCell(
                icon = Lucide.Layers,
                value = setsCompleted.toString(),
                label = "SETS",
                modifier = Modifier.weight(1f),
            )
            if (totalReps > 0) {
                MetricCell(
                    icon = Lucide.Repeat,
                    value = totalReps.toString(),
                    label = "REPS",
                    modifier = Modifier.weight(1f),
                )
            }
            if (volumeKg > 0) {
                MetricCell(
                    icon = Lucide.Dumbbell,
                    value = formatVolume(volumeKg),
                    label = "VOLUME",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        AppTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = "Notes",
            placeholder = "How did it feel? PRs, tweaks, anything for next time...",
            singleLine = false,
            minHeight = 132.dp,
            modifier = Modifier.padding(top = 40.dp),
        )
    }
}

@Composable
private fun MetricCell(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            color = colors.textTertiary,
            modifier = Modifier.padding(top = 4.dp, start = 20.dp),
        )
    }
}

private fun formatVolume(kg: Double): String = when {
    kg >= 100_000 -> "${(kg / 1_000).roundToInt()}t"
    kg >= 1_000 -> {
        val tenths = ((kg / 1_000) * 10).roundToInt()
        "${tenths / 10}.${tenths % 10}t"
    }
    else -> "${kg.roundToInt()}kg"
}

@Preview(showBackground = true)
@Composable
private fun FinishedPhasePreview() {
    AppTheme(theme = AppTheme.Dark) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalAppColors.current.background)
                .padding(horizontal = 24.dp),
        ) {
            FinishedPhase(
                sessionTitle = "Push Day",
                startedAtMillis = 1_767_200_000_000L,
                finishedAtMillis = 1_767_200_000_000L + 2_145_000L,
                elapsedSeconds = 2_145L,
                setsCompleted = 18,
                totalReps = 186,
                volumeKg = 12_400.0,
                notes = "",
                onNotesChange = {},
            )
        }
    }
}