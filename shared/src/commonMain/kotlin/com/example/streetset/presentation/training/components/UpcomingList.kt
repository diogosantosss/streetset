package com.example.streetset.presentation.training.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.training.model.WorkoutStep

@Composable
fun UpcomingList(
    steps: List<WorkoutStep>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            UpcomingRow(
                step = step,
                showDivider = index > 0,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UpcomingListPreview() {
    val block1 = Block(id = 1, sessionId = 1, exerciseId = 1, position = 0, protocol = "WARM_UP", restSeconds = 60)
    val set1 = PlannedSet(id = 1, blockId = 1, setNumber = 1, repsText = "5", loadText = "60")
    val block2 = Block(id = 2, sessionId = 1, exerciseId = 2, position = 1, protocol = "BACK_OFF", restSeconds = 90, rpe = 7)
    val set2 = PlannedSet(id = 2, blockId = 2, setNumber = 1, repsText = "8", loadText = "BW")
    val steps = listOf(
        WorkoutStep(
            blockWithSets = BlockWithSets(block1, "Squat", listOf(set1), emptyList()),
            set = set1,
            setIndex = 2,
            totalSetsInBlock = 3,
        ),
        WorkoutStep(
            blockWithSets = BlockWithSets(block2, "Pull Ups", listOf(set2), emptyList()),
            set = set2,
            setIndex = 1,
            totalSetsInBlock = 3,
        ),
    )
    UpcomingList(steps = steps)
}
