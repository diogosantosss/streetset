package com.example.streetset.presentation.training.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = step.protocolDisplay.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = accent,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Set ${step.setIndex} of ${step.totalSetsInBlock}",
                fontSize = 13.sp,
                color = colors.textSecondary,
            )
        }

        Text(
            text = step.exerciseName,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 12.dp),
        )

        SetProgressBar(
            current = step.setIndex,
            total = step.totalSetsInBlock,
            color = accent,
            modifier = Modifier.padding(top = 16.dp),
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

@Composable
private fun SetProgressBar(
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