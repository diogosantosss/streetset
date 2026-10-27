package com.example.streetset.presentation.sessiondetail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Trash2
import com.example.streetset.presentation.sessiondetail.BlockOptionsState
import com.example.streetset.ui.components.ConfirmSheet
import com.example.streetset.ui.components.ModalCard
import com.example.streetset.ui.components.ProcessingModal
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun BlockOptionsSheet(
    state: BlockOptionsState,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    val error = (state as? BlockOptionsState.ConfirmDelete)?.error

    ModalCard(
        visible = state is BlockOptionsState.Options,
        onDismiss = onDismiss,
    ) {
        Text(
            text = "Block options",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onEdit)
                .padding(horizontal = 4.dp, vertical = 12.dp),
        ) {
            Icon(
                imageVector = Lucide.Pencil,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text("Edit", fontSize = 16.sp, color = colors.textPrimary)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onDelete)
                .padding(horizontal = 4.dp, vertical = 12.dp),
        ) {
            Icon(
                imageVector = Lucide.Trash2,
                contentDescription = null,
                tint = colors.error,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text("Delete", fontSize = 16.sp, color = colors.error)
        }
    }

    ConfirmSheet(
        visible = state is BlockOptionsState.ConfirmDelete,
        title = "Delete block?",
        message = listOfNotNull(
            "This block and its sets will be deleted.",
            error,
        ).joinToString("\n\n"),
        confirmText = "Delete",
        isDestructive = true,
        onConfirm = onConfirmDelete,
        onDismiss = onDismiss,
    )

    if (state is BlockOptionsState.Deleting) {
        ProcessingModal(text = "Deleting block...")
    }
}