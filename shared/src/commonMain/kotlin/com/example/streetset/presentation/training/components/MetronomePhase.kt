package com.example.streetset.presentation.training.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.Gauge
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Timer
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.presentation.training.model.MetronomeUiState
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.presentation.training.model.formatRestSecondsShort
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors

@Composable
fun MetronomePhase(
    step: WorkoutStep,
    metronome: ActiveProtocolState.Metronome,
    nextStep: WorkoutStep?,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val accent = TrainingProtocolColors.colorFor(step.protocol.name, colors.accent)
    val isRunning = metronome.isRunning

    // Locked to the beat: it restarts whenever the beat count changes,
    // instead of free-running and drifting away from the sound
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(metronome.beatCount) {
        if (metronome.beatCount > 0) {
            pulse.snapTo(1f)
            pulse.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = metronome.beatIntervalMillis.coerceIn(120L, 450L).toInt()),
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        PhaseWorkingHeader(
            protocol = step.protocol,
            trailing = "Set ${step.setIndex} of ${step.totalSetsInBlock}",
            title = step.exerciseName,
            current = step.setIndex,
            total = step.totalSetsInBlock,
        )

        TrainingClock(
            progress = if (isRunning) 1f else 0f,
            accent = accent,
            stroke = 6.dp,
            pulse = if (isRunning) pulse.value else null,
            label = "metronome_clock",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 28.dp)
                .size(200.dp),
        ) {
            ProtocolCenterAnimated(
                targetState = isRunning,
                label = "metronome_center",
            ) { running ->
                if (!running) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .background(accent)
                            .clickable(onClick = onStart),
                    ) {
                        Text(
                            text = "START",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = colors.background,
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer {
                            val scale = 1f + 0.08f * pulse.value
                            scaleX = scale
                            scaleY = scale
                        },
                    ) {
                        ProtocolKicker("BEAT")
                        Text(
                            text = "${metronome.beatCount}",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accent,
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
        ) {
            MetaChip(icon = Lucide.Activity, text = "${metronome.bpm} BPM")
        }

        SetMetricsCard(
            reps = step.repsDisplay,
            loadValue = step.loadValue,
            loadUnit = step.loadUnit,
            modifier = Modifier.padding(top = 28.dp),
        )

        val hasMeta = step.restSeconds > 0 || step.rpe != null
        if (hasMeta) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 20.dp),
            ) {
                if (step.restSeconds > 0) {
                    MetaChip(
                        icon = Lucide.Timer,
                        text = formatRestSecondsShort(step.restSeconds),
                    )
                }
                step.rpe?.let { rpe ->
                    MetaChip(icon = Lucide.Gauge, text = "RPE $rpe")
                }
            }
        }

        NextUpCard(
            step = nextStep,
            modifier = Modifier.padding(top = 28.dp),
        )
    }
}

// Previews
private fun previewMetronomeStep(): WorkoutStep {
    val set = PlannedSet(id = 1, blockId = 1, setNumber = 1, repsText = "8", loadText = "BW")
    return WorkoutStep(
        blockWithSets = BlockWithSets(
            block = Block(
                id = 1,
                sessionId = 1,
                position = 0,
                protocol = TrainingProtocol.METRONOME,
                exerciseId = 1,
                restSeconds = 90,
                bpm = 60,
            ),
            exerciseName = "Tempo Push-ups",
            sets = listOf(set),
        ),
        set = set,
        setIndex = 2,
        totalSetsInBlock = 3,
    )
}

@Preview(showBackground = true, name = "MetronomePhase - Idle")
@Composable
private fun MetronomePhaseIdlePreview() {
    AppTheme(theme = AppTheme.Graphite) {
        Column(modifier = Modifier.background(LocalAppColors.current.background).padding(24.dp)) {
            MetronomePhase(
                step = previewMetronomeStep(),
                metronome = ActiveProtocolState.Metronome(bpm = 60),
                nextStep = null,
                onStart = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "MetronomePhase - Running")
@Composable
private fun MetronomePhaseRunningPreview() {
    AppTheme(theme = AppTheme.Graphite) {
        Column(modifier = Modifier.background(LocalAppColors.current.background).padding(24.dp)) {
            MetronomePhase(
                step = previewMetronomeStep(),
                metronome = ActiveProtocolState.Metronome(uiState = MetronomeUiState.RUNNING, bpm = 60, beatCount = 3),
                nextStep = null,
                onStart = {},
            )
        }
    }
}