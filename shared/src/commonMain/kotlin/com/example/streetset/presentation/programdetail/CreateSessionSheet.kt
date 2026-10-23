package com.example.streetset.presentation.programdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.components.AppTextField
import com.example.streetset.ui.components.ModalCard
import com.example.streetset.ui.theme.LocalAppColors

private val weekdays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@Composable
fun CreateSessionSheet(
    state: CreateSessionState,
    onInputChange: (SessionInput) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    val form = state as? CreateSessionState.Form

    ModalCard(
        visible = state !is CreateSessionState.Closed,
        onDismiss = onDismiss,
        canDismiss = state !is CreateSessionState.Submitting,
    ) {
        Text("Create Session", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
        Spacer(Modifier.height(16.dp))
        Text("Day", fontSize = 14.sp, color = colors.textSecondary)
        Spacer(Modifier.height(8.dp))
        weekdays.chunked(4).forEach { rowDays ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                rowDays.forEach { day ->
                    val dayNumber = weekdays.indexOf(day) + 1
                    val selected = form?.input?.weekday == dayNumber
                    OutlinedButton(
                        onClick = { form?.let { onInputChange(it.input.copy(weekday = dayNumber)) } },
                        modifier = Modifier.weight(1f).height(42.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        border = BorderStroke(1.dp, if (selected) colors.accent else colors.border.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) colors.accent.copy(alpha = 0.18f) else Color.Transparent,
                            contentColor = colors.textPrimary,
                        ),
                    ) { Text(day.take(3), fontSize = 12.sp) }
                }
                repeat(4 - rowDays.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(4.dp))
        }
        Spacer(Modifier.height(8.dp))
        AppTextField(
            value = form?.input?.name.orEmpty(),
            onValueChange = { value -> form?.let { onInputChange(it.input.copy(name = value)) } },
            label = "Name",
            placeholder = "e.g. Push",
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = form?.input?.type.orEmpty(),
            onValueChange = { value -> form?.let { onInputChange(it.input.copy(type = value)) } },
            label = "Type",
            placeholder = "e.g. Strength",
        )
        form?.error?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, fontSize = 12.sp, color = colors.error)
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onDismiss,
                enabled = state !is CreateSessionState.Submitting,
                modifier = Modifier.weight(1f).height(48.dp),
                border = BorderStroke(1.dp, colors.border.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
            ) { Text("Cancel") }
            Button(
                onClick = onConfirm,
                enabled = form?.input?.isValid == true,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.textPrimary,
                    disabledContainerColor = colors.accent.copy(alpha = 0.4f),
                    disabledContentColor = colors.textPrimary.copy(alpha = 0.6f),
                ),
            ) { Text(if (state is CreateSessionState.Submitting) "Creating…" else "Create") }
        }
    }
}
