package com.example.streetset.presentation.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Trash2
import com.example.streetset.data.dao.SessionLogDao
import com.example.streetset.presentation.stats.HistoryOptionsState
import com.example.streetset.presentation.stats.HistoryOptionsViewModel
import com.example.streetset.ui.components.AppTextField
import com.example.streetset.ui.components.ConfirmSheet
import com.example.streetset.ui.components.ModalCard
import com.example.streetset.ui.components.ProcessingModal
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun HistorySheets(
    state: HistoryOptionsState,
    viewModel: HistoryOptionsViewModel,
) {
    val colors = LocalAppColors.current

    // Keep the last entry/state so the card content does not vanish while it animates out
    var optionsEntry by remember { mutableStateOf<SessionLogDao.HistoryEntry?>(null) }
    (state as? HistoryOptionsState.Options)?.let { optionsEntry = it.entry }
    ModalCard(visible = state is HistoryOptionsState.Options, onDismiss = viewModel::dismiss) {
        optionsEntry?.let { entry ->
            Text(
                text = entry.sessionName,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            SheetAction(
                icon = Lucide.Pencil,
                label = "Edit notes",
                tint = colors.textPrimary,
                onClick = viewModel::requestEdit,
            )
            SheetAction(
                icon = Lucide.Trash2,
                label = "Delete",
                tint = colors.error,
                onClick = viewModel::requestDelete,
            )
        }
    }

    var editing by remember { mutableStateOf<HistoryOptionsState.Editing?>(null) }
    (state as? HistoryOptionsState.Editing)?.let { editing = it }
    ModalCard(visible = state is HistoryOptionsState.Editing, onDismiss = viewModel::dismiss) {
        editing?.let { current ->
            Text(
                text = "Edit notes",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(16.dp))
            AppTextField(
                value = current.notes,
                onValueChange = viewModel::onNotesChange,
                label = "Notes",
                placeholder = "How did it feel? PRs, tweaks, anything for next time...",
                singleLine = false,
                minHeight = 120.dp,
            )
            current.error?.let { error ->
                Text(
                    text = error,
                    fontSize = 13.sp,
                    color = colors.error,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = viewModel::dismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = BorderStroke(1.dp, colors.border.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = viewModel::saveNotes,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.onAccent,
                    ),
                ) {
                    Text("Save")
                }
            }
        }
    }

    val confirmDelete = state as? HistoryOptionsState.ConfirmDelete
    ConfirmSheet(
        visible = confirmDelete != null,
        title = "Delete workout?",
        message = listOfNotNull(
            "This removes the workout from your history. This action cannot be undone.",
            confirmDelete?.error,
        ).joinToString("\n\n"),
        confirmText = "Delete",
        isDestructive = true,
        onConfirm = viewModel::confirmDelete,
        onDismiss = viewModel::dismiss,
    )

    if (state is HistoryOptionsState.Deleting) {
        ProcessingModal(text = "Deleting workout...")
    }
}

@Composable
private fun SheetAction(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 16.sp, color = tint)
    }
}
