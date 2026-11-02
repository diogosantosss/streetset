package com.example.streetset.presentation.training.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors

@Composable
fun CurrentSetPhase(
    step: WorkoutStep,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val accent = TrainingProtocolColors.colorFor(step.protocol.name, colors.accent)

    Column(modifier = modifier.fillMaxWidth()) {
        PhaseWorkingHeader(
            protocolLabel = step.protocolDisplay.uppercase(),
            accent = accent,
            trailing = "Set ${step.setIndex} of ${step.totalSetsInBlock}",
            title = step.exerciseName,
            current = step.setIndex,
            total = step.totalSetsInBlock,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
        ) {
            StatCard(
                value = step.repsDisplay,
                unit = null,
                label = "REPS",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                value = step.loadValue,
                unit = step.loadUnit,
                label = "LOAD",
                modifier = Modifier.weight(1f),
            )
        }

        RestMetaRow(
            restSeconds = step.restSeconds,
            rpe = step.rpe,
            modifier = Modifier.padding(top = 28.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrentSetPhasePreview() {
    val block = Block(
        id = 1,
        sessionId = 1,
        exerciseId = 1,
        position = 0,
        protocol = TrainingProtocol.TOP_SET,
        restSeconds = 90,
        rpe = 8,
    )
    val set = PlannedSet(
        id = 1,
        blockId = 1,
        setNumber = 1,
        repsText = "3",
        loadText = "80",
    )
    val step = WorkoutStep(
        blockWithSets = BlockWithSets(
            block = block,
            exerciseName = "Bench Press",
            sets = listOf(set),
            children = emptyList(),
        ),
        set = set,
        setIndex = 1,
        totalSetsInBlock = 3,
    )
    CurrentSetPhase(step = step)
}
