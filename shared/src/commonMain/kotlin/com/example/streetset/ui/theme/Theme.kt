package com.example.streetset.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppColors = staticCompositionLocalOf { graphiteColors }

enum class AppTheme(
    val displayName: String,
    val colors: AppColors,
    val isDark: Boolean,
) {
    Graphite("Graphite", graphiteColors, true),
    Slate("Slate", slateColors, true),
    WarmAsh("Warm Ash", warmAshColors, true),
    Paper("Paper", paperColors, false),
}

@Composable
fun AppTheme(
    theme: AppTheme = AppTheme.Graphite,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppColors provides theme.colors) {
        MaterialTheme(
            colorScheme = theme.colors.toColorScheme(theme.isDark),
            content = content
        )
    }
}

private fun AppColors.toColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = accent,
            background = background,
            surface = surface,
            onPrimary = onAccent,
            onBackground = textPrimary,
            onSurface = textPrimary,
            outline = border,
            error = error,
            onError = onAccent,
        )
    } else {
        lightColorScheme(
            primary = accent,
            background = background,
            surface = surface,
            onPrimary = onAccent,
            onBackground = textPrimary,
            onSurface = textPrimary,
            outline = border,
            error = error,
            onError = onAccent,
        )
    }
}
