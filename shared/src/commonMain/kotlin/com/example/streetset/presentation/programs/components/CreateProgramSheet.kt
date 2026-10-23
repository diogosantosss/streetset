package com.example.streetset.presentation.programs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.presentation.programs.ProgramInput
import com.example.streetset.ui.components.AppTextField
import com.example.streetset.ui.components.DateField
import com.example.streetset.ui.components.ModalCard
import com.example.streetset.ui.theme.LocalAppColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton

@Composable
fun ProgramFormSheet(
    visible: Boolean,
    title: String,
    confirmText: String,
    initialInput: ProgramInput?,
    error: String?,
    onConfirm: (ProgramInput) -> Unit,
    onDismiss: () -> Unit,
    onErrorCleared: () -> Unit = {},
) {
    val colors = LocalAppColors.current

    var input by remember { mutableStateOf(initialInput ?: ProgramInput.initial()) }
    var displayTitle by remember { mutableStateOf(title) }
    var displayConfirmText by remember { mutableStateOf(confirmText) }
    var displayError by remember { mutableStateOf(error) }

    LaunchedEffect(visible) {
        if (visible) {
            input = initialInput ?: ProgramInput.initial()
            displayTitle = title
            displayConfirmText = confirmText
        }
    }

    if (visible) displayError = error

    ModalCard(visible = visible, onDismiss = onDismiss) {
        Text(
            text = displayTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = input.name,
            onValueChange = {
                input = input.copy(name = it)
                if (displayError != null) onErrorCleared()
            },
            label = "Name",
            placeholder = "e.g. Strength Base",
        )

        if (displayError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = displayError ?: "",
                fontSize = 12.sp,
                color = colors.error,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AppTextField(
            value = input.goal,
            onValueChange = { input = input.copy(goal = it) },
            label = "Goal",
            placeholder = "e.g. Build foundational strength",
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateField(
                label = "Start",
                date = input.startDate,
                onDateChange = { input = input.copy(startDate = it) },
                modifier = Modifier.weight(1f),
            )
            DateField(
                label = "End",
                date = input.endDate,
                onDateChange = { input = input.copy(endDate = it) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                border = BorderStroke(1.dp, colors.border.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.textPrimary,
                ),
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { onConfirm(input) },
                enabled = input.isValid,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.textPrimary,
                    disabledContainerColor = colors.accent.copy(alpha = 0.4f),
                    disabledContentColor = colors.textPrimary.copy(alpha = 0.6f),
                ),
            ) {
                Text(displayConfirmText)
            }
        }
    }
}

@Preview
@Composable
fun PreviewProgramFormSheet() {
    ProgramFormSheet(
        visible = true,
        title = "Program Title",
        confirmText = "Confirm",
        initialInput = ProgramInput.initial(),
        error = null,
        onConfirm = {},
        onDismiss = {},
        onErrorCleared = {},
    )
}
