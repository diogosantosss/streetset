package com.example.streetset.util

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.streetset.navigation.ProgramDetailRoute
import com.example.streetset.navigation.ProgramsRoute
import com.example.streetset.navigation.SessionDetailRoute
import com.example.streetset.navigation.SettingsRoute
import com.example.streetset.navigation.StatsRoute
import com.example.streetset.navigation.Tab


private const val TabTransitionDurationMillis = 300

object StreetSetNav {

    // Routes / Tabs
    fun NavHostController.navigateToTab(tab: Tab) {
        navigate(tab.route) {
            popUpTo(graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun NavBackStackEntry?.isDetail(): Boolean =
        this?.destination?.hasRoute<ProgramDetailRoute>() == true ||
            this?.destination?.hasRoute<SessionDetailRoute>() == true

    fun NavBackStackEntry?.toTab(): Tab? = when {
        this == null -> null
        destination.hasRoute<ProgramsRoute>() -> Tab.Programs
        destination.hasRoute<StatsRoute>() -> Tab.Stats
        destination.hasRoute<SettingsRoute>() -> Tab.Settings
        else -> null
    }

    fun NavBackStackEntry?.tabOrPrograms(): Tab? =
        toTab() ?: if (isDetail()) Tab.Programs else null


    // Bottom bar transitions
    val bottomBarEnter: EnterTransition = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(TabTransitionDurationMillis),
    ) + fadeIn(animationSpec = tween(TabTransitionDurationMillis))

    val bottomBarExit: ExitTransition = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(TabTransitionDurationMillis),
    ) + fadeOut(animationSpec = tween(TabTransitionDurationMillis))

    // Tabs transitions
    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        when {
            enteringDetail() || enteringDetailFromDetail() -> slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(TabTransitionDurationMillis),
            )
            else -> tabTransition(tabDirection()).enter
        }
    }

    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        when {
            enteringDetail() || enteringDetailFromDetail() -> slideOutHorizontally(
                targetOffsetX = { -it / 4 },
                animationSpec = tween(TabTransitionDurationMillis),
            )
            else -> tabTransition(tabDirection()).exit
        }
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        when {
            leavingDetail() -> slideInHorizontally(
                initialOffsetX = { -it / 4 },
                animationSpec = tween(TabTransitionDurationMillis),
            )
            returningFromDetail() -> slideInHorizontally(
                initialOffsetX = { -it / 4 },
                animationSpec = tween(TabTransitionDurationMillis),
            )
            else -> tabTransition(-tabDirection()).enter
        }
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        when {
            leavingDetail() -> slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(TabTransitionDurationMillis),
            )
            returningFromDetail() -> slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(TabTransitionDurationMillis),
            )
            else -> tabTransition(-tabDirection()).exit
        }
    }

    // Helpers
    private fun AnimatedContentTransitionScope<NavBackStackEntry>.enteringDetail() =
        !initialState.isDetail() && targetState.isDetail()

    private fun AnimatedContentTransitionScope<NavBackStackEntry>.enteringDetailFromDetail() =
        initialState.isDetail() && targetState.isDetail() && initialState.destination.route != targetState.destination.route

    private fun AnimatedContentTransitionScope<NavBackStackEntry>.leavingDetail() =
        initialState.isDetail() && !targetState.isDetail()

    private fun AnimatedContentTransitionScope<NavBackStackEntry>.returningFromDetail() =
        initialState.isDetail() && targetState.isDetail() && initialState.destination.route != targetState.destination.route


    private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabDirection(): Int {
        val from = initialState.tabOrPrograms()?.ordinal ?: 0
        val to = targetState.tabOrPrograms()?.ordinal ?: 0
        return if (to >= from) 1 else -1
    }

    private fun tabTransition(direction: Int) = TabTransition(
        enter = slideInHorizontally(
            animationSpec = tween(TabTransitionDurationMillis),
            initialOffsetX = { direction * it },
        ) + fadeIn(tween(TabTransitionDurationMillis)),
        exit = slideOutHorizontally(
            animationSpec = tween(TabTransitionDurationMillis),
            targetOffsetX = { -direction * it },
        ) + fadeOut(tween(TabTransitionDurationMillis)),
    )

    private data class TabTransition(
        val enter: EnterTransition,
        val exit: ExitTransition,
    )
}
