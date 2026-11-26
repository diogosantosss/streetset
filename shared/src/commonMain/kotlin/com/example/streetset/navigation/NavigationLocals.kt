package com.example.streetset.navigation

import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.staticCompositionLocalOf
import dev.chrisbanes.haze.HazeState

/**
 * Haze state created once at the root and shared with the tab screens.
 *
 * Screens receive it through this local instead of having it threaded through
 * every screen declaration as a parameter.
 */
internal val LocalHaze = staticCompositionLocalOf<HazeState> { error("HazeState not provided") }

/**
 * Stack state of each bottom tab, keyed by the tab itself.
 *
 * A tab writes its `canPop` here from the nested navigator, and the root reads
 * only the active tab's entry to decide whether the bottom bar is visible.
 * Keying by tab prevents an outgoing tab with a pushed stack from hiding the
 * bar on the incoming tab.
 */
internal val LocalTabCanPop = staticCompositionLocalOf<SnapshotStateMap<StreetSetTab, Boolean>> {
    error("LocalTabCanPop not provided")
}
