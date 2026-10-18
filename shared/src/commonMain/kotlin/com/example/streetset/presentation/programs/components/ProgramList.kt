package com.example.streetset.presentation.programs.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.streetset.data.entities.Program
import com.example.streetset.ui.theme.AppTheme
import kotlinx.datetime.LocalDate

@Composable
fun ProgramList(
    modifier: Modifier = Modifier,
    programs: List<Program>,
    onProgramClick: (Program) -> Unit,
    header: @Composable (() -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (header != null) {
            item(key = "header") {
                header()
            }
        }

        items(programs, key = { it.id ?: it.hashCode() }) { program ->
            ProgramCard(
                program = program,
                onClick = { onProgramClick(program) }
            )
        }
    }
}

@Preview
@Composable
private fun ProgramListPreview() {
    AppTheme(theme = AppTheme.Dark) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1B1B21))
        ) {
            ProgramList(
                programs = listOf(
                    Program(1, "Strength Base", "Build strength", LocalDate(2026, 10, 20), LocalDate(2026, 11, 17), 4),
                    Program(2, "Hypertrophy", "Muscle gain", LocalDate(2026, 11, 20), LocalDate(2026, 12, 17), 4),
                ),
                onProgramClick = {},
                header = { ProgramsTitle() },
            )
        }
    }
}