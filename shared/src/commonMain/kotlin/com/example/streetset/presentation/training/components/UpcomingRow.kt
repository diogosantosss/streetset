package com.example.streetset.presentation.training.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

enum class UpcomingRowState {
    Done,
    Current,
    Upcoming,
}

@Composable
fun UpcomingRow(
    step: WorkoutStep,
    showDivider: Boolean,
    state: UpcomingRowState = UpcomingRowState.Upcoming,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val accent = TrainingProtocolColors.colorFor(step.protocol, colors.accent)

    val isDone = state == UpcomingRowState.Done
    val isCurrent = state == UpcomingRowState.Current

    val targetAlpha = if (isDone) 0.4f else 1f
    val contentAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 320),
        label = "upcoming_row_alpha",
    )
    val dotColor by animateColorAsState(
        targetValue = when {
            isCurrent -> accent
            isDone -> colors.textTertiary
            else -> accent.copy(alpha = 0.8f)
        },
        animationSpec = tween(durationMillis = 320),
        label = "upcoming_row_dot",
    )
    val nameColor by animateColorAsState(
        targetValue = when {
            isCurrent -> colors.textPrimary
            isDone -> colors.textTertiary
            else -> colors.textPrimary
        },
        animationSpec = tween(durationMillis = 320),
        label = "upcoming_row_name",
    )
    val valueColor by animateColorAsState(
        targetValue = if (isDone) colors.textTertiary else colors.textPrimary,
        animationSpec = tween(durationMillis = 320),
        label = "upcoming_row_value",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.textTertiary.copy(alpha = 0.16f)),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isCurrent) colors.surfaceHigh.copy(alpha = 0.7f) else colors.background)
                .alpha(contentAlpha)
                .padding(horizontal = if (isCurrent) 12.dp else 0.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 6.dp else 4.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = step.exerciseName,
                        fontSize = 15.sp,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                        color = nameColor,
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
                        color = if (isDone) colors.textTertiary else accent,
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
                    if (step.restSeconds > 0 && !isDone) {
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
                        if (!isDone) {
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
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = step.repsDisplay,
                    fontSize = if (isCurrent) 17.sp else 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = valueColor,
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
                        fontSize = if (isCurrent) 17.sp else 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = valueColor,
                    )
                    step.loadUnit?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = colors.textTertiary,
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
        loadText = "120",
    )
    val step = WorkoutStep(
        blockWithSets = BlockWithSets(
            block = block,
            exerciseName = "Back Squat",
            sets = listOf(set),
            children = emptyList(),
        ),
        set = set,
        setIndex = 2,
        totalSetsInBlock = 3,
    )
    Column {
        UpcomingRow(step = step, showDivider = false, state = UpcomingRowState.Done)
        UpcomingRow(step = step, showDivider = false, state = UpcomingRowState.Current)
        UpcomingRow(step = step, showDivider = false, state = UpcomingRowState.Upcoming)
    }
}