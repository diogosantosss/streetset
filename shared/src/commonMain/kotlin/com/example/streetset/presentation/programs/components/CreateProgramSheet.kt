package com.example.streetset.presentation.programs.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.presentation.programs.ProgramInput
import com.example.streetset.ui.components.AppTextField
import com.example.streetset.ui.components.DateField
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun CreateProgramSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onCreate: (ProgramInput) -> Unit,
) {
    val colors = LocalAppColors.current

    var input by remember { mutableStateOf(ProgramInput.initial()) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .pointerInput(Unit) { detectTapGestures { onDismiss() } },
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(colors.surface)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.border),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "New Program",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    value = input.name,
                    onValueChange = { input = input.copy(name = it) },
                    label = "Name",
                    placeholder = "e.g. Strength Base",
                )

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
                    ) { Text("Cancel") }

                    Button(
                        onClick = { onCreate(input) },
                        enabled = input.isValid,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) { Text("Create") }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewCreateProgramSheet() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        CreateProgramSheet(
            visible = true,
            onDismiss = {},
            onCreate = {},
        )
    }
}
