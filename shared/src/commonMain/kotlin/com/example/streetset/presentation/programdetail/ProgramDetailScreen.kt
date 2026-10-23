package com.example.streetset.presentation.programdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.data.entities.Week
import com.example.streetset.ui.components.TopBar
import com.example.streetset.ui.components.TopBarDefaults
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.util.shortDisplay
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProgramDetailScreen(
    hazeState: HazeState,
    programId: Int,
    viewModel: ProgramDetailViewModel = koinViewModel { parametersOf(programId) },
    onBack: () -> Unit,
) {
    val colors = LocalAppColors.current
    val state by viewModel.state.collectAsState()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val listState = rememberLazyListState()

    val scrolled by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset > 0 || listState.firstVisibleItemIndex > 0 }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        when (val current = state) {
            is ProgramDetailState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.accent,
                )
            }

            is ProgramDetailState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = current.message,
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                    )
                }
            }

            is ProgramDetailState.Success -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = TopBarDefaults.Height + statusBarPadding + 12.dp,
                        bottom = 128.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    items(current.weeks) { week ->
                        WeekSection(week = week)
                    }
                }

                TopBar(
                    title = current.program.name,
                    hazeState = hazeState,
                    scrolled = scrolled,
                    showBack = true,
                    onBackClick = onBack,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}

@Composable
private fun WeekSection(week: Week) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Week ${week.number}",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            if (week.startDate != null && week.endDate != null) {
                Text(
                    text = "${week.startDate.shortDisplay()} - ${week.endDate.shortDisplay()}",
                    fontSize = 14.sp,
                    color = colors.textTertiary,
                )
            }
        }

        Text(
            text = "Start creating your sessions",
            fontSize = 14.sp,
            color = colors.textSecondary,
        )
    }
}
