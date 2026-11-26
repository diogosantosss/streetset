package com.example.streetset.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.streetset.presentation.calendar.CalendarScreen
import com.example.streetset.presentation.sessiondetail.SessionDetailScreen
import com.example.streetset.presentation.settings.SettingsScreen
import com.example.streetset.presentation.stats.StatsScreen
import com.example.streetset.presentation.training.TrainingSessionScreen

/**
 * Calendar root screen.
 *
 * Delegates the click to the nested navigator instead of knowing about route
 * classes: the screen only says "a session was opened", and navigation decides
 * which [Screen] to push.
 */
internal object CalendarHomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CalendarScreen(
            hazeState = LocalHaze.current,
            onSessionClick = { sessionId, title ->
                navigator.push(SessionDetailVoyagerScreen(sessionId, title))
            },
        )
    }
}

// Stats root screen; kept as a declaration so the tab shell owns navigation.
internal object StatsHomeScreen : Screen {
    @Composable
    override fun Content() {
        StatsScreen(hazeState = LocalHaze.current)
    }
}

// Settings root screen.
internal object SettingsHomeScreen : Screen {
    @Composable
    override fun Content() {
        SettingsScreen()
    }
}

/**
 * Session detail screen wrapper.
 *
 * The underlying composable takes callbacks (`onBackClick`, `onStartClick`)
 * rather than a `NavHostController`, keeping it testable and independent of the
 * navigation library.
 */
private data class SessionDetailVoyagerScreen(
    private val sessionId: Int,
    private val title: String,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        SessionDetailScreen(
            sessionId = sessionId,
            title = title,
            hazeState = LocalHaze.current,
            onBackClick = { navigator.pop() },
            onStartClick = {
                navigator.push(TrainingSessionVoyagerScreen(sessionId, title))
            },
        )
    }
}

/**
 * Workout screen wrapper.
 *
 * `onFinish` pops until the Calendar root screen, which clears both this screen
 * and the session detail beneath it in one step, mirroring the old
 * `popBackStack<CalendarRoute>(inclusive = false)` behaviour.
 */
private data class TrainingSessionVoyagerScreen(
    private val sessionId: Int,
    private val title: String,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TrainingSessionScreen(
            sessionId = sessionId,
            title = title,
            hazeState = LocalHaze.current,
            onBack = { navigator.pop() },
            onFinish = { navigator.popUntil { screen -> screen is CalendarHomeScreen } },
        )
    }
}
