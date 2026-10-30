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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors

enum class EmomUiState {
    IDLE,
    PREPARE_COUNTDOWN,
    RUNNING,
    COMPLETED,
}

@Composable
fun EmomPhase(
    step: WorkoutStep,
    emomUiState: EmomUiState,
    currentRound: Int,
    totalRounds: Int,
    secondsLeft: Int,
    prepareSecondsLeft: Int,
    intervalSeconds: Int = 60,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val accent = TrainingProtocolColors.colorFor(step.protocol, colors.accent)
    val trackColor = colors.textTertiary.copy(alpha = 0.16f)

    val progress = when (emomUiState) {
        EmomUiState.RUNNING -> if (secondsLeft >= 0) (secondsLeft.toFloat() / intervalSeconds.toFloat()).coerceIn(0f, 1f) else 0f
        EmomUiState.PREPARE_COUNTDOWN -> if (prepareSecondsLeft >= 0) (prepareSecondsLeft.toFloat() / 5f).coerceIn(0f, 1f) else 0f
        else -> 1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 280, easing = LinearEasing),
        label = "emom_progress",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        Text(
            text = step.exerciseName,
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
                text = "${step.repsDisplay} REPS",
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
                text = step.protocolDisplay.uppercase(),
                fontSize = 13.sp,
                color = colors.textTertiary,
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 36.dp)
                .size(248.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 7.dp.toPx()
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

            AnimatedContent(
                targetState = emomUiState,
                transitionSpec = {
                    (scaleIn(tween(220)) + fadeIn(tween(220))).togetherWith(scaleOut(tween(180)) + fadeOut(tween(180)))
                },
                label = "emom_center",
            ) { state ->
                when (state) {
                    EmomUiState.IDLE -> {
                        Button(
                            onClick = onStart,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                            modifier = Modifier.size(140.dp),
                        ) {
                            Text(
                                text = "START",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                            )
                        }

                    }
                    EmomUiState.PREPARE_COUNTDOWN -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "GET READY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.4.sp,
                                color = colors.textTertiary,
                            )
                            AnimatedContent(
                                targetState = prepareSecondsLeft,
                                transitionSpec = {
                                    (fadeIn(tween(160)) + slideInVertically(tween(160), initialOffsetY = { -it / 4 }))
                                        .togetherWith(fadeOut(tween(140)) + slideOutVertically(tween(140), targetOffsetY = { it / 4 }))
                                },
                                label = "prepare_count",
                            ) { value ->
                                Text(
                                    text = "$value",
                                    fontSize = 86.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                )
                            }
                        }
                    }

                    EmomUiState.RUNNING -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ROUND ${currentRound + 1} / $totalRounds",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.4.sp,
                                color = colors.textTertiary,
                            )
                            AnimatedContent(
                                targetState = secondsLeft,
                                transitionSpec = {
                                    (fadeIn(tween(160)) + slideInVertically(tween(160), initialOffsetY = { -it / 6 }))
                                        .togetherWith(fadeOut(tween(140)) + slideOutVertically(tween(140), targetOffsetY = { it / 6 }))
                                },
                                label = "emom_seconds",
                            ) { value ->
                                Text(
                                    text = value.toString().padStart(2, '0'),
                                    fontSize = 86.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-2).sp,
                                    color = colors.textPrimary,
                                )
                            }
                        }
                    }

                    EmomUiState.COMPLETED -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "DONE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.6.sp,
                                color = accent,
                            )
                            Text(
                                text = "ALL ROUNDS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        if (emomUiState == EmomUiState.RUNNING) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 26.dp),
            ) {
                repeat(totalRounds) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (index <= currentRound) accent else colors.textTertiary.copy(alpha = 0.3f)),
                    )
                }
            }
        }

        Text(
            text = "Every Minute On the Minute",
            fontSize = 13.sp,
            color = colors.textTertiary,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmomPhasePreview() {
    val block = Block(
        id = 1,
        sessionId = 1,
        exerciseId = 1,
        position = 0,
        protocol = "EMOM",
        restSeconds = 0,
        rounds = 5,
    )
    val set = PlannedSet(
        id = 1,
        blockId = 1,
        setNumber = 1,
        repsText = "10",
        loadText = "40",
    )
    val step = WorkoutStep(
        blockWithSets = BlockWithSets(
            block = block,
            exerciseName = "Clean & Jerk",
            sets = listOf(set),
            children = emptyList(),
        ),
        set = set,
        setIndex = 1,
        totalSetsInBlock = 1,
    )
    EmomPhase(
        step = step,
        emomUiState = EmomUiState.IDLE,
        currentRound = 0,
        totalRounds = 5,
        secondsLeft = 60,
        prepareSecondsLeft = 5,
        onStart = {},
    )
}