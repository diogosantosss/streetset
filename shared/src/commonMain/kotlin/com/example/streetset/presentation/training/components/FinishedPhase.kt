package com.example.streetset.presentation.training.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun FinishedPhase(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Text(
        text = "Workout finished",
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.textPrimary,
        modifier = modifier.padding(vertical = 48.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun FinishedPhasePreview() {
    FinishedPhase()
}
