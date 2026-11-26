package com.example.streetset.util

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

/** Shared duration for tab and bottom-bar navigation animations. */
internal const val ScreenTransitionMillis = 280

val bottomBarEnter: EnterTransition = slideInVertically(
    initialOffsetY = { it },
    animationSpec = tween(ScreenTransitionMillis),
) + fadeIn(animationSpec = tween(ScreenTransitionMillis))

val bottomBarExit: ExitTransition = slideOutVertically(
    targetOffsetY = { it },
    animationSpec = tween(ScreenTransitionMillis),
) + fadeOut(animationSpec = tween(ScreenTransitionMillis))

/**
 * Horizontal page-turn slide used for bottom-tab switches.
 *
 * Both screens travel the same distance in opposite directions, which reads as
 * a page turn rather than a crossfade. [forward] follows the tab index order.
 */
internal fun horizontalScreenSlide(forward: Boolean): ContentTransform =
    slideInHorizontally(
        animationSpec = tween(ScreenTransitionMillis),
        initialOffsetX = { width -> if (forward) width else -width },
    ) togetherWith slideOutHorizontally(
        animationSpec = tween(ScreenTransitionMillis),
        targetOffsetX = { width -> if (forward) -width else width },
    )
