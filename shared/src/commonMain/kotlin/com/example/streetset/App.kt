package com.example.streetset

import androidx.compose.animation.AnimatedVisibility
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
import androidx.navigation.toRoute
import com.example.streetset.navigation.ProgramDetailRoute
import com.example.streetset.navigation.ProgramsRoute
import com.example.streetset.navigation.SessionDetailRoute
import com.example.streetset.navigation.SettingsRoute
import com.example.streetset.navigation.StatsRoute
import com.example.streetset.navigation.Tab
import com.example.streetset.presentation.programdetail.ProgramDetailScreen
import com.example.streetset.presentation.programs.ProgramsScreen
import com.example.streetset.presentation.sessiondetail.SessionDetailScreen
import com.example.streetset.presentation.settings.SettingsScreen
import com.example.streetset.presentation.settings.SettingsViewModel
import com.example.streetset.presentation.stats.StatsScreen
import com.example.streetset.ui.components.BottomTabBar
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.util.StreetSetNav.bottomBarEnter
import com.example.streetset.util.StreetSetNav.bottomBarExit
import com.example.streetset.util.StreetSetNav.enter
import com.example.streetset.util.StreetSetNav.exit
import com.example.streetset.util.StreetSetNav.isDetail
import com.example.streetset.util.StreetSetNav.navigateToTab
import com.example.streetset.util.StreetSetNav.popEnter
import com.example.streetset.util.StreetSetNav.popExit
import com.example.streetset.util.StreetSetNav.tabOrPrograms
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel

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
            enterTransition = enter,
            exitTransition = exit,
            popEnterTransition = popEnter,
            popExitTransition = popExit,
        ) {
            composable<ProgramsRoute> { ProgramsScreen(navController = navController, hazeState = hazeState) }
            composable<StatsRoute> { StatsScreen() }
            composable<SettingsRoute> { SettingsScreen() }
            composable<ProgramDetailRoute> { entry: NavBackStackEntry ->
                val route = entry.toRoute<ProgramDetailRoute>()
                ProgramDetailScreen(
                    programId = route.programId,
                    onBack = { navController.popBackStack() },
                    hazeState = hazeState,
                    onSessionClick = { session ->
                        navController.navigate(SessionDetailRoute(sessionId = session.id ?: 0, title = session.name))
                    },
                )
            }
            composable<SessionDetailRoute> { entry: NavBackStackEntry ->
                val route = entry.toRoute<SessionDetailRoute>()
                SessionDetailScreen(
                    navController = navController,
                    sessionId = route.sessionId,
                    title = route.title,
                    hazeState = hazeState,
                )
            }
        }

        AnimatedVisibility(
            visible = !backStackEntry.isDetail(),
            enter = bottomBarEnter,
            exit = bottomBarExit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
        ) {
            BottomTabBar(
                selectedTab = backStackEntry.tabOrPrograms() ?: Tab.Programs,
                onTabSelected = { tab -> navController.navigateToTab(tab) },
                hazeState = hazeState,
            )
        }
    }
}
