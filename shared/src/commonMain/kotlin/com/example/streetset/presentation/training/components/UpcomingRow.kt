package com.example.streetset.presentation.training.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors

@Composable
fun UpcomingRow(
    step: WorkoutStep,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val accent = TrainingProtocolColors.colorFor(step.protocol, colors.accent)

    Column(modifier = modifier.fillMaxWidth()) {
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.textTertiary.copy(alpha = 0.2f)),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = step.exerciseName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step.protocolDisplay.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.7.sp,
                        color = accent,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 3.dp),
                ) {
                    Text(
                        text = "Set ${step.setIndex}/${step.totalSetsInBlock}",
                        fontSize = 12.sp,
                        color = colors.textTertiary,
                    )
                    if (step.restSeconds > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "·", fontSize = 12.sp, color = colors.textTertiary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${step.restDisplayShort} rest",
                            fontSize = 12.sp,
                            color = colors.textTertiary,
                        )
                    }
                    step.rpe?.let { rpe ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "·", fontSize = 12.sp, color = colors.textTertiary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RPE $rpe",
                            fontSize = 12.sp,
                            color = colors.textTertiary,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = step.repsDisplay,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                )
                if (step.loadValue != "-") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+",
                        fontSize = 13.sp,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step.loadValue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                    )
                    step.loadUnit?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UpcomingRowPreview() {
    val block = Block(
        id = 1,
        sessionId = 1,
        exerciseId = 1,
        position = 0,
        protocol = "BACK_OFF",
        restSeconds = 90,
        rpe = 7,
    )
    val set = PlannedSet(
        id = 1,
        blockId = 1,
        setNumber = 1,
        repsText = "8",
        loadText = "BW",
    )
    val step = WorkoutStep(
        blockWithSets = BlockWithSets(
            block = block,
            exerciseName = "Pull Ups",
            sets = listOf(set),
            children = emptyList(),
        ),
        set = set,
        setIndex = 1,
        totalSetsInBlock = 3,
    )
    UpcomingRow(step = step, showDivider = true)
}