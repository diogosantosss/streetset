package com.example.streetset.presentation.programs.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Trash2
import com.example.streetset.data.entities.Program
import com.example.streetset.ui.components.ModalCard
import com.example.streetset.ui.theme.LocalAppColors
import kotlinx.datetime.LocalDate

@Composable
fun ProgramOptionsSheet(
    visible: Boolean,
    program: Program?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current

    var displayProgram by remember { mutableStateOf(program) }
    if (visible && program != null) displayProgram = program

    ModalCard(visible = visible, onDismiss = onDismiss) {
        Text(
            text = displayProgram?.name ?: "",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        SheetActionItem(
            icon = Lucide.Pencil,
            label = "Edit",
            onClick = onEdit,
        )

        Spacer(modifier = Modifier.height(4.dp))

        SheetActionItem(
            icon = Lucide.Trash2,
            label = "Delete",
            onClick = onDelete,
            tint = colors.error,
        )
    }
}

@Composable
private fun SheetActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null,
) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint ?: colors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = tint ?: colors.textPrimary,
        )
    }
}

@Preview
@Composable
fun PreviewProgramOptionsSheet() {
    ProgramOptionsSheet(
        visible = true,
        program = Program(
            name = "Program Name",
            goal = "This is a goal",
            startDate = LocalDate(2021, 1, 1),
            endDate = LocalDate(2021, 1, 1),
        ),
        onEdit = {},
        onDelete = {},
        onDismiss = {},
    )
}