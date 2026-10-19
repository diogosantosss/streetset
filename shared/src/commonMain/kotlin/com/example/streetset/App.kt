package com.example.streetset

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.streetset.navigation.Tab
import com.example.streetset.presentation.app.AppState
import com.example.streetset.presentation.app.AppViewModel
import com.example.streetset.presentation.programs.ProgramInput
import com.example.streetset.presentation.programs.ProgramsScreen
import com.example.streetset.presentation.programs.ProgramsViewModel
import com.example.streetset.presentation.programs.components.CreateProgramSheet
import com.example.streetset.presentation.settings.SettingsScreen
import com.example.streetset.presentation.stats.StatsScreen
import com.example.streetset.ui.components.BottomTabBar
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.AppTheme as ThemeEnum
import com.example.streetset.ui.theme.LocalAppColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    AppTheme(theme = ThemeEnum.Dark) {
        AppRoute()
    }
}

@Composable
private fun AppRoute(
    appViewModel: AppViewModel = koinViewModel(),
    programsViewModel: ProgramsViewModel = koinViewModel(),
) {
    val state by appViewModel.state.collectAsStateWithLifecycle()

    AppScreen(
        state = state,
        onTabSelected = appViewModel::selectTab,
        onAddProgram = appViewModel::showCreateProgram,
        onDismissProgram = appViewModel::dismissCreateProgram,
        onCreateProgram = { input: ProgramInput ->
            programsViewModel.createProgram(input)
            appViewModel.dismissCreateProgram()
        },
        programsViewModel = programsViewModel,
    )
}

@Composable
private fun AppScreen(
    state: AppState,
    onTabSelected: (Tab) -> Unit,
    onAddProgram: () -> Unit,
    onDismissProgram: () -> Unit,
    onCreateProgram: (ProgramInput) -> Unit,
    programsViewModel: ProgramsViewModel,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        TabContent(
            selectedTab = state.selectedTab,
            programsViewModel = programsViewModel,
            onAddProgramClick = onAddProgram,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding(),
        ) {
            BottomTabBar(
                selectedTab = state.selectedTab,
                onTabSelected = onTabSelected,
            )
        }

        CreateProgramSheet(
            visible = state.showCreateProgram,
            onDismiss = onDismissProgram,
            onCreate = onCreateProgram,
        )

    }
}

@Composable
private fun TabContent(
    modifier: Modifier = Modifier,
    selectedTab: Tab,
    programsViewModel: ProgramsViewModel,
    onAddProgramClick: () -> Unit,
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    AnimatedContent(
        targetState = selectedTab,
        transitionSpec = { tabTransition() },
        label = "AppTabTransition",
        modifier = modifier,
    ) { tab: Tab ->
        saveableStateHolder.SaveableStateProvider(key = tab.name) {
            when (tab) {
                Tab.Programs -> ProgramsScreen(onAddProgramClick, programsViewModel)
                Tab.Stats -> StatsScreen()
                Tab.Settings -> SettingsScreen()
            }
        }
    }
}

private fun AnimatedContentTransitionScope<Tab>.tabTransition(): ContentTransform {
    val forward = targetState.ordinal > initialState.ordinal
    val enterOffset: (Int) -> Int = { if (forward) it else -it }
    val exitOffset: (Int) -> Int = { if (forward) -it else it }
    val spec = tween<IntOffset>(durationMillis = 300)
    val fadeSpec = tween<Float>(durationMillis = 300)

    return (slideInHorizontally(spec, enterOffset) + fadeIn(fadeSpec)) togetherWith
            (slideOutHorizontally(spec, exitOffset) + fadeOut(fadeSpec))
}