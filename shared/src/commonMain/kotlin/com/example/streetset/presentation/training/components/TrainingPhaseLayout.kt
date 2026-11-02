package com.example.streetset.presentation.training.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.theme.LocalAppColors

@Composable
internal fun PhaseWorkingHeader(
    protocolLabel: String,
    accent: Color,
    trailing: String,
    title: String,
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
    ellipsizeTitle: Boolean = true,
) {
    val colors = LocalAppColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = protocolLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = accent,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = trailing,
                fontSize = 13.sp,
                color = colors.textSecondary,
            )
        }

        Text(
            text = title,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            maxLines = if (ellipsizeTitle) 2 else Int.MAX_VALUE,
            overflow = if (ellipsizeTitle) TextOverflow.Ellipsis else TextOverflow.Clip,
            modifier = Modifier.padding(top = 12.dp),
        )

        SetProgressBar(
            current = current,
            total = total,
            color = accent,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
internal fun SetProgressBar(
    current: Int,
    total: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (total <= 1) return
    val track = LocalAppColors.current.textTertiary.copy(alpha = 0.25f)

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        repeat(total) { index ->
            val number = index + 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            number < current -> color.copy(alpha = 0.45f)
                            number == current -> color
                            else -> track
                        },
                    ),
            )
        }
    }
}

@Composable
internal fun TimedProtocolScaffold(
    title: String,
    highlight: String,
    protocolLabel: String,
    accent: Color,
    progress: Float,
    progressLabel: String,
    footer: String,
    modifier: Modifier = Modifier,
    extraBelowRing: @Composable (() -> Unit)? = null,
    ringContent: @Composable BoxScope.() -> Unit,
) {
    val colors = LocalAppColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 10.dp),
        ) {
            Text(
                text = highlight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = accent,
            )
            Text(
                text = "•",
                fontSize = 13.sp,
                color = colors.textTertiary,
            )
            Text(
                text = protocolLabel.uppercase(),
                fontSize = 13.sp,
                color = colors.textTertiary,
            )
        }

        ProtocolProgressRing(
            progress = progress,
            accent = accent,
            progressLabel = progressLabel,
            modifier = Modifier
                .padding(top = 36.dp)
                .size(248.dp),
            content = ringContent,
        )

        extraBelowRing?.invoke()

        Text(
            text = footer,
            fontSize = 13.sp,
            color = colors.textTertiary,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}

@Composable
internal fun ProtocolProgressRing(
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier,
    stroke: Dp = 7.dp,
    animateDurationMillis: Int = 280,
    progressLabel: String = "protocol_progress",
    content: @Composable BoxScope.() -> Unit,
) {
    val trackColor = LocalAppColors.current.textTertiary.copy(alpha = 0.16f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = animateDurationMillis, easing = LinearEasing),
        label = progressLabel,
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = stroke.toPx()
            val diameter = size.minDimension
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)
            val strokeStyle = Stroke(width = strokeWidth, cap = StrokeCap.Round)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = strokeStyle,
            )
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = strokeStyle,
            )
        }
        content()
    }
}

@Composable
internal fun ProtocolStartButton(
    onStart: () -> Unit,
    modifier: Modifier = Modifier.size(140.dp),
) {
    val colors = LocalAppColors.current
    Button(
        onClick = onStart,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accent,
            contentColor = colors.onAccent,
        ),
        modifier = modifier,
    ) {
        Text(
            text = "START",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
internal fun <T> ProtocolCenterAnimated(
    targetState: T,
    label: String,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            (scaleIn(tween(220)) + fadeIn(tween(220)))
                .togetherWith(scaleOut(tween(180)) + fadeOut(tween(180)))
        },
        label = label,
        content = { content(it) },
    )
}

@Composable
internal fun ProtocolGetReady(secondsLeft: Int, label: String) {
    val colors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProtocolKicker("GET READY")
        SlidingNumber(
            value = secondsLeft,
            label = label,
            slideDivisor = 4,
            letterSpacing = TextUnit.Unspecified,
            color = colors.textPrimary,
        )
    }
}

@Composable
internal fun ProtocolRunningSeconds(
    kicker: String,
    secondsLeft: Int,
    label: String,
) {
    val colors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProtocolKicker(kicker)
        SlidingNumber(
            value = secondsLeft,
            label = label,
            padStart = 2,
            slideDivisor = 6,
            letterSpacing = (-2).sp,
            color = colors.textPrimary,
        )
    }
}

@Composable
internal fun ProtocolDoneCenter(
    subtitle: String,
    accent: Color,
) {
    val colors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProtocolKicker(
            text = "DONE",
            color = accent,
            letterSpacing = 1.6.sp,
        )
        Text(
            text = subtitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
internal fun ProtocolKicker(
    text: String,
    color: Color = LocalAppColors.current.textTertiary,
    letterSpacing: TextUnit = 1.4.sp,
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = letterSpacing,
        color = color,
    )
}

@Composable
private fun SlidingNumber(
    value: Int,
    label: String,
    slideDivisor: Int,
    letterSpacing: TextUnit,
    color: Color,
    padStart: Int? = null,
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (fadeIn(tween(160)) + slideInVertically(tween(160), initialOffsetY = { -it / slideDivisor }))
                .togetherWith(fadeOut(tween(140)) + slideOutVertically(tween(140), targetOffsetY = { it / slideDivisor }))
        },
        label = label,
    ) { number ->
        Text(
            text = padStart?.let { number.toString().padStart(it, '0') } ?: "$number",
            fontSize = 86.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = letterSpacing,
            color = color,
        )
    }
}
