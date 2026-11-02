package com.example.streetset.presentation.training.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
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
    val accent = TrainingProtocolColors.colorFor(step.protocol.name, colors.accent)

    val progress = when (emomUiState) {
        EmomUiState.RUNNING -> if (secondsLeft >= 0) (secondsLeft.toFloat() / intervalSeconds.toFloat()).coerceIn(0f, 1f) else 0f
        EmomUiState.PREPARE_COUNTDOWN -> if (prepareSecondsLeft >= 0) (prepareSecondsLeft.toFloat() / 5f).coerceIn(0f, 1f) else 0f
        else -> 1f
    }

    TimedProtocolScaffold(
        title = step.exerciseName,
        highlight = "${step.repsDisplay} REPS",
        protocolLabel = step.protocolDisplay,
        accent = accent,
        progress = progress,
        progressLabel = "emom_progress",
        footer = "Every Minute On the Minute",
        modifier = modifier,
        extraBelowRing = {
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
        },
    ) {
        ProtocolCenterAnimated(
            targetState = emomUiState,
            label = "emom_center",
        ) { state ->
            when (state) {
                EmomUiState.IDLE -> ProtocolStartButton(onStart = onStart)
                EmomUiState.PREPARE_COUNTDOWN -> ProtocolGetReady(
                    secondsLeft = prepareSecondsLeft,
                    label = "prepare_count",
                )
                EmomUiState.RUNNING -> ProtocolRunningSeconds(
                    kicker = "ROUND ${currentRound + 1} / $totalRounds",
                    secondsLeft = secondsLeft,
                    label = "emom_seconds",
                )
                EmomUiState.COMPLETED -> ProtocolDoneCenter(
                    subtitle = "ALL ROUNDS",
                    accent = accent,
                )
            }
        }
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
        protocol = TrainingProtocol.EMOM,
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
