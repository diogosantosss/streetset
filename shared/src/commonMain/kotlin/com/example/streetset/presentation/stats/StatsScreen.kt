package com.example.streetset.presentation.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.streetset.presentation.stats.components.ActivitySection
import com.example.streetset.presentation.stats.components.EmptyStats
import com.example.streetset.presentation.stats.components.MixSection
import com.example.streetset.presentation.stats.components.StatsError
import com.example.streetset.presentation.stats.components.StatsSkeleton
import com.example.streetset.presentation.stats.components.SummaryRow
import com.example.streetset.presentation.stats.components.TopExercisesSection
import com.example.streetset.presentation.stats.components.WeeksSection
import com.example.streetset.ui.components.BOTTOM_BAR_HEIGTH
import com.example.streetset.ui.components.TopBar
import com.example.streetset.ui.components.TopBarDefaults
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StatsScreen(
    hazeState: HazeState,
    viewModel: StatsViewModel = koinViewModel<StatsViewModel>(),
) {
    val colors = LocalAppColors.current
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    val scrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val success = state is StatsState.Success

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + TopBarDefaults.Height + 16.dp,
                bottom = BOTTOM_BAR_HEIGTH,
            ),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
        ) {
            when (val current = state) {
                StatsState.Loading -> item { StatsSkeleton() }

                is StatsState.Error -> item { StatsError(current.error, viewModel::retry) }

                is StatsState.Success -> {
                    val stats = current.stats
                    item(key = "summary") { SummaryRow(stats) }
                    item(key = "activity") { ActivitySection(stats) }

                    if (stats.totalSessions == 0) {
                        item(key = "empty") { EmptyStats() }
                    } else {
                        item(key = "weeks") { WeeksSection(stats.weeks) }
                        if (stats.mix.isNotEmpty()) item(key = "mix") { MixSection(stats.mix) }
                        if (stats.topExercises.isNotEmpty()) item(key = "top") { TopExercisesSection(stats.topExercises) }
                    }
                }
            }
        }

        TopBar(
            title = "Stats",
            scrolled = success && scrolled,
            hazeState = if (success) hazeState else null,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}
