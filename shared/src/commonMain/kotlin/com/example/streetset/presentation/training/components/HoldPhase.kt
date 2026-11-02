package com.example.streetset.presentation.training.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.presentation.training.model.HoldUiState
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors

@Composable
fun HoldPhase(
    step: WorkoutStep,
    holdUiState: HoldUiState,
    secondsLeft: Int,
    targetSeconds: Int,
    prepareSecondsLeft: Int,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val accent = TrainingProtocolColors.colorFor(step.protocol.name, colors.accent)

    // Para o hold, o anel enche à medida que o tempo passa (progresso, não contagem).
    val progress = when (holdUiState) {
        HoldUiState.RUNNING -> if (targetSeconds > 0) {
            (1f - secondsLeft.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f
        HoldUiState.PREPARE_COUNTDOWN -> if (prepareSecondsLeft in 0..5) {
            (1f - prepareSecondsLeft.toFloat() / 5f).coerceIn(0f, 1f)
        } else 0f
        HoldUiState.COMPLETED -> 1f
        HoldUiState.IDLE -> 0f
    }

    TimedProtocolScaffold(
        title = step.exerciseName,
        highlight = "${targetSeconds}s HOLD",
        protocolLabel = step.protocolDisplay,
        accent = accent,
        progress = progress,
        progressLabel = "hold_progress",
        footer = "Hold for ${targetSeconds}s",
        modifier = modifier,
    ) {
        ProtocolCenterAnimated(
            targetState = holdUiState,
            label = "hold_center",
        ) { state ->
            when (state) {
                HoldUiState.IDLE -> ProtocolStartButton(onStart = onStart)
                HoldUiState.PREPARE_COUNTDOWN -> ProtocolGetReady(
                    secondsLeft = prepareSecondsLeft,
                    label = "hold_prepare",
                )
                HoldUiState.RUNNING -> ProtocolRunningSeconds(
                    kicker = "HOLD",
                    secondsLeft = secondsLeft,
                    label = "hold_seconds",
                )
                HoldUiState.COMPLETED -> ProtocolDoneCenter(
                    subtitle = "HOLD COMPLETE",
                    accent = accent,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HoldPhasePreview() {
    val block = Block(
        id = 1, sessionId = 1, exerciseId = 1, position = 0,
        protocol = TrainingProtocol.HOLD, restSeconds = 60, rpe = 8,
    )
    val set = PlannedSet(id = 1, blockId = 1, setNumber = 1, repsText = "45", loadText = "BW")
    val step = WorkoutStep(
        blockWithSets = BlockWithSets(
            block = block,
            exerciseName = "Plank",
            sets = listOf(set),
            children = emptyList(),
        ),
        set = set,
        setIndex = 1,
        totalSetsInBlock = 1,
    )
    HoldPhase(
        step = step,
        holdUiState = HoldUiState.IDLE,
        secondsLeft = 45,
        targetSeconds = 45,
        prepareSecondsLeft = 5,
        onStart = {},
    )
}
