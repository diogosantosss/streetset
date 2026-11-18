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
 * Achromatic palettes: the only colors in the app are the protocol accents
 * ([TrainingProtocolColors]) and success/error. [accent] is near-white on the dark
 * palettes and near-black on the light one, so it works both as a fill and as a
 * text/icon tint. The four palettes share this structure and only differ in the
 * value and temperature of their greys.
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

/** A · Graphite — neutral grey, the default. */
val graphiteColors = AppColors(
    background = Color(0xFF14161A),
    backgroundTop = Color(0xFF1C1E22),
    surface = Color(0xFF202329),
    surfaceHigh = Color(0xFF2A2E35),
    border = Color(0xFF3A3F47),
    textPrimary = Color(0xFFF7F8F9),
    textSecondary = Color(0xFFB0B5BC),
    textTertiary = Color(0xFF868C95),
    accent = Color(0xFFF5F6F7),
    success = Color(0xFF4ADE80),
    onAccent = Color(0xFF0D0E10),
    shadow = Color(0x40000000),
    error = Color(0xFFFF5C5C),
    glow = Color(0x0FFFFFFF),
)

/** B · Slate — cool grey with a hint of blue. */
val slateColors = AppColors(
    background = Color(0xFF12171D),
    backgroundTop = Color(0xFF1B2027),
    surface = Color(0xFF222932),
    surfaceHigh = Color(0xFF2C343E),
    border = Color(0xFF3A4450),
    textPrimary = Color(0xFFF2F5F8),
    textSecondary = Color(0xFFA8B2BF),
    textTertiary = Color(0xFF7E8996),
    accent = Color(0xFFE8EEF5),
    success = Color(0xFF4ADE80),
    onAccent = Color(0xFF0D0E10),
    shadow = Color(0x40000000),
    error = Color(0xFFFF5C5C),
    glow = Color(0x0FFFFFFF),
)

/** C · Warm Ash — grey with a warm, sandy tint. */
val warmAshColors = AppColors(
    background = Color(0xFF151311),
    backgroundTop = Color(0xFF1E1C1A),
    surface = Color(0xFF262320),
    surfaceHigh = Color(0xFF302C28),
    border = Color(0xFF413B35),
    textPrimary = Color(0xFFFAF7F2),
    textSecondary = Color(0xFFB8AFA5),
    textTertiary = Color(0xFF8C8379),
    accent = Color(0xFFF7F3EC),
    success = Color(0xFF4ADE80),
    onAccent = Color(0xFF0D0E10),
    shadow = Color(0x40000000),
    error = Color(0xFFFF5C5C),
    glow = Color(0x0FFFFFFF),
)

/** D · Paper — warm off-white with white surfaces. */
val paperColors = AppColors(
    background = Color(0xFFEDEBE6),
    backgroundTop = Color(0xFFF6F5F2),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFF0EEE9),
    border = Color(0xFFDBD8D1),
    textPrimary = Color(0xFF1A1A19),
    textSecondary = Color(0xFF63635F),
    textTertiary = Color(0xFF8E8B85),
    accent = Color(0xFF1A1B1E),
    success = Color(0xFF12A150),
    onAccent = Color(0xFFFFFFFF),
    shadow = Color(0x1A000000),
    error = Color(0xFFD93A3A),
    glow = Color(0x00000000),
)
