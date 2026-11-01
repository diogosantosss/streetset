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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Gauge
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Repeat
import com.composables.icons.lucide.Scale
import com.composables.icons.lucide.Timer
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.presentation.training.model.formatRestSecondsShort
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors

/** Current step of a circuit or superset: one round, with all its exercises on screen. */
@Composable
fun CurrentGroupPhase(
    step: WorkoutStep,
    group: ActiveProtocolState.Group,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val block = step.blockWithSets.block
    val children = step.blockWithSets.children
    val accent = TrainingProtocolColors.colorFor(group.protocol.name, colors.accent)
    val rest = group.restSeconds

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
                text = "Round ${step.setIndex} of ${step.totalSetsInBlock}",
                fontSize = 13.sp,
                color = colors.textSecondary,
            )
        }

        Text(
            text = if (children.size == 1) "1 exercise" else "${children.size} exercises",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 12.dp),
        )

        RoundProgress(
            current = step.setIndex,
            total = step.totalSetsInBlock,
            color = accent,
            modifier = Modifier.padding(top = 16.dp),
        )

        Column(modifier = Modifier.padding(top = 20.dp)) {
            children.forEachIndexed { index, child ->
                val label = if (group.isSuperset) ('A' + index).toString() else (index + 1).toString()
                GroupExerciseRow(label = label, child = child, accent = accent)
                if (index < children.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 28.dp)
                            .height(0.5.dp)
                            .background(colors.textTertiary.copy(alpha = 0.3f)),
                    )
                }
            }
        }

        val hasFooter = rest > 0 || block.rpe != null
        if (hasFooter) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 20.dp),
            ) {
                if (rest > 0) {
                    val target = if (group.isLastRound) step.protocolDisplay.lowercase() else "round"
                    IconText(
                        icon = Lucide.Timer,
                        text = "${formatRestSecondsShort(rest)} rest after this $target",
                        color = colors.textTertiary,
                        fontSize = 13.sp,
                        iconSize = 13.dp,
                    )
                }
                block.rpe?.let {
                    IconText(
                        icon = Lucide.Gauge,
                        text = "RPE $it",
                        color = colors.textTertiary,
                        fontSize = 13.sp,
                        iconSize = 13.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupExerciseRow(
    label: String,
    child: BlockWithSets,
    accent: Color,
) {
    val colors = LocalAppColors.current
    val set = child.sets.firstOrNull()
    val reps = set?.repsText?.trim().takeUnless { it.isNullOrEmpty() }
    val load = formatGroupLoad(set?.loadText)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = accent,
            modifier = Modifier.width(28.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            if (reps != null) {
                Text(
                    text = reps,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = child.exerciseName ?: "-",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (load != null) {
            Spacer(modifier = Modifier.width(12.dp))
            IconText(Lucide.Dumbbell, load, colors.textSecondary)
        }
    }
}

@Composable
private fun IconText(
    icon: ImageVector,
    text: String,
    color: Color,
    fontSize: TextUnit = 17.sp,
    iconSize: Dp = 14.dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LocalAppColors.current.textTertiary,
            modifier = Modifier.size(iconSize),
        )
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            color = color,
            maxLines = 1,
        )
    }
}

/** One thin segment per round: done, current and remaining. */
@Composable
private fun RoundProgress(
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

/** Numbers get "kg", text like "BW" stays as is. */
private fun formatGroupLoad(raw: String?): String? {
    val load = raw?.trim().orEmpty()
    return when {
        load.isEmpty() || load == "-" -> null
        load.uppercase() == "BW" -> null
        load.replace(",", ".").toDoubleOrNull() != null -> "${load}kg"
        else -> load
    }
}

// Previews
private fun previewChild(id: Int, name: String, reps: String, load: String) = BlockWithSets(
    block = Block(id = id, sessionId = 1, parentId = 1, position = id, protocol = TrainingProtocol.NORMAL, exerciseId = id),
    exerciseName = name,
    sets = listOf(PlannedSet(id = id, blockId = id, setNumber = 1, repsText = reps, loadText = load)),
)

private fun previewGroupStep(protocol: TrainingProtocol, children: List<BlockWithSets>, round: Int) = WorkoutStep(
    blockWithSets = BlockWithSets(
        block = Block(
            id = 1,
            sessionId = 1,
            position = 0,
            protocol = protocol,
            rounds = 3,
            restSeconds = 60,
            restBetweenRoundsSeconds = 30,
            rpe = 8,
        ),
        children = children,
    ),
    set = PlannedSet(blockId = 1, setNumber = round),
    setIndex = round,
    totalSetsInBlock = 3,
)

@Preview(showBackground = true, name = "CurrentGroupPhase - Circuit")
@Composable
private fun CurrentGroupPhaseCircuitPreview() {
    val step = previewGroupStep(
        TrainingProtocol.CIRCUIT,
        listOf(
            previewChild(2, "Pull-ups", "10", "10"),
            previewChild(3, "Push-ups", "10", "BW"),
            previewChild(4, "Squats", "12", "20"),
        ),
        round = 2,
    )

    AppTheme(theme = AppTheme.Dark) {
        Column(modifier = Modifier.background(LocalAppColors.current.background).padding(24.dp)) {
            CurrentGroupPhase(
                step = step,
                group = ActiveProtocolState.Group.from(step),
            )
        }
    }
}

@Preview(showBackground = true, name = "CurrentGroupPhase - Superset")
@Composable
private fun CurrentGroupPhaseSupersetPreview() {
    val step = previewGroupStep(
        TrainingProtocol.SUPERSET,
        listOf(
            previewChild(2, "Bench Press", "8", "70"),
            previewChild(3, "Bent Over Row", "8", "60"),
        ),
        round = 3,
    )

    AppTheme(theme = AppTheme.Dark) {
        Column(modifier = Modifier.background(LocalAppColors.current.background).padding(24.dp)) {
            CurrentGroupPhase(
                step = step,
                group = ActiveProtocolState.Group.from(step),
            )
        }
    }
}