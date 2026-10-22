package com.example.streetset.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppColors = staticCompositionLocalOf { lightColors }

enum class AppTheme(
    val displayName: String,
    val isDark: Boolean,
    val colors: AppColors,
) {
    Dark("Dark", true, darkColors),
    Light("Light", false, lightColors),
    LightWarm("Warm", false, lightWarmColors),
    LightCool("Cool", false, lightCoolColors),
    LightIndigo("Indigo", false, lightIndigoColors),
    DarkGreen("Dark Green", true, darkGreenColors),
    DarkOrange("Dark Orange", true, darkOrangeColors),
    DarkPurple("Dark Purple", true, darkPurpleColors),
    ;

    val valueType: String
        get() = if (isDark) "Dark" else "Light"
}

@Composable
fun AppTheme(
    theme: AppTheme = AppTheme.Dark,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppColors provides theme.colors) {
        MaterialTheme(
            colorScheme = theme.toColorScheme(),
            content = content
        )
    }
}

private fun AppTheme.toColorScheme(): ColorScheme {
    val colors = this.colors
    return if (isDark) {
        darkColorScheme(
            primary = colors.accent,
            background = colors.background,
            surface = colors.surface,
            onPrimary = colors.onAccent,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            outline = colors.border,
            error = colors.error,
            onError = colors.onAccent,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            background = colors.background,
            surface = colors.surface,
            onPrimary = colors.onAccent,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            outline = colors.border,
            error = colors.error,
            onError = colors.onAccent,
        )
    }
}
