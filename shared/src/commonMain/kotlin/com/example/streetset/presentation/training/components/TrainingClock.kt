package com.example.streetset.presentation.training.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Timer dial shared by the training phases: Rest, EMOM, Hold and Metronome.
 *
 * The caller owns the countdown logic and the center content; this composable
 * only draws the ring, its soft glow and the animated sweep. `pulse` is used by
 * the metronome to keep the dial in sync with each beat.
 */
@Composable
internal fun TrainingClock(
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier,
    stroke: Dp = 8.dp,
    label: String = "training_clock",
    pulse: Float? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = LocalAppColors.current
    // A full second per tick keeps the sweep moving at a constant speed; a spring
    // re-accelerates every second and reads as a series of small jumps.
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = label,
    )
    val pulseStrength = pulse?.coerceIn(0f, 1f) ?: 1f

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // The glow is a blurred copy of the arc, not a thicker stroke around it.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(10.dp),
        ) {
            val strokePx = stroke.toPx()
            val radius = (size.minDimension - strokePx * 4f) / 2f
            if (radius <= 0f || animatedProgress <= 0f) return@Canvas
            val center = Offset(size.width / 2f, size.height / 2f)
            drawArc(
                color = accent.copy(alpha = 0.35f + 0.3f * pulseStrength),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = stroke.toPx()
            val radius = (size.minDimension - strokePx * 4f) / 2f
            if (radius <= 0f) return@Canvas

            val center = Offset(size.width / 2f, size.height / 2f)
            val arcTopLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)
            val sweep = 360f * animatedProgress

            // A little light behind the digits gives the dial some depth.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.08f), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
                radius = radius - strokePx,
                center = center,
            )

            drawArc(
                color = colors.textTertiary.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            if (animatedProgress > 0f) {
                drawArc(
                    color = accent.copy(alpha = 0.8f + 0.2f * pulseStrength),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )

                if (animatedProgress < 0.995f) {
                    val angle = (-90f + sweep) * (PI / 180f).toFloat()
                    val tip = Offset(
                        x = center.x + radius * cos(angle),
                        y = center.y + radius * sin(angle),
                    )
                    drawCircle(color = accent, radius = strokePx * 0.38f, center = tip)
                }
            }
        }
        content()
    }
}

// Previews
@Preview(showBackground = true, name = "TrainingClock - Idle")
@Composable
private fun TrainingClockIdlePreview() {
    AppTheme(theme = AppTheme.Graphite) {
        TrainingClock(
            progress = 0f,
            accent = LocalAppColors.current.accent,
            modifier = Modifier
                .size(240.dp)
                .padding(12.dp),
        ) {
            ProtocolStartButton(onStart = {})
        }
    }
}

@Preview(showBackground = true, name = "TrainingClock - Running")
@Composable
private fun TrainingClockRunningPreview() {
    AppTheme(theme = AppTheme.Graphite) {
        TrainingClock(
            progress = 0.65f,
            accent = LocalAppColors.current.accent,
            modifier = Modifier
                .size(240.dp)
                .padding(12.dp),
        ) {
            ProtocolRunningSeconds(secondsLeft = 39, label = "clock_preview")
        }
    }
}

@Preview(showBackground = true, name = "TrainingClock - Complete")
@Composable
private fun TrainingClockCompletePreview() {
    AppTheme(theme = AppTheme.Graphite) {
        val accent = LocalAppColors.current.accent
        TrainingClock(
            progress = 1f,
            accent = accent,
            modifier = Modifier
                .size(240.dp)
                .padding(12.dp),
        ) {
            ProtocolDoneCenter(subtitle = "ALL ROUNDS", accent = accent)
        }
    }
}

@Preview(showBackground = true, name = "TrainingClock - Pulse")
@Composable
private fun TrainingClockPulsePreview() {
    AppTheme(theme = AppTheme.Graphite) {
        val accent = LocalAppColors.current.accent
        TrainingClock(
            progress = 1f,
            accent = accent,
            pulse = 1f,
            modifier = Modifier
                .size(200.dp)
                .padding(12.dp),
        ) {
            Text(
                text = "3",
                fontSize = 64.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
        }
    }
}
