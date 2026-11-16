package com.example.streetset.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppColors = staticCompositionLocalOf { lightColors }

enum class AppTheme(val displayName: String) {
    System("System"),
    Light("Light"),
    Dark("Dark"),
}

@Composable
fun AppTheme(
    theme: AppTheme = AppTheme.Dark,
    content: @Composable () -> Unit
) {
    val isDark = when (theme) {
        AppTheme.System -> isSystemInDarkTheme()
        AppTheme.Light -> false
        AppTheme.Dark -> true
    }
    val colors = if (isDark) darkColors else lightColors

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(isDark),
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
