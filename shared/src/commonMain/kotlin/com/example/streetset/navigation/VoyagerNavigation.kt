@file:OptIn(ExperimentalVoyagerApi::class)

package com.example.streetset.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab as VoyagerTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.streetset.ui.components.BottomTabBar
import com.example.streetset.util.bottomBarEnter
import com.example.streetset.util.bottomBarExit
import com.example.streetset.util.horizontalScreenSlide
import dev.chrisbanes.haze.HazeState

/**
 * Entry point for StreetSet navigation.
 *
 * Uses Voyager's [TabNavigator] for the three bottom tabs (Calendar, Stats, Settings).
 * A `TabNavigator` deliberately has no back stack of its own: tabs are siblings.
 * Each tab therefore hosts its own nested [Navigator], so pushing a session detail
 * or a workout only affects that tab.
 * Switching tabs preserves the other tabs' stacks and scroll positions.
 *
 * [hazeState] is provided through [LocalHaze] instead of being passed as a
 * parameter to every [Screen]. Screens stay simple declarations and can request
 * the blur state only when they need it.
 */
@Composable
internal fun StreetSetNavigation(hazeState: HazeState) {
    CompositionLocalProvider(LocalHaze provides hazeState) {
        TabNavigator(CalendarVoyagerTab) {
            StreetSetTabs()
        }
    }
}

/**
 * Shared contract for the three bottom tabs.
 *
 * Extending Voyager's [VoyagerTab] with [uiTab] lets us derive the tab options
 * from the existing [Tab] enum: index, title and ordering have one source of
 * truth. That is why none of the tab objects repeat a title or an index.
 */
internal sealed interface StreetSetTab : VoyagerTab {
    /** The matching entry in the UI enum, used by [BottomTabBar] and the slide direction. */
    val uiTab: Tab

    /** Derived from [uiTab]; `icon` is null because [BottomTabBar] draws the icons. */
    override val options: TabOptions
        @Composable get() = TabOptions(
            index = uiTab.index.toUShort(),
            title = uiTab.title,
            icon = null,
        )
}

private data object CalendarVoyagerTab : StreetSetTab {
    override val uiTab = Tab.Calendar

    @Composable
    override fun Content() = ShowRootScreen(CalendarHomeScreen)
}

private data object StatsVoyagerTab : StreetSetTab {
    override val uiTab = Tab.Stats

    @Composable
    override fun Content() = ShowRootScreen(StatsHomeScreen)
}

private data object SettingsVoyagerTab : StreetSetTab {
    override val uiTab = Tab.Settings

    @Composable
    override fun Content() = ShowRootScreen(SettingsHomeScreen)
}

/**
 * Hosts one nested navigator per tab.
 *
 * Each tab starts at a fixed root screen. The nested navigator handles its own
 * back presses, so Android's back button and the TopBar back arrow both pop only
 * the current tab's stack. Screens are rendered with [SlideTransition], so
 * entering or leaving a session slides horizontally instead of snapping.
 *
 * The navigator's `canPop` is published to [LocalTabCanPop] keyed by this tab,
 * which lets the bottom bar hide while a detail/workout screen is visible. It is
 * written in a [SideEffect] so the map always reflects committed navigation
 * state, and keying by tab prevents an outgoing tab from hiding the bar of the
 * incoming one.
 */
@Composable
private fun StreetSetTab.ShowRootScreen(screen: Screen) {
    val tabCanPop = LocalTabCanPop.current

    Navigator(
        screen = screen,
        disposeBehavior = NavigatorDisposeBehavior(disposeSteps = false),
    ) { navigator ->
        // Read during composition so a stack change recomposes this scope and
        // the SideEffect publishes the new value to the shared tab map.
        val canPop = navigator.canPop
        SideEffect { tabCanPop[this@ShowRootScreen] = canPop }

        SlideTransition(
            navigator = navigator,
            disposeScreenAfterTransitionEnd = true,
        )
    }
}

/**
 * Renders the selected tab and the shared bottom bar.
 *
 * The bar is deliberately a sibling of [AnimatedContent], not a child: it stays
 * pinned to the bottom while the tab content slides. Its visibility is derived
 * from [LocalTabCanPop] for the *active* tab only, so the bar is shown on the
 * three home screens and hidden on session detail/workout screens, even when
 * another tab still has a pushed stack.
 *
 * [TabNavigator.saveableState] keeps each tab's subtree across switches, so
 * returning to a tab restores its scroll position and stack.
 */
@Composable
private fun StreetSetTabs() {
    val tabNavigator = LocalTabNavigator.current
    val activeTab = checkNotNull(tabNavigator.current as? StreetSetTab) {
        "Unsupported Voyager tab: ${tabNavigator.current}"
    }
    val tabCanPop = remember { mutableStateMapOf<StreetSetTab, Boolean>() }

    CompositionLocalProvider(LocalTabCanPop provides tabCanPop) {
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = { tabSlideSpec(this) },
                label = "tab_content",
            ) { tab ->
                tabNavigator.saveableState(key = "tab", tab = tab) {
                    tab.Content()
                }
            }

            AnimatedVisibility(
                visible = tabCanPop[activeTab] != true,
                enter = bottomBarEnter,
                exit = bottomBarExit,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding(),
            ) {
                BottomTabBar(
                    selectedTab = activeTab.uiTab,
                    onTabSelected = { tab -> tabNavigator.current = tab.toVoyagerTab() },
                    hazeState = LocalHaze.current,
                )
            }
        }
    }
}

/**
 * Builds the tab transition from the tab order.
 *
 * The actual animation lives in [horizontalScreenSlide] so all navigation
 * animations share one duration and one specification.
 */
private fun tabSlideSpec(
    scope: AnimatedContentTransitionScope<StreetSetTab>,
) = horizontalScreenSlide(
    forward = scope.targetState.uiTab.index >= scope.initialState.uiTab.index,
)

/** Maps a [BottomTabBar] selection to its Voyager tab instance. */
private fun Tab.toVoyagerTab(): StreetSetTab = when (this) {
    Tab.Calendar -> CalendarVoyagerTab
    Tab.Stats -> StatsVoyagerTab
    Tab.Settings -> SettingsVoyagerTab
}
