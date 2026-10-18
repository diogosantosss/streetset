package com.example.streetset

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.streetset.data.DatabaseSeeder
import com.example.streetset.navigation.Tab
import com.example.streetset.presentation.programs.ProgramsScreen
import com.example.streetset.presentation.settings.SettingsScreen
import com.example.streetset.presentation.stats.StatsScreen
import com.example.streetset.ui.components.BottomTabBar
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.AppTheme as ThemeEnum
import com.example.streetset.ui.theme.LocalAppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun App() {
    AppTheme(theme = ThemeEnum.Light) {
        AppScreen()
    }
}

@Composable
private fun AppScreen() {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.Programs) }
    val seeder: DatabaseSeeder = koinInject()
    val colors = LocalAppColors.current

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) { seeder.seedIfEmpty() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                slideInHorizontally(tween(300)) { if (forward) it else -it } + fadeIn(tween(300)) togetherWith
                    slideOutHorizontally(tween(300)) { if (forward) -it else it } + fadeOut(tween(300))
            },
            modifier = Modifier.fillMaxSize(),
        ) { tab ->
            when (tab) {
                Tab.Programs -> ProgramsScreen()
                Tab.Stats -> StatsScreen()
                Tab.Settings -> SettingsScreen()
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            BottomTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        }
    }
}
