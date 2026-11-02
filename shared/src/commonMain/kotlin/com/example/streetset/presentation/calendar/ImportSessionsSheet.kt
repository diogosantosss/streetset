package com.example.streetset.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Lucide
import com.example.streetset.presentation.calendar.importer.ImportParseResult
import com.example.streetset.ui.components.ModalCard
import com.example.streetset.ui.components.ProcessingModal
import com.example.streetset.ui.theme.LocalAppColors
import kotlinx.datetime.LocalDate

@Composable
fun ImportSessionsHost(viewModel: ImportSessionsViewModel) {
    val state by viewModel.state.collectAsState()

    ImportSessionsSheet(
        state = state,
        onTextChange = viewModel::updateText,
        onImport = viewModel::import,
        onDismiss = viewModel::close,
    )
}

@Composable
fun ImportSessionsSheet(
    state: ImportSessionsState,
    onTextChange: (String) -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current

    // Keep the last content so the cards don't empty out while they animate away
    val editing = state as? ImportSessionsState.Editing
    var lastEditing by remember { mutableStateOf<ImportSessionsState.Editing?>(null) }
    if (editing != null) lastEditing = editing
    val shown = editing ?: lastEditing

    val done = state as? ImportSessionsState.Done
    var lastDone by remember { mutableStateOf<ImportSessionsState.Done?>(null) }
    if (done != null) lastDone = done
    val shownDone = done ?: lastDone

    ModalCard(
        visible = state is ImportSessionsState.Editing,
        onDismiss = onDismiss,
    ) {
        Text(
            text = "Import sessions",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )
        Text(
            text = "Paste the rows copied from your spreadsheet",
            fontSize = 13.sp,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = 2.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
        PasteField(value = shown?.text.orEmpty(), onChange = onTextChange)

        if (shown?.text.isNullOrBlank()) {
            Text(
                text = "date, session, block, exercise, protocol, reps, load, rest, rpe, rounds, rest_between, hold, bpm",
                fontSize = 11.sp,
                color = colors.textTertiary,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            ParseSummary(parsed = shown?.parsed ?: ImportParseResult.Empty)
        }

        shown?.error?.let {
            Text(
                text = it,
                fontSize = 13.sp,
                color = colors.error,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        val canImport = shown?.parsed?.isValid == true
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Cancel",
                fontSize = 15.sp,
                color = colors.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Import",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.background,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accent.copy(alpha = if (canImport) 1f else 0.35f))
                    .clickable(enabled = canImport, onClick = onImport)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }

    ModalCard(
        visible = state is ImportSessionsState.Done,
        onDismiss = onDismiss,
    ) {
        val added = shownDone?.added ?: 0
        val replaced = shownDone?.replaced ?: 0

        Text(
            text = "Import finished",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )
        if (added > 0) {
            Text(
                text = "$added ${if (added == 1) "session" else "sessions"} added",
                fontSize = 15.sp,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        if (replaced > 0) {
            Text(
                text = "$replaced ${if (replaced == 1) "session" else "sessions"} replaced",
                fontSize = 15.sp,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = if (added > 0) 4.dp else 12.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Done",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.background,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accent)
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }

    if (state is ImportSessionsState.Importing) {
        ProcessingModal(text = "Importing sessions...")
    }
}

@Composable
private fun PasteField(value: String, onChange: (String) -> Unit) {
    val colors = LocalAppColors.current

    BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = colors.textPrimary,
            fontFamily = FontFamily.Monospace,
        ),
        cursorBrush = SolidColor(colors.accent),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.background.copy(alpha = 0.5f))
            .padding(12.dp),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text("Paste your rows here", fontSize = 13.sp, color = colors.textTertiary)
                }
                inner()
            }
        },
    )
}

@Composable
private fun ParseSummary(parsed: ImportParseResult) {
    val colors = LocalAppColors.current

    Column(modifier = Modifier.padding(top = 12.dp)) {
        if (parsed.errors.isNotEmpty()) {
            parsed.errors.take(4).forEach { error ->
                Text(
                    text = "Line ${error.line}: ${error.message}",
                    fontSize = 12.sp,
                    color = colors.error,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
            if (parsed.errors.size > 4) {
                Text(
                    text = "and ${parsed.errors.size - 4} more",
                    fontSize = 12.sp,
                    color = colors.textTertiary,
                )
            }
            return@Column
        }

        if (parsed.sessions.isEmpty()) return@Column

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryItem(Lucide.Calendar, "${parsed.sessions.size} ${if (parsed.sessions.size == 1) "session" else "sessions"}")
            SummaryItem(Lucide.Dumbbell, "${parsed.blockCount} ${if (parsed.blockCount == 1) "block" else "blocks"}")
        }
        parsed.sessions.take(3).forEach { session ->
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(
                    text = session.name.ifBlank { "Untitled" },
                    fontSize = 13.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = session.date.shortLabel(), fontSize = 12.sp, color = colors.textSecondary)
            }
        }
        if (parsed.sessions.size > 3) {
            Text(
                text = "and ${parsed.sessions.size - 3} more",
                fontSize = 12.sp,
                color = colors.textTertiary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Text(
            text = "A session with the same name on the same day is replaced.",
            fontSize = 11.sp,
            color = colors.textTertiary,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun SummaryItem(icon: ImageVector, text: String) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, null, tint = colors.textTertiary, modifier = Modifier.size(13.dp))
        Text(text, fontSize = 13.sp, color = colors.textSecondary)
    }
}

private fun LocalDate.shortLabel(): String = "$day ${month.displayName().take(3)} $year"
