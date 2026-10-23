package com.example.streetset.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.ChartColumn
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings

enum class Tab(
    val title: String,
    val icon: ImageVector,
    val route: Any
) {
    Programs("Programs", Lucide.Dumbbell, ProgramsRoute),
    Stats("Stats", Lucide.ChartColumn, StatsRoute),
    Settings("Settings", Lucide.Settings, SettingsRoute);
}
