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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.components.ModalCard
import com.example.streetset.ui.components.ProcessingModal
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun CreateSessionSheet(
    state: CreateSessionState,
    onNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current

    // Keeps the last form so the card doesn't empty out while it animates away
    val form = state as? CreateSessionState.Form
    var lastForm by remember { mutableStateOf<CreateSessionState.Form?>(null) }
    if (form != null) lastForm = form
    val shown = form ?: lastForm

    ModalCard(
        visible = state is CreateSessionState.Form,
        onDismiss = onDismiss,
    ) {
        Text(
            text = "New session",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )
        Text(
            text = shown?.date?.fullTitle().orEmpty(),
            fontSize = 13.sp,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = 2.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))
        NameField(
            value = shown?.name.orEmpty(),
            onChange = onNameChange,
            onDone = onCreate,
        )

        shown?.error?.let {
            Text(
                text = it,
                fontSize = 13.sp,
                color = colors.error,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
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
            Spacer(modifier = Modifier.padding(start = 8.dp))
            Text(
                text = "Create",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.background,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accent)
                    .clickable(onClick = onCreate)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }

    if (state is CreateSessionState.Creating) {
        ProcessingModal(text = "Creating session...")
    }
}

@Composable
private fun NameField(
    value: String,
    onChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    val colors = LocalAppColors.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 17.sp, color = colors.textPrimary),
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            decorationBox = { inner ->
                Box(modifier = Modifier.padding(vertical = 10.dp)) {
                    if (value.isEmpty()) {
                        Text("Name (optional)", fontSize = 17.sp, color = colors.textTertiary)
                    }
                    inner()
                }
            },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.accent.copy(alpha = 0.6f)),
        )
    }
}