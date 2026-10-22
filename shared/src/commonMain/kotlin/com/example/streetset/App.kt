package com.example.streetset

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.streetset.navigation.ProgramsRoute
import com.example.streetset.navigation.SettingsRoute
import com.example.streetset.navigation.StatsRoute
import com.example.streetset.navigation.Tab
import com.example.streetset.presentation.programs.ProgramsScreen
import com.example.streetset.presentation.settings.SettingsScreen
import com.example.streetset.presentation.settings.SettingsViewModel
import com.example.streetset.presentation.stats.StatsScreen
import com.example.streetset.ui.components.BottomTabBar
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel

private const val TabTransitionDurationMillis = 300

@Composable
fun App() {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val theme by settingsViewModel.theme.collectAsState()

    AppTheme(theme = theme) {
        StreetSetApp()
    }
}

@Composable
private fun StreetSetApp(
    navController: NavHostController = rememberNavController(),
) {
    val colors = LocalAppColors.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedTab = backStackEntry.toTab()
    val hazeState = rememberHazeState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        NavHost(
            navController = navController,
            startDestination = ProgramsRoute,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { tabTransition(direction = tabDirection(initialState, targetState)).enter },
            exitTransition = { tabTransition(direction = tabDirection(initialState, targetState)).exit },
            popEnterTransition = { tabTransition(direction = tabDirection(initialState, targetState)).enter },
            popExitTransition = { tabTransition(direction = tabDirection(initialState, targetState)).exit },
        ) {
            composable<ProgramsRoute> { ProgramsScreen(hazeState = hazeState) }
            composable<StatsRoute> { StatsScreen() }
            composable<SettingsRoute> { SettingsScreen() }
        }

        BottomTabBar(
            selectedTab = selectedTab ?: Tab.Programs,
            onTabSelected = { tab -> navController.navigateToTab(tab) },
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
        )
    }
}

private fun NavHostController.navigateToTab(tab: Tab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavBackStackEntry?.toTab(): Tab? {
    val destination = this?.destination ?: return null
    return when {
        destination.hasRoute<ProgramsRoute>() -> Tab.Programs
        destination.hasRoute<StatsRoute>() -> Tab.Stats
        destination.hasRoute<SettingsRoute>() -> Tab.Settings
        else -> null
    }
}

private fun tabDirection(initial: NavBackStackEntry, target: NavBackStackEntry): Int {
    val from = initial.toTab()?.ordinal ?: 0
    val to = target.toTab()?.ordinal ?: 0
    return if (to < from) -1 else 1
}

private data class TabTransition(
    val enter: EnterTransition,
    val exit: ExitTransition,
)

private fun tabTransition(direction: Int): TabTransition {
    val offset: (Int) -> Int = { direction * it }
    return TabTransition(
        enter = slideInHorizontally(
            animationSpec = tween(TabTransitionDurationMillis),
            initialOffsetX = offset,
        ) + fadeIn(tween(TabTransitionDurationMillis)),
        exit = slideOutHorizontally(
            animationSpec = tween(TabTransitionDurationMillis),
            targetOffsetX = { -direction * it },
        ) + fadeOut(tween(TabTransitionDurationMillis)),
    )
}
