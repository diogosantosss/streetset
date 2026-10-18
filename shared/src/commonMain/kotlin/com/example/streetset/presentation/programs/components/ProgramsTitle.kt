package com.example.streetset.presentation.programs.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun ProgramsTitle(modifier: Modifier = Modifier) {
    Text(
        text = "Programs",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.textPrimary,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun ProgramsTitlePreview() {
    AppTheme(theme = AppTheme.Dark) {
        ProgramsTitle(
            modifier = Modifier.padding(start = 16.dp, end = 64.dp, top = 8.dp, bottom = 8.dp)
        )
    }
}