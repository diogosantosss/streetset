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
import com.composables.icons.lucide.Gauge
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Timer
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.presentation.training.model.formatRestSecondsShort

@Composable
fun CurrentSetPhase(
    step: WorkoutStep,
    nextStep: WorkoutStep?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PhaseWorkingHeader(
            protocol = step.protocol,
            trailing = "Set ${step.setIndex} of ${step.totalSetsInBlock}",
            title = step.exerciseName,
            current = step.setIndex,
            total = step.totalSetsInBlock,
        )

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
                    MetaChip(
                        icon = Lucide.Gauge,
                        text = "RPE $rpe",
                    )
                }
            }
        }

        NextUpCard(
            step = nextStep,
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
    CurrentSetPhase(step = step, nextStep = step.copy(setIndex = 2))
}
