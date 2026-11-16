package com.example.streetset.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Shared semantic palette for the training protocols shown throughout a session. */
object TrainingProtocolColors {
    val warmUp = Color(0xFFF59E0B)
    val topSet = Color(0xFFEF4444)
    val backOff = Color(0xFF60A5FA)
    val superset = Color(0xFFA78BFA)
    val circuit = Color(0xFF2DD4BF)
    val hold = Color(0xFF22C55E)
    val emom = Color(0xFF6AD09C)
    val metronome = Color(0xFFFB923C)

    fun colorFor(protocol: String, accent: Color): Color = when (
        protocol.lowercase().replace("_", "").replace("-", "").replace(" ", "")
    ) {
        "warmup" -> warmUp
        "topset" -> topSet
        "backoff" -> backOff
        "superset" -> superset
        "circuit" -> circuit
        "hold" -> hold
        "emom" -> emom
        "metronome" -> metronome
        else -> accent
    }
}

/**
 * Achromatic palette: the only colors in the app are the protocol accents
 * ([TrainingProtocolColors]) and success/error. [accent] is near-white on dark and
 * near-black on light, so it works both as a fill and as a text/icon tint.
 */
data class AppColors(
    val background: Color,
    val backgroundTop: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val success: Color,
    val onAccent: Color,
    val shadow: Color,
    val error: Color,
    val glow: Color,
)

/** Very subtle vertical wash used behind every screen. */
val AppColors.backgroundBrush: Brush
    get() = Brush.verticalGradient(listOf(backgroundTop, background))

val darkColors = AppColors(
    background = Color(0xFF0B0C0E),
    backgroundTop = Color(0xFF131519),
    surface = Color(0xFF141518),
    surfaceHigh = Color(0xFF1C1E22),
    border = Color(0xFF2B2E33),
    textPrimary = Color(0xFFF4F5F6),
    textSecondary = Color(0xFF9A9FA6),
    textTertiary = Color(0xFF6A6F76),
    accent = Color(0xFFEDEEF0),
    success = Color(0xFF4ADE80),
    onAccent = Color(0xFF0D0E10),
    shadow = Color(0x40000000),
    error = Color(0xFFFF5C5C),
    glow = Color(0x0FFFFFFF),
)

val lightColors = AppColors(
    background = Color(0xFFF7F7F8),
    backgroundTop = Color(0xFFF7F7F8),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFEFEFF1),
    border = Color(0xFFE2E2E5),
    textPrimary = Color(0xFF101112),
    textSecondary = Color(0xFF5B5F64),
    textTertiary = Color(0xFF8E9298),
    accent = Color(0xFF1A1B1E),
    success = Color(0xFF12A150),
    onAccent = Color(0xFFFFFFFF),
    shadow = Color(0x1A000000),
    error = Color(0xFFD93A3A),
    glow = Color(0x00000000),
)
