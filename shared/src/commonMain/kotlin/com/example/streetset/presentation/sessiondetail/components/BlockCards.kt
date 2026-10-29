package com.example.streetset.presentation.sessiondetail.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Gauge
import com.composables.icons.lucide.Hourglass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Repeat
import com.composables.icons.lucide.Timer
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors


sealed interface BlockUiState {
    data object Loading : BlockUiState

    data class Error(val message: String) : BlockUiState

    data class Content(
        val item: BlockWithSets,
        val isBusy: Boolean = false
    ) : BlockUiState
}

// Entry point
@Composable
fun BlockSection(
    state: BlockUiState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onRetry: () -> Unit = {},
) {
    when (state) {
        BlockUiState.Loading -> BlockSkeletonSection(modifier = modifier)

        is BlockUiState.Error -> BlockErrorSection(state.message, onRetry, modifier)

        is BlockUiState.Content -> {
            val item = state.item
            when (item.block.protocol.normalizedProtocol()) {
                "superset", "circuit" -> GroupBlockSection(item, state.isBusy, modifier, onClick, onLongClick)
                "hold" -> HoldBlockSection(item, state.isBusy, modifier, onClick, onLongClick)
                "emom" -> EmomBlockSection(item, state.isBusy, modifier, onClick, onLongClick)
                "metronome" -> MetronomeBlockSection(item, state.isBusy, modifier, onClick, onLongClick)
                else -> StandardBlockSection(item, state.isBusy, modifier, onClick, onLongClick)
            }
        }
    }
}

// 1. Normal / Warm-up / Top set / Back-off
@Composable
fun StandardBlockSection(
    item: BlockWithSets,
    isBusy: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    SectionFrame(
        block = item.block,
        title = item.exerciseName,
        isBusy = isBusy,
        footer = metaItems(item.block),
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        SetRows(item.sets)
    }
}

// 2. Superset / Circuit
@Composable
fun GroupBlockSection(
    item: BlockWithSets,
    isBusy: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = LocalAppColors.current
    val block = item.block
    val color = TrainingProtocolColors.colorFor(block.protocol, colors.accent)
    val isSuperset = block.protocol.normalizedProtocol() == "superset"

    SectionFrame(
        block = block,
        title = null,
        isBusy = isBusy,
        footer = block.rpe?.let { listOf(MetaItem(Lucide.Gauge, "RPE $it")) }.orEmpty(),
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        // Linha de resumo: rondas + descanso entre rondas
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 9.dp),
        ) {
            Text(
                text = block.rounds?.let { if (it == 1) "1 round" else "$it rounds" } ?: "Rounds",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (block.restBetweenRoundsSeconds > 0) {
                Text(
                    text = "${formatSeconds(block.restBetweenRoundsSeconds)} rest between",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                )
            }
        }
        InsetDivider()

        // Barra vertical a ligar os exercícios do grupo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(start = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.copy(alpha = 0.85f)),
            )
            Column(modifier = Modifier.weight(1f)) {
                item.children.forEachIndexed { index, child ->
                    val label = if (isSuperset) ('A' + index).toString() else (index + 1).toString()
                    GroupExerciseRow(label = label, child = child, color = color)
                    if (index < item.children.lastIndex) InsetDivider(start = 12.dp)
                }
            }
        }
    }
}

@Composable
private fun GroupExerciseRow(label: String, child: BlockWithSets, color: Color) {
    val colors = LocalAppColors.current
    val summary = child.sets.joinToString("  ·  ") { set ->
        listOfNotNull(set.repsText, set.loadText).joinToString(" × ")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 16.dp, top = 9.dp, bottom = 9.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.width(20.dp),
        )
        Text(
            text = child.exerciseName ?: "Exercise",
            fontSize = 14.sp,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = summary,
            fontSize = 13.sp,
            color = colors.textSecondary,
            maxLines = 1,
        )
    }
}

// 3. Hold
@Composable
fun HoldBlockSection(
    item: BlockWithSets,
    isBusy: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val color = TrainingProtocolColors.colorFor(item.block.protocol, LocalAppColors.current.accent)

    SectionFrame(
        block = item.block,
        title = item.exerciseName,
        isBusy = isBusy,
        footer = metaItems(item.block),
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        item.block.holdSeconds?.let {
            HeroRow(Lucide.Hourglass, formatSeconds(it), "HOLD PER REP", color)
            InsetDivider()
        }
        SetRows(item.sets)
    }
}

// 4. EMOM
@Composable
fun EmomBlockSection(
    item: BlockWithSets,
    isBusy: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val color = TrainingProtocolColors.colorFor(item.block.protocol, LocalAppColors.current.accent)
    val totalMinutes = item.sets.sumOf { it.minutes ?: 0 }

    SectionFrame(
        block = item.block,
        title = item.exerciseName,
        isBusy = isBusy,
        footer = metaItems(item.block, includeRest = false),
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        if (totalMinutes > 0) {
            HeroRow(Lucide.Clock, "$totalMinutes min", "EVERY MINUTE ON THE MINUTE", color)
            InsetDivider()
        }
        SetRows(item.sets) { set -> set.minutes?.let { "$it min" } ?: "Set ${set.setNumber}" }
    }
}

// 5. Metronome
@Composable
fun MetronomeBlockSection(
    item: BlockWithSets,
    isBusy: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val color = TrainingProtocolColors.colorFor(item.block.protocol, LocalAppColors.current.accent)

    SectionFrame(
        block = item.block,
        title = item.exerciseName,
        isBusy = isBusy,
        footer = metaItems(item.block),
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        item.block.bpm?.let {
            HeroRow(Lucide.Activity, "$it", "BPM", color)
            InsetDivider()
        }
        SetRows(item.sets)
    }
}

// States: Loading & Errors
@Composable
fun BlockSkeletonSection(
    modifier: Modifier = Modifier,
    rows: Int = 3,
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        SkeletonBar(
            width = 110.dp,
            height = 10.dp,
            alpha = alpha,
            modifier = Modifier.padding(start = 16.dp, bottom = 10.dp, top = 2.dp),
        )
        GroupedSurface {
            repeat(rows) { index ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                ) {
                    SkeletonBar(width = 48.dp, alpha = alpha)
                    Spacer(modifier = Modifier.weight(1f))
                    SkeletonBar(width = 96.dp, alpha = alpha)
                }
                if (index < rows - 1) InsetDivider()
            }
        }
    }
}

@Composable
private fun SkeletonBar(
    width: Dp,
    alpha: Float,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(LocalAppColors.current.textTertiary.copy(alpha = alpha)),
    )
}

@Composable
fun BlockErrorSection(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    GroupedSurface(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFFEF4444),
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Retry",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.accent,
                modifier = Modifier.clickable(onClick = onRetry),
            )
        }
    }
}

// Shared Components
@Composable
private fun GroupedSurface(
    modifier: Modifier = Modifier,
    background: Color = LocalAppColors.current.surfaceHigh,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background),
        content = content,
    )
}

@Composable
private fun SectionFrame(
    block: Block,
    title: String?,
    isBusy: Boolean,
    footer: List<MetaItem> = emptyList(),
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalAppColors.current
    val color = TrainingProtocolColors.colorFor(block.protocol, colors.accent)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val clickable = !isBusy && (onClick != null || onLongClick != null)

    val surfaceColor = if (pressed && clickable) {
        Color.Black.copy(alpha = 0.12f).compositeOver(colors.surfaceHigh)
    } else {
        colors.surfaceHigh
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
        ) {
            Text(
                text = protocolLabel(block.protocol).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                color = color,
            )
            if (!title.isNullOrBlank()) {
                Text(
                    text = "  ·  ",
                    fontSize = 11.sp,
                    color = colors.textTertiary,
                )
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.6.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = colors.textSecondary,
                )
            }
        }

        GroupedSurface(
            background = surfaceColor,
            modifier = Modifier
                .alpha(if (isBusy) 0.5f else 1f)
                .combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = clickable,
                    onClick = { onClick?.invoke() },
                    onLongClick = onLongClick,
                ),
            content = content,
        )

        if (footer.isNotEmpty()) {
            MetaFooter(
                items = footer,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp),
            )
        }
    }
}

@Composable
private fun SetRows(
    sets: List<PlannedSet>,
    label: (PlannedSet) -> String = { "Set ${it.setNumber}" },
) {
    val colors = LocalAppColors.current

    sets.forEachIndexed { index, set ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 9.dp),
        ) {
            Text(
                text = label(set),
                fontSize = 14.sp,
                color = colors.textPrimary,
            )
            Spacer(modifier = Modifier.weight(1f))
            SetValues(set)
        }
        if (index < sets.lastIndex) InsetDivider()
    }
}

@Composable
private fun HeroRow(
    icon: ImageVector,
    value: String, label:
    String, color: Color
) {
    val colors = LocalAppColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(11.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun InsetDivider(start: Dp = 16.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = start)
            .height(0.5.dp)
            .background(LocalAppColors.current.textTertiary.copy(alpha = 0.3f)),
    )
}

// Helpers
private data class MetaItem(val icon: ImageVector, val text: String)

private fun metaItems(block: Block, includeRest: Boolean = true): List<MetaItem> = listOfNotNull(
    if (includeRest && block.restSeconds > 0) MetaItem(Lucide.Timer, "${formatSeconds(block.restSeconds)} rest") else null,
    block.rpe?.let { MetaItem(Lucide.Gauge, "RPE $it") },
)

private fun formatLoad(raw: String?): String? {
    val load = raw?.trim().orEmpty()
    if (load.isEmpty()) return null
    return if (load.replace(",", ".").toDoubleOrNull() != null) "$load kg" else load
}

private fun setSummary(set: PlannedSet): String {
    val parts = listOfNotNull(set.repsText?.let { "$it reps" }, set.loadText)
    return if (parts.isEmpty()) "-" else parts.joinToString("  ·  ")
}

private fun metaLine(block: Block, includeRest: Boolean = true): String? {
    val parts = listOfNotNull(
        if (includeRest && block.restSeconds > 0) "${formatSeconds(block.restSeconds)} rest" else null,
        block.rpe?.let { "RPE $it" },
    )
    return if (parts.isEmpty()) null else parts.joinToString("  ·  ")
}

private fun String.normalizedProtocol(): String =
    lowercase().replace("_", "").replace("-", "").replace(" ", "")

private fun protocolLabel(protocol: String): String = when (protocol.normalizedProtocol()) {
    "normal" -> "Normal"
    "warmup" -> "Warm-up"
    "topset" -> "Top set"
    "backoff" -> "Back-off"
    "superset" -> "Superset"
    "circuit" -> "Circuit"
    "hold" -> "Hold"
    "emom" -> "EMOM"
    "metronome" -> "Metronome"
    else -> protocol
}

private fun formatSeconds(total: Int): String {
    val minutes = total / 60
    val seconds = total % 60
    return when {
        minutes == 0 -> "${seconds}s"
        seconds == 0 -> "${minutes}min"
        else -> "${minutes}m ${seconds}s"
    }
}

@Composable
private fun IconValue(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    valueColor: Color = LocalAppColors.current.textSecondary,
    iconTint: Color = LocalAppColors.current.textTertiary,
    iconSize: Dp = 13.dp,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = valueColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun SetValues(
    set: PlannedSet,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
) {
    val reps = set.repsText?.trim().takeUnless { it.isNullOrEmpty() }
    val load = formatLoad(set.loadText)

    if (reps == null && load == null) {
        Text(
            text = "-",
            fontSize = fontSize,
            color = LocalAppColors.current.textTertiary,
            modifier = modifier,
        )
        return
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        reps?.let { IconValue(Lucide.Repeat, it, fontSize = fontSize) }
        load?.let { IconValue(Lucide.Dumbbell, it, fontSize = fontSize) }
    }
}

@Composable
private fun MetaFooter(items: List<MetaItem>, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        items.forEach { item ->
            IconValue(
                icon = item.icon,
                text = item.text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                valueColor = colors.textTertiary,
                iconSize = 12.dp,
            )
        }
    }
}

// Previews
@Composable
private fun PreviewSurface(content: @Composable ColumnScope.() -> Unit) {
    AppTheme(theme = AppTheme.Dark) {
        Column(
            modifier = Modifier
                .background(LocalAppColors.current.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content,
        )
    }
}

private fun previewBlock(
    protocol: String,
    id: Int = 1,
    parentId: Int? = null,
    restSeconds: Int = 0,
    rounds: Int? = null,
    restBetweenRoundsSeconds: Int = 0,
    holdSeconds: Int? = null,
    rpe: Int? = null,
    bpm: Int? = null,
) = Block(
    id = id,
    sessionId = 1,
    parentId = parentId,
    protocol = protocol,
    restSeconds = restSeconds,
    rounds = rounds,
    restBetweenRoundsSeconds = restBetweenRoundsSeconds,
    holdSeconds = holdSeconds,
    rpe = rpe,
    bpm = bpm,
)

private fun previewSet(
    blockId: Int,
    number: Int,
    reps: String? = null,
    load: String? = null,
    minutes: Int? = null,
) = PlannedSet(blockId = blockId, setNumber = number, repsText = reps, loadText = load, minutes = minutes)

private val previewStandard = BlockWithSets(
    block = previewBlock("topset", restSeconds = 180, rpe = 9),
    exerciseName = "Back Squat",
    sets = listOf(previewSet(1, 1, "3", "120kg")),
)

@Preview
@Composable
private fun StandardBlockSectionPreview() {
    PreviewSurface {
        StandardBlockSection(
            BlockWithSets(
                block = previewBlock("warmup", restSeconds = 60),
                exerciseName = "Back Squat",
                sets = listOf(previewSet(1, 1, "10", "40kg"), previewSet(1, 2, "5", "60kg")),
            ),
        )
        StandardBlockSection(previewStandard)
        StandardBlockSection(
            BlockWithSets(
                block = previewBlock("backoff", id = 3, restSeconds = 120, rpe = 8),
                exerciseName = "Back Squat",
                sets = listOf(
                    previewSet(3, 1, "6", "100kg"),
                    previewSet(3, 2, "6", "100kg"),
                    previewSet(3, 3, "6", "100kg"),
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun GroupBlockSectionPreview() {
    PreviewSurface {
        GroupBlockSection(
            BlockWithSets(
                block = previewBlock("circuit", rounds = 2, restBetweenRoundsSeconds = 30, rpe = 8),
                children = listOf(
                    BlockWithSets(previewBlock("normal", id = 2, parentId = 1), "Pull-ups", listOf(previewSet(2, 1, "10", "10kg"))),
                    BlockWithSets(previewBlock("normal", id = 3, parentId = 1), "Push-ups", listOf(previewSet(3, 1, "10"))),
                    BlockWithSets(previewBlock("normal", id = 4, parentId = 1), "Squats", listOf(previewSet(4, 1, "10", "30kg"))),
                ),
            ),
        )
        GroupBlockSection(
            BlockWithSets(
                block = previewBlock("superset", id = 5, rounds = 3, restBetweenRoundsSeconds = 90, rpe = 8),
                children = listOf(
                    BlockWithSets(previewBlock("normal", id = 6, parentId = 5), "Bench Press", listOf(previewSet(6, 1, "8", "70kg"))),
                    BlockWithSets(previewBlock("normal", id = 7, parentId = 5), "Bent Over Row", listOf(previewSet(7, 1, "8", "60kg"))),
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun HoldBlockSectionPreview() {
    PreviewSurface {
        HoldBlockSection(
            BlockWithSets(
                block = previewBlock("hold", restSeconds = 90, holdSeconds = 30, rpe = 8),
                exerciseName = "Front Lever Hold",
                sets = listOf(
                    previewSet(1, 1, "3", "BW"),
                    previewSet(1, 2, "3", "BW"),
                    previewSet(1, 3, "3", "BW"),
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun EmomBlockSectionPreview() {
    PreviewSurface {
        EmomBlockSection(
            BlockWithSets(
                block = previewBlock("emom", rpe = 7),
                exerciseName = "Kettlebell Swing",
                sets = listOf(previewSet(1, 1, "12", "24kg", minutes = 10)),
            ),
        )
    }
}

@Preview
@Composable
private fun MetronomeBlockSectionPreview() {
    PreviewSurface {
        MetronomeBlockSection(
            BlockWithSets(
                block = previewBlock("metronome", restSeconds = 90, rpe = 7, bpm = 60),
                exerciseName = "Tempo Push-ups",
                sets = listOf(
                    previewSet(1, 1, "8", "BW"),
                    previewSet(1, 2, "8", "BW"),
                    previewSet(1, 3, "8", "BW"),
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun BlockStatesPreview() {
    PreviewSurface {
        BlockSection(state = BlockUiState.Loading)
        BlockSection(state = BlockUiState.Content(previewStandard, isBusy = true))
        BlockSection(state = BlockUiState.Error("Could not load block"))
    }
}
