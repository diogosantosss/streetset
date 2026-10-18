package com.example.streetset.ui.theme

import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val success: Color,
    val onAccent: Color,
    val shadow: Color
)

val darkColors = AppColors(
    background = Color(0xFF1B1B21),
    surface = Color(0xFF26262E),
    surfaceHigh = Color(0xFF31313A),
    border = Color(0xFF3C3C46),
    textPrimary = Color(0xFFF2F2F5),
    textSecondary = Color(0xFFAAAAB4),
    textTertiary = Color(0xFF7F7F8A),
    accent = Color(0xFF0A84FF),
    success = Color(0xFF32D74B),
    onAccent = Color(0xFFFFFFFF),
    shadow = Color(0x26000000)
)

val lightColors = AppColors(
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFE9ECF3),
    border = Color(0xFFD6D9E0),
    textPrimary = Color(0xFF1F2937),
    textSecondary = Color(0xFF4B5563),
    textTertiary = Color(0xFF8A94A6),
    accent = Color(0xFF3B82F6),
    success = Color(0xFF1F9D46),
    onAccent = Color(0xFFFFFFFF),
    shadow = Color(0x20000000)
)
