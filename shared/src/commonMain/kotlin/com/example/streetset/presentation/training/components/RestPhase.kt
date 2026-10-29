package com.example.streetset.presentation.training.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.presentation.training.model.formatRestSecondsShort
import com.example.streetset.util.Time
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun RestPhase(
    restSecondsLeft: Long,
    restEndsAtMillis: Long,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val accent = colors.accent
    val trackColor = colors.textTertiary.copy(alpha = 0.16f)

    val totalRestSeconds = remember(restEndsAtMillis) {
        val now = Time.nowMillis()
        val diff = ((restEndsAtMillis - now + 999L) / 1000L).coerceAtLeast(1L)
        diff
    }

    val targetProgress = remember(restEndsAtMillis, restSecondsLeft, totalRestSeconds) {
        if (totalRestSeconds <= 0L) 0f else (restSecondsLeft.toFloat() / totalRestSeconds.toFloat()).coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 320, easing = LinearEasing),
        label = "rest_progress",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 36.dp, bottom = 24.dp),
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(240)) + slideInVertically(tween(240), initialOffsetY = { -it / 12 }),
        ) {
            Text(
                text = "REST",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp,
                color = colors.textTertiary,
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 28.dp)
                .size(236.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 6.dp.toPx()
                val diameter = size.minDimension
                val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)

                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )

                val sweep = 360f * animatedProgress
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }

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
            text = "${formatRestSecondsShort(totalRestSeconds.toInt())} total",
            fontSize = 12.sp,
            color = colors.textTertiary,
            modifier = Modifier.padding(top = 10.dp),
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
    val endsAt = Time.nowMillis() + 15000L
    RestPhase(
        restSecondsLeft = 15,
        restEndsAtMillis = endsAt,
    )
}
