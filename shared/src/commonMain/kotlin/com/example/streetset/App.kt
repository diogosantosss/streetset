package com.example.streetset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import com.example.streetset.navigation.StreetSetNavigation
import com.example.streetset.presentation.settings.SettingsViewModel
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root composable of StreetSet.
 *
 * Owns only app-wide state: the selected theme and the keep-screen-on preference,
 * both read from [SettingsViewModel]. Everything below this point is navigation,
 * which lives in [StreetSetNavigation]. Keeping those concerns separate means the
 * navigation graph does not need to know about settings, and the theme does not
 * need to know about screens.
 */
@Composable
fun App() {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val theme by settingsViewModel.theme.collectAsState()
    val keepScreenOn by settingsViewModel.keepScreenOn.collectAsState()

    AppTheme(theme = theme) {
        StreetSetApp(keepScreenOn = keepScreenOn)
    }
}

/**
 * Draws the single surface behind the whole app and hosts the navigator.
 *
 * The background is intentionally flat: a shallow vertical gradient produced
 * visible 8-bit banding on iOS, so all screens read [LocalAppColors.background]
 * instead of a brush. [HazeState] is created here and shared with every screen
 * through [LocalHaze], so the TopBar blur reacts to scroll on any tab.
 *
 * [keepScreenOn] is applied to the root box rather than to individual screens so
 * the display stays awake during a workout even if the user navigates away and
 * back, without a flicker when a screen is replaced.
 */
@Composable
private fun StreetSetApp(keepScreenOn: Boolean) {
    val colors = LocalAppColors.current
    val hazeState = rememberHazeState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .then(if (keepScreenOn) Modifier.keepScreenOn() else Modifier),
    ) {
        StreetSetNavigation(hazeState = hazeState)
    }
}
