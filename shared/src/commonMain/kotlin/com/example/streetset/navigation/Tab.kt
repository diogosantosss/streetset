package com.example.streetset.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChartColumn
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings

enum class Tab(
    val title: String,
    val icon: ImageVector,
    val index: Int,
) {
    Calendar("Calendar", Lucide.Calendar, 0),
    Stats("Stats", Lucide.ChartColumn, 1),
    Settings("Settings", Lucide.Settings, 2),
}
