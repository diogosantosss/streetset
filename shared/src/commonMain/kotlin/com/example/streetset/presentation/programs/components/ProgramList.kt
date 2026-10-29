package com.example.streetset.presentation.programs.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.streetset.data.entities.Program
import com.example.streetset.data.dao.ProgramSessionProgress
import com.example.streetset.ui.components.BOTTOM_BAR_HEIGTH
import com.example.streetset.ui.theme.AppTheme
import kotlinx.datetime.LocalDate

@Composable
fun ProgramList(
    modifier: Modifier = Modifier,
    programs: List<Program>,
    sessionProgress: Map<Int, ProgramSessionProgress> = emptyMap(),
    onProgramClick: (Program) -> Unit,
    onProgramLongClick: (Program) -> Unit = onProgramClick,
    state: LazyListState = rememberLazyListState(),
    topPadding: Dp = 16.dp,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = topPadding + statusBarPadding,
            bottom = BOTTOM_BAR_HEIGTH,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(programs, key = { it.id ?: it.hashCode() }) { program ->
            ProgramCard(
                program = program,
                completedSessions = sessionProgress[program.id]?.completedSessions ?: 0,
                totalSessions = sessionProgress[program.id]?.totalSessions ?: 0,
                onClick = { onProgramClick(program) },
                onLongPress = { onProgramLongClick(program) },
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
            )
        }
    }
}
