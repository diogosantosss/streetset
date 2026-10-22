package com.example.streetset.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun ConfirmSheet(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current

    var displayMessage by remember { mutableStateOf(message) }
    if (visible) displayMessage = message

    ModalCard(visible = visible, onDismiss = onDismiss) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = displayMessage,
            fontSize = 14.sp,
            color = colors.textSecondary,
        )

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
                Text(cancelText)
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = if (isDestructive) {
                    ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = colors.textPrimary,
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.textPrimary,
                    )
                },
            ) {
                Text(confirmText)
            }
        }
    }
}

@Preview
@Composable
fun ConfirmSheetPreview() {
    ConfirmSheet(
        visible = true,
        title = "This is a title",
        message = "This is a message",
        confirmText = "Confirm",
        cancelText = "Cancel",
        isDestructive = false,
        onConfirm = {},
        onDismiss = {},
    )
}