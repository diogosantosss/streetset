package com.example.streetset.presentation.training.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.presentation.training.model.formatRestSecondsShort
import com.example.streetset.ui.theme.LocalAppColors

/** Smallest rest the −15s button allows. Matches the ViewModel floor. */
private const val MinAdjustableRestSeconds = 5L

@Composable
fun RestPhase(
    restSecondsLeft: Long,
    restTotalSeconds: Int,
    nextStep: WorkoutStep?,
    onAdjust: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val accent = colors.accent

    val progress = if (restTotalSeconds <= 0) {
        0f
    } else {
        (restSecondsLeft.toFloat() / restTotalSeconds.toFloat()).coerceIn(0f, 1f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        ProtocolKicker(
            text = "REST",
            color = colors.textTertiary,
            letterSpacing = 1.6.sp,
        )

        TrainingClock(
            progress = progress,
            accent = accent,
            label = "rest_progress",
            modifier = Modifier
                .padding(top = 28.dp)
                .size(248.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                val time = formatRest(restSecondsLeft)
                val parts = time.split(":")
                val minutes = parts.getOrNull(0) ?: "00"
                val secondsPart = parts.getOrNull(1) ?: "00"

                DigitFlip(value = minutes)
                Text(
                    text = ":",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
                DigitFlip(value = secondsPart)
            }
        }

        Text(
            text = "${formatRestSecondsShort(restTotalSeconds)} total",
            fontSize = 12.sp,
            color = colors.textTertiary,
            modifier = Modifier.padding(top = 12.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 22.dp),
        ) {
            RestAdjustButton(
                label = "−15s",
                enabled = restSecondsLeft > MinAdjustableRestSeconds,
                onClick = { onAdjust(-15) },
            )
            RestAdjustButton(
                label = "+15s",
                enabled = true,
                onClick = { onAdjust(15) },
            )
        }

        NextUpCard(
            step = nextStep,
            modifier = Modifier.padding(top = 28.dp),
        )
    }
}

@Composable
private fun RestAdjustButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val contentAlpha = if (enabled) 1f else 0.4f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.surfaceHigh.copy(alpha = 0.6f))
            .border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary.copy(alpha = contentAlpha),
        )
    }
}

private fun formatRest(seconds: Long): String {
    val m = (seconds / 60).toString().padStart(2, '0')
    val s = (seconds % 60).toString().padStart(2, '0')
    return "$m:$s"
}

@Preview(showBackground = true)
@Composable
private fun RestPhasePreview() {
    RestPhase(
        restSecondsLeft = 90,
        restTotalSeconds = 90,
        nextStep = null,
        onAdjust = {},
    )
}
