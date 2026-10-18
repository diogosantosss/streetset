package com.example.streetset.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.ChartColumn
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings

enum class Tab(val title: String, val icon: ImageVector) {
    Programs("Programs", Lucide.Dumbbell),
    Stats("Stats", Lucide.ChartColumn),
    Settings("Settings", Lucide.Settings),
}
