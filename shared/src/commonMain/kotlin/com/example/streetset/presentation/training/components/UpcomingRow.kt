package com.example.streetset.presentation.training.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.Gauge
import com.composables.icons.lucide.Hourglass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Repeat
import com.composables.icons.lucide.Scale
import com.composables.icons.lucide.Timer
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.presentation.training.model.formatRestSecondsShort
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors

enum class UpcomingRowState {
    Done,
    Current,
    Upcoming,
}

@Immutable
private data class UpcomingStyle(
    val accent: Color,
    val primary: Color,
    val secondary: Color,
    val muted: Color,
    val isDone: Boolean,
    val isCurrent: Boolean,
)

@Composable
private fun rememberUpcomingStyle(
    protocol: TrainingProtocol,
    state: UpcomingRowState,
): UpcomingStyle {
    val colors = LocalAppColors.current
    val isDone = state == UpcomingRowState.Done

    val primary by animateColorAsState(
        targetValue = if (isDone) colors.textTertiary else colors.textPrimary,
        animationSpec = tween(durationMillis = 320),
        label = "upcoming_row_primary",
    )

    return UpcomingStyle(
        accent = if (isDone) colors.textTertiary else TrainingProtocolColors.colorFor(protocol.name, colors.accent),
        primary = primary,
        secondary = colors.textSecondary,
        muted = colors.textTertiary,
        isDone = isDone,
        isCurrent = state == UpcomingRowState.Current,
    )
}

@Composable
fun UpcomingRow(
    step: WorkoutStep,
    showDivider: Boolean,
    state: UpcomingRowState = UpcomingRowState.Upcoming,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val style = rememberUpcomingStyle(step.protocol, state)

    val contentAlpha by animateFloatAsState(
        targetValue = if (style.isDone) 0.4f else 1f,
        animationSpec = tween(durationMillis = 320),
        label = "upcoming_row_alpha",
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
                .background(if (style.isCurrent) colors.surfaceHigh.copy(alpha = 0.7f) else colors.background)
                .alpha(contentAlpha)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            when (step.protocol) {
                TrainingProtocol.EMOM -> EmomUpcomingMeta(step = step, style = style)
                TrainingProtocol.CIRCUIT, TrainingProtocol.SUPERSET -> GroupUpcomingMeta(step = step, style = style)
                TrainingProtocol.HOLD -> HoldUpcomingMeta(step = step, style = style)
                TrainingProtocol.METRONOME -> MetronomeUpcomingMeta(step = step, style = style)
                else -> StandardUpcomingMeta(step = step, style = style)
            }
        }
    }
}

// Protocols
@Composable
private fun RowScope.StandardUpcomingMeta(step: WorkoutStep, style: UpcomingStyle) {
    UpcomingLeft(step = step, style = style) {
        MetaRow(
            setLabel = step.setLabel(),
            rest = step.restSeconds,
            rpe = step.rpe,
        )
    }
    Spacer(modifier = Modifier.width(12.dp))
    StandardRight(step = step, style = style)
}

@Composable
private fun RowScope.EmomUpcomingMeta(step: WorkoutStep, style: UpcomingStyle) {
    val block = step.blockWithSets.block
    val rest = block.restBetweenRoundsSeconds.takeIf { it > 0 } ?: step.restSeconds

    UpcomingLeft(step = step, style = style) {
        MetaRow(
            rounds = step.emomRounds,
            rest = rest,
            rpe = step.rpe,
        )
    }
    Spacer(modifier = Modifier.width(12.dp))
    StandardRight(step = step, style = style)
}

@Composable
private fun RowScope.HoldUpcomingMeta(step: WorkoutStep, style: UpcomingStyle) {
    val hold = step.blockWithSets.block.holdSeconds

    UpcomingLeft(step = step, style = style) {
        MetaRow(
            setLabel = step.setLabel(),
            rest = step.restSeconds,
            rpe = step.rpe,
        )
    }
    Spacer(modifier = Modifier.width(12.dp))
    if (hold != null && hold > 0) {
        RightValue(
            primary = formatRestSecondsShort(hold),
            secondary = listOfNotNull("${step.repsDisplay} reps", step.loadDisplay()).joinToString("  "),
            style = style,
            icon = Lucide.Hourglass,
        )
    } else {
        StandardRight(step = step, style = style)
    }
}

@Composable
private fun RowScope.MetronomeUpcomingMeta(step: WorkoutStep, style: UpcomingStyle) {
    UpcomingLeft(step = step, style = style) {
        MetaRow(
            setLabel = step.setLabel(),
            rest = step.restSeconds,
            rpe = step.rpe,
            bpm = step.blockWithSets.block.bpm,
        )
    }
    Spacer(modifier = Modifier.width(12.dp))
    StandardRight(step = step, style = style)
}

/** Circuit and superset. No right side: each exercise carries its own reps and load. */
@Composable
private fun RowScope.GroupUpcomingMeta(step: WorkoutStep, style: UpcomingStyle) {
    val block = step.blockWithSets.block
    val children = step.blockWithSets.children
    val rest = block.restBetweenRoundsSeconds.takeIf { it > 0 } ?: block.restSeconds

    UpcomingLeft(step = step, style = style) {
        if (children.isNotEmpty()) {
            GroupExercises(
                children = children,
                isSuperset = step.protocol == TrainingProtocol.SUPERSET,
                style = style,
            )
        }
        MetaRow(
            rounds = block.rounds,
            rest = rest,
            rpe = block.rpe,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

// Shared building blocks
@Composable
private fun RowScope.UpcomingLeft(
    step: WorkoutStep,
    style: UpcomingStyle,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.weight(1f)) {
        UpcomingTitle(name = step.exerciseName, tag = step.protocolDisplay.uppercase(), style = style)
        content()
    }
}

@Composable
private fun UpcomingTitle(name: String, tag: String, style: UpcomingStyle) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name,
            fontSize = 15.sp,
            fontWeight = if (style.isCurrent) FontWeight.SemiBold else FontWeight.Medium,
            color = style.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        // Groups are often named after their protocol, so the tag would repeat the title
        if (!name.equals(tag, ignoreCase = true)) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tag,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.7.sp,
                color = style.accent,
            )
        }
    }
}

/** Bottom row of an upcoming item. Any parameter left null is not shown. */
@Composable
private fun MetaRow(
    modifier: Modifier = Modifier.padding(top = 3.dp),
    setLabel: String? = null,
    rounds: Int? = null,
    rest: Int? = null,
    rpe: Int? = null,
    bpm: Int? = null,
) {
    val colors = LocalAppColors.current
    val roundsValue = rounds?.takeIf { it > 0 }
    val bpmValue = bpm?.takeIf { it > 0 }
    // Details are hidden once the item is done
    val restValue = rest?.takeIf { it > 0 }

    if (setLabel == null && roundsValue == null && bpmValue == null && restValue == null && rpe == null) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        setLabel?.let {
            Text(text = it, fontSize = 12.sp, color = colors.textTertiary)
        }
        roundsValue?.let {
            MetaItem(Lucide.Repeat, if (it == 1) "1 round" else "$it rounds")
        }
        bpmValue?.let {
            MetaItem(Lucide.Activity, "$it BPM")
        }
        restValue?.let {
            MetaItem(Lucide.Timer, "${formatRestSecondsShort(it)} rest")
        }
        rpe?.let {
            MetaItem(Lucide.Gauge, "RPE $it")
        }
    }
}

@Composable
private fun MetaItem(
    icon: ImageVector,
    text: String,
    color: Color = LocalAppColors.current.textTertiary,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp),
        )
        Text(text = text, fontSize = 12.sp, color = color, maxLines = 1)
    }
}

/** Right side of an item: a main value and an optional secondary line. */
@Composable
private fun RightValue(
    primary: String,
    secondary: String?,
    style: UpcomingStyle,
    icon: ImageVector? = null,
) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = style.primary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = style.primary,
            )
        }
        if (!secondary.isNullOrEmpty()) {
            Text(text = secondary, fontSize = 12.sp, color = style.muted)
        }
    }
}

@Composable
private fun StandardRight(step: WorkoutStep, style: UpcomingStyle) {
    RightValue(
        primary = "${step.repsDisplay} reps",
        secondary = step.loadDisplay(),
        style = style,
    )
}

// Circuit and superset
@Composable
private fun GroupExercises(
    children: List<BlockWithSets>,
    isSuperset: Boolean,
    style: UpcomingStyle,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(IntrinsicSize.Min),
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(1.dp))
                .background(style.accent.copy(alpha = if (style.isDone) 0.3f else 0.7f)),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
        ) {
            children.forEachIndexed { index, child ->
                val label = if (isSuperset) ('A' + index).toString() else (index + 1).toString()
                GroupExerciseLine(label = label, child = child, style = style)
            }
        }
    }
}

@Composable
private fun GroupExerciseLine(
    label: String,
    child: BlockWithSets,
    style: UpcomingStyle,
) {
    val set = child.sets.firstOrNull()
    val reps = set?.repsText?.trim().takeUnless { it.isNullOrEmpty() }
    val load = formatLoad(set?.loadText)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = style.accent,
            modifier = Modifier.width(14.dp),
        )
        Text(
            text = child.exerciseName ?: "-",
            fontSize = 13.sp,
            color = style.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            reps?.let { MetaItem(Lucide.Repeat, it, color = style.secondary) }
            load?.let { MetaItem(Lucide.Scale, it, color = style.secondary) }
        }
    }
}

// Helpers
private fun WorkoutStep.setLabel(): String = "Set $setIndex/$totalSetsInBlock"

private fun WorkoutStep.loadDisplay(): String? =
    loadValue.takeIf { it != "-" }?.let { value -> loadUnit?.let { "$value $it" } ?: value }

/** Numbers get "kg", text like "BW" stays as is. */
private fun formatLoad(raw: String?): String? {
    val load = raw?.trim().orEmpty()
    return when {
        load.isEmpty() || load == "-" -> null
        load.replace(",", ".").toDoubleOrNull() != null -> "$load kg"
        else -> load
    }
}

// Previews
private fun previewStep(
    block: Block,
    name: String,
    set: PlannedSet,
    setIndex: Int = 1,
    total: Int = 1,
    children: List<BlockWithSets> = emptyList(),
) = WorkoutStep(
    blockWithSets = BlockWithSets(
        block = block,
        exerciseName = name,
        sets = if (children.isEmpty()) listOf(set) else emptyList(),
        children = children,
    ),
    set = set,
    setIndex = setIndex,
    totalSetsInBlock = total,
)

private fun previewChild(
    id: Int,
    parentId: Int,
    name: String,
    reps: String,
    load: String,
) = BlockWithSets(
    block = Block(id = id, sessionId = 1, parentId = parentId, position = id, protocol = TrainingProtocol.NORMAL, exerciseId = id),
    exerciseName = name,
    sets = listOf(PlannedSet(id = id, blockId = id, setNumber = 1, repsText = reps, loadText = load)),
)

@Preview(showBackground = true, name = "UpcomingRow - Standard")
@Composable
private fun UpcomingRowPreviewStandard() {
    val step = previewStep(
        block = Block(id = 1, sessionId = 1, exerciseId = 1, position = 0, protocol = TrainingProtocol.NORMAL, restSeconds = 90, rpe = 7),
        name = "Back Squat",
        set = PlannedSet(id = 1, blockId = 1, setNumber = 1, repsText = "8", loadText = "120"),
        setIndex = 2,
        total = 3,
    )
    Column {
        UpcomingRow(step = step, showDivider = false, state = UpcomingRowState.Done)
        UpcomingRow(step = step, showDivider = false, state = UpcomingRowState.Current)
        UpcomingRow(step = step, showDivider = false, state = UpcomingRowState.Upcoming)
    }
}

@Preview(showBackground = true, name = "UpcomingRow - EMOM")
@Composable
private fun UpcomingRowPreviewEmom() {
    val step = previewStep(
        block = Block(id = 2, sessionId = 1, exerciseId = 1, position = 1, protocol = TrainingProtocol.EMOM, rounds = 5, restBetweenRoundsSeconds = 60, rpe = 6),
        name = "Kettlebell Swing",
        set = PlannedSet(id = 2, blockId = 2, setNumber = 1, repsText = "10", loadText = "BW"),
        total = 5,
    )
    UpcomingRow(step = step, showDivider = false)
}

@Preview(showBackground = true, name = "UpcomingRow - Hold")
@Composable
private fun UpcomingRowPreviewHold() {
    val step = previewStep(
        block = Block(id = 6, sessionId = 1, exerciseId = 1, position = 5, protocol = TrainingProtocol.HOLD, restSeconds = 90, holdSeconds = 30, rpe = 8),
        name = "Front Lever",
        set = PlannedSet(id = 6, blockId = 6, setNumber = 1, repsText = "3", loadText = "BW"),
        setIndex = 1,
        total = 3,
    )
    UpcomingRow(step = step, showDivider = false)
}

@Preview(showBackground = true, name = "UpcomingRow - Metronome")
@Composable
private fun UpcomingRowPreviewMetronome() {
    val step = previewStep(
        block = Block(id = 7, sessionId = 1, exerciseId = 1, position = 6, protocol = TrainingProtocol.METRONOME, restSeconds = 90, bpm = 60, rpe = 7),
        name = "Tempo Push-ups",
        set = PlannedSet(id = 7, blockId = 7, setNumber = 1, repsText = "8", loadText = "BW"),
        setIndex = 2,
        total = 3,
    )
    UpcomingRow(step = step, showDivider = false)
}

@Preview(showBackground = true, name = "UpcomingRow - Circuit and Superset")
@Composable
private fun UpcomingRowPreviewGroups() {
    val circuit = previewStep(
        block = Block(id = 3, sessionId = 1, position = 2, protocol = TrainingProtocol.CIRCUIT, rounds = 3, restBetweenRoundsSeconds = 45, rpe = 8),
        name = "Circuit",
        set = PlannedSet(id = 0, blockId = 3, setNumber = 1),
        children = listOf(
            previewChild(4, 3, "Pull-ups", "10", "10"),
            previewChild(5, 3, "Push-ups", "10", "BW"),
            previewChild(6, 3, "Squats", "12", "20"),
        ),
    )
    val superset = previewStep(
        block = Block(id = 8, sessionId = 1, position = 7, protocol = TrainingProtocol.SUPERSET, rounds = 3, restBetweenRoundsSeconds = 90, rpe = 8),
        name = "Superset",
        set = PlannedSet(id = 0, blockId = 8, setNumber = 1),
        children = listOf(
            previewChild(9, 8, "Bench Press", "8", "70"),
            previewChild(10, 8, "Bent Over Row", "8", "60"),
        ),
    )
    Column {
        UpcomingRow(step = circuit, showDivider = false, state = UpcomingRowState.Current)
        UpcomingRow(step = superset, showDivider = true)
    }
}