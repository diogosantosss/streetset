package com.example.streetset.presentation.sessiondetail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import com.example.streetset.presentation.sessiondetail.AddBlockState
import com.example.streetset.presentation.sessiondetail.description
import com.example.streetset.presentation.sessiondetail.label
import com.example.streetset.presentation.sessiondetail.model.GroupExerciseDraft
import com.example.streetset.presentation.sessiondetail.model.PlannedSetDraft
import com.example.streetset.presentation.sessiondetail.model.TrainingBlockDraft
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.presentation.sessiondetail.model.isValid
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors

@Composable
fun AddBlockInline(
    state: AddBlockState,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onDraftChange: (TrainingBlockDraft) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val isOpen = state !is AddBlockState.Closed

    // Keeps the last open state so the editor doesn't flash empty while it animates out
    var lastOpen by remember { mutableStateOf<AddBlockState>(AddBlockState.Editing()) }
    if (isOpen) lastOpen = state

    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = !isOpen,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            Column {
                AddBlockButton(onClick = onOpen)
                Text(
                    text = "Add an exercise block to this session",
                    fontSize = 12.sp,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = 16.dp, top = 6.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = isOpen,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            BlockComposer(
                state = lastOpen,
                onDraftChange = onDraftChange,
                onCancel = onClose,
                onSave = onSave,
            )
        }
    }
}

@Composable
private fun AddBlockButton(onClick: () -> Unit) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.accent.copy(alpha = 0.15f)),
        ) {
            Icon(
                imageVector = Lucide.Plus,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Add block",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun BlockComposer(
    state: AddBlockState,
    onDraftChange: (TrainingBlockDraft) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = when (state) {
        AddBlockState.Closed -> TrainingBlockDraft()
        is AddBlockState.Editing -> state.draft
        is AddBlockState.Submitting -> state.draft
    }
    val error = (state as? AddBlockState.Editing)?.error
    val isBusy = state is AddBlockState.Submitting
    val focusManager = LocalFocusManager.current

    // Closes the keyboard when saving starts
    LaunchedEffect(isBusy) {
        if (isBusy) focusManager.clearFocus()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        ComposerHeader(onCancel = onCancel, cancelEnabled = !isBusy)

        Box {
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.alpha(if (isBusy) 0.5f else 1f),
            ) {
                ProtocolSection(
                    selected = draft.protocol,
                    onSelected = { onDraftChange(draft.copy(protocol = it)) },
                )

                if (draft.protocol.isGroup) {
                    DetailsSection(draft = draft, onDraftChange = onDraftChange)
                    GroupExercisesSection(draft = draft, onDraftChange = onDraftChange)
                } else {
                    ExerciseSection(
                        name = draft.exerciseName,
                        onNameChange = { onDraftChange(draft.copy(exerciseName = it)) },
                    )
                    SetsSection(
                        sets = draft.sets,
                        onSetsChange = { onDraftChange(draft.copy(sets = it)) },
                    )
                    DetailsSection(draft = draft, onDraftChange = onDraftChange)
                }
            }

            // Blocks touches while saving
            if (isBusy) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent().changes.forEach { it.consume() }
                                }
                            }
                        },
                )
            }
        }

        error?.let {
            Text(
                text = it,
                fontSize = 13.sp,
                color = Color(0xFFEF4444),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        SaveButton(
            isBusy = isBusy,
            hasError = error != null,
            enabled = draft.isValid,
            onClick = onSave,
        )
    }
}

@Composable
private fun ComposerHeader(
    onCancel: () -> Unit,
    cancelEnabled: Boolean,
) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
    ) {
        Text(
            text = "New block",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Cancel",
            fontSize = 15.sp,
            color = if (cancelEnabled) colors.accent else colors.textTertiary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = cancelEnabled, onClick = onCancel)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ProtocolSection(
    selected: TrainingProtocol,
    onSelected: (TrainingProtocol) -> Unit,
) {
    val colors = LocalAppColors.current

    Column {
        SectionCaption("PROTOCOL")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            TrainingProtocol.entries.forEach { protocol ->
                val protocolColor = TrainingProtocolColors.colorFor(protocol.name, colors.accent)
                val isSelected = protocol == selected

                Text(
                    text = protocol.label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) protocolColor else colors.textSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) protocolColor.copy(alpha = 0.16f) else colors.surfaceHigh)
                        .clickable { onSelected(protocol) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
        Text(
            text = selected.description,
            fontSize = 12.sp,
            color = colors.textTertiary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp),
        )
    }
}

@Composable
private fun ExerciseSection(
    name: String,
    onNameChange: (String) -> Unit,
) {
    val colors = LocalAppColors.current

    FormSection(caption = "EXERCISE") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .padding(horizontal = 16.dp),
        ) {
            Icon(
                imageVector = Lucide.Dumbbell,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            InlineField(
                value = name,
                onValueChange = onNameChange,
                placeholder = "Search or create an exercise",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SetsSection(
    sets: List<PlannedSetDraft>,
    onSetsChange: (List<PlannedSetDraft>) -> Unit,
) {
    FormSection(caption = "SETS") {
        sets.forEachIndexed { index, set ->
            SetFieldRow(
                number = index + 1,
                set = set,
                canDelete = sets.size > 1,
                onChange = { changed ->
                    onSetsChange(sets.mapIndexed { itemIndex, item -> if (itemIndex == index) changed else item })
                },
                onDelete = { onSetsChange(sets.filterIndexed { itemIndex, _ -> itemIndex != index }) },
            )
            FormDivider()
        }
        AddRow(text = "Add set", onClick = { onSetsChange(sets + PlannedSetDraft()) })
    }
}

@Composable
private fun SetFieldRow(
    number: Int,
    set: PlannedSetDraft,
    canDelete: Boolean,
    onChange: (PlannedSetDraft) -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .padding(start = 16.dp, end = if (canDelete) 6.dp else 16.dp),
    ) {
        Text(
            text = "Set $number",
            fontSize = 14.sp,
            color = colors.textSecondary,
            modifier = Modifier.width(52.dp),
        )
        InlineField(
            value = set.reps,
            onValueChange = { onChange(set.copy(reps = it)) },
            placeholder = "Reps",
            modifier = Modifier.weight(1f),
        )
        VerticalHairline()
        InlineField(
            value = set.load,
            onValueChange = { onChange(set.copy(load = it)) },
            placeholder = "Load",
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
        if (canDelete) {
            IconButton(icon = Lucide.Trash2, tint = colors.textTertiary, onClick = onDelete)
        }
    }
}

private data class DetailRow(
    val label: String,
    val value: String,
    val placeholder: String,
    val onChange: (String) -> Unit,
)

@Composable
private fun DetailsSection(
    draft: TrainingBlockDraft,
    onDraftChange: (TrainingBlockDraft) -> Unit,
) {
    val protocol = draft.protocol
    val restBetweenLabel = if (protocol == TrainingProtocol.SUPERSET) "Rest between supersets" else "Rest between rounds"

    val rows = buildList {
        if (protocol.needsRounds) add(DetailRow("Rounds", draft.rounds, "3") { onDraftChange(draft.copy(rounds = it)) })
        if (protocol == TrainingProtocol.HOLD) add(DetailRow("Hold", draft.holdSeconds, "30s") { onDraftChange(draft.copy(holdSeconds = it)) })
        if (protocol == TrainingProtocol.METRONOME) add(DetailRow("BPM", draft.bpm, "60") { onDraftChange(draft.copy(bpm = it)) })
        if (protocol != TrainingProtocol.EMOM && !protocol.isGroup) {
            add(DetailRow("Rest", draft.restSeconds, "90s") { onDraftChange(draft.copy(restSeconds = it)) })
        }
        if (protocol.needsRounds) {
            add(DetailRow(restBetweenLabel, draft.restBetweenRoundsSeconds, "30s") { onDraftChange(draft.copy(restBetweenRoundsSeconds = it)) })
        }
        add(DetailRow("RPE", draft.rpe, "8") { onDraftChange(draft.copy(rpe = it)) })
    }

    FormSection(caption = "DETAILS") {
        rows.forEachIndexed { index, row ->
            SettingsRow(
                label = row.label,
                value = row.value,
                placeholder = row.placeholder,
                onValueChange = row.onChange,
            )
            if (index < rows.lastIndex) FormDivider()
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = colors.textPrimary,
        )
        Spacer(modifier = Modifier.width(12.dp))
        InlineField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun GroupExercisesSection(
    draft: TrainingBlockDraft,
    onDraftChange: (TrainingBlockDraft) -> Unit,
) {
    val color = TrainingProtocolColors.colorFor(draft.protocol.name, LocalAppColors.current.accent)

    FormSection(caption = "EXERCISES") {
        draft.exercises.forEachIndexed { index, exercise ->
            GroupExerciseEditor(
                index = index,
                exercise = exercise,
                color = color,
                canDelete = draft.exercises.size > 1,
                onChange = { changed ->
                    onDraftChange(draft.copy(exercises = draft.exercises.mapIndexed { itemIndex, item -> if (itemIndex == index) changed else item }))
                },
                onDelete = {
                    onDraftChange(draft.copy(exercises = draft.exercises.filterIndexed { itemIndex, _ -> itemIndex != index }))
                },
            )
            FormDivider()
        }
        AddRow(
            text = "Add exercise",
            onClick = { onDraftChange(draft.copy(exercises = draft.exercises + GroupExerciseDraft())) },
        )
    }
}

@Composable
private fun GroupExerciseEditor(
    index: Int,
    exercise: GroupExerciseDraft,
    color: Color,
    canDelete: Boolean,
    onChange: (GroupExerciseDraft) -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalAppColors.current

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .padding(start = 16.dp, end = if (canDelete) 6.dp else 16.dp),
        ) {
            Text(
                text = ('A' + index).toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                modifier = Modifier.width(20.dp),
            )
            InlineField(
                value = exercise.name,
                onValueChange = { onChange(exercise.copy(name = it)) },
                placeholder = "Select exercise",
                modifier = Modifier.weight(1f),
            )
            if (canDelete) {
                IconButton(icon = Lucide.Trash2, tint = colors.textTertiary, onClick = onDelete)
            }
        }
        FormDivider(start = 36.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .padding(start = 36.dp, end = 16.dp),
        ) {
            InlineField(
                value = exercise.reps,
                onValueChange = { onChange(exercise.copy(reps = it)) },
                placeholder = "Reps",
                modifier = Modifier.weight(1f),
            )
            VerticalHairline()
            InlineField(
                value = exercise.load,
                onValueChange = { onChange(exercise.copy(load = it)) },
                placeholder = "Load",
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun SaveButton(
    isBusy: Boolean,
    hasError: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val canClick = enabled && !isBusy

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.accent.copy(alpha = if (enabled) 1f else 0.35f))
            .clickable(enabled = canClick, onClick = onClick),
    ) {
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = colors.background,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Saving…", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.background)
        } else {
            if (!hasError) {
                Icon(Lucide.Check, null, tint = colors.background, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = if (hasError) "Try again" else "Save block",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.background,
            )
        }
    }
}

// Loading
@Composable
private fun ComposerSkeleton() {
    val transition = rememberInfiniteTransition(label = "composerSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "composerSkeletonAlpha",
    )

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        listOf(1, 3, 2).forEach { rows ->
            Column {
                FormSkeletonBar(
                    width = 80.dp,
                    height = 10.dp,
                    alpha = alpha,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                )
                FormSurface {
                    repeat(rows) { index ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp)
                                .padding(horizontal = 16.dp),
                        ) {
                            FormSkeletonBar(width = 48.dp, alpha = alpha)
                            Spacer(modifier = Modifier.weight(1f))
                            FormSkeletonBar(width = 96.dp, alpha = alpha)
                        }
                        if (index < rows - 1) FormDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSkeletonBar(
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

// Shared Components
@Composable
private fun FormSurface(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LocalAppColors.current.surfaceHigh),
        content = content,
    )
}

@Composable
private fun FormSection(
    caption: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        SectionCaption(caption)
        FormSurface(content = content)
    }
}

@Composable
private fun SectionCaption(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.6.sp,
        color = LocalAppColors.current.textTertiary,
        modifier = Modifier.padding(start = 16.dp, bottom = 6.dp),
    )
}

@Composable
private fun FormDivider(start: Dp = 16.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = start)
            .height(0.5.dp)
            .background(LocalAppColors.current.textTertiary.copy(alpha = 0.3f)),
    )
}

@Composable
private fun VerticalHairline() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(0.5.dp)
            .height(18.dp)
            .background(LocalAppColors.current.textTertiary.copy(alpha = 0.4f)),
    )
}

@Composable
private fun AddRow(text: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 44.dp)
            .padding(horizontal = 16.dp),
    ) {
        Icon(Lucide.Plus, null, tint = colors.accent, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.accent)
    }
}

@Composable
private fun InlineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    val colors = LocalAppColors.current

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 15.sp, color = colors.textPrimary, textAlign = textAlign),
        cursorBrush = SolidColor(colors.accent),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = if (textAlign == TextAlign.End) Alignment.CenterEnd else Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, fontSize = 15.sp, color = colors.textTertiary, maxLines = 1)
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun IconButton(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(15.dp))
    }
}

// Previews
@Composable
private fun AddBlockPreviewHost(initial: AddBlockState) {
    var state by remember { mutableStateOf(initial) }

    AppTheme(theme = AppTheme.Dark) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(LocalAppColors.current.background)
                .padding(16.dp),
        ) {
            Text("Session blocks", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textPrimary)
            AddBlockInline(
                state = state,
                onOpen = { state = AddBlockState.Editing() },
                onClose = { state = AddBlockState.Closed },
                onDraftChange = { state = AddBlockState.Editing(it) },
                onSave = { state = AddBlockState.Closed },
            )
        }
    }
}

private val previewStandardDraft = TrainingBlockDraft(
    protocol = TrainingProtocol.TOP_SET,
    exerciseName = "Back Squat",
    sets = listOf(PlannedSetDraft("3", "120kg")),
    restSeconds = "180s",
    rpe = "9",
)

private val previewGroupDraft = TrainingBlockDraft(
    protocol = TrainingProtocol.CIRCUIT,
    rounds = "2",
    restBetweenRoundsSeconds = "30s",
    exercises = listOf(
        GroupExerciseDraft("Pull-ups", "10", "10kg"),
        GroupExerciseDraft("Push-ups", "10", ""),
    ),
)

@Preview
@Composable
private fun AddBlockStandardPreview() = AddBlockPreviewHost(AddBlockState.Editing(previewStandardDraft))

@Preview
@Composable
private fun AddBlockGroupPreview() = AddBlockPreviewHost(AddBlockState.Editing(previewGroupDraft))

@Preview
@Composable
private fun AddBlockSubmittingPreview() = AddBlockPreviewHost(AddBlockState.Submitting(previewStandardDraft))

@Preview
@Composable
private fun AddBlockErrorPreview() = AddBlockPreviewHost(AddBlockState.Editing(previewStandardDraft, error = "Could not save block. Try again."))

@Preview
@Composable
private fun AddBlockClosedPreview() = AddBlockPreviewHost(AddBlockState.Closed)