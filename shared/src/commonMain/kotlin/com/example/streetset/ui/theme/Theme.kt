package com.example.streetset.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppColors = staticCompositionLocalOf { lightColors }

enum class AppTheme {
    Dark,
    Light
}

private fun AppTheme.toColorScheme() = when (this) {
    AppTheme.Dark -> darkColorScheme(
        primary = darkColors.accent,
        background = darkColors.background,
        surface = darkColors.surface,
        onPrimary = darkColors.onAccent,
        onBackground = darkColors.textPrimary,
        onSurface = darkColors.textPrimary,
        outline = darkColors.border
    )

    AppTheme.Light -> lightColorScheme(
        primary = lightColors.accent,
        background = lightColors.background,
        surface = lightColors.surface,
        onPrimary = lightColors.onAccent,
        onBackground = lightColors.textPrimary,
        onSurface = lightColors.textPrimary,
        outline = lightColors.border
    )
}

@Composable
fun AppTheme(
    theme: AppTheme = AppTheme.Light,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppColors provides theme.toAppColors()) {
        MaterialTheme(
            colorScheme = theme.toColorScheme(),
            content = content
        )
    }
}

private fun AppTheme.toAppColors() = when (this) {
    AppTheme.Dark -> darkColors
    AppTheme.Light -> lightColors
}
