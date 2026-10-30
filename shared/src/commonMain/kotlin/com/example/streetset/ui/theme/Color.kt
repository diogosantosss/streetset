package com.example.streetset.ui.theme

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
    val shadow: Color,
    val error: Color,
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
    shadow = Color(0x26000000),
    error = Color(0xFFFF453A),
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
    shadow = Color(0x20000000),
    error = Color(0xFFD32F2F),
)

val lightWarmColors = AppColors(
    background = Color(0xFFFBF7F0),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFF4EDE2),
    border = Color(0xFFE3D8C8),
    textPrimary = Color(0xFF2A2420),
    textSecondary = Color(0xFF6B6156),
    textTertiary = Color(0xFFA89B8A),
    accent = Color(0xFFD9480F),
    success = Color(0xFF1F9D46),
    onAccent = Color(0xFFFFFFFF),
    shadow = Color(0x1A000000),
    error = Color(0xFFD32F2F),
)

val lightCoolColors = AppColors(
    background = Color(0xFFF5F9F8),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFE8F2EF),
    border = Color(0xFFCFE0DB),
    textPrimary = Color(0xFF12211D),
    textSecondary = Color(0xFF4A5B57),
    textTertiary = Color(0xFF88A29B),
    accent = Color(0xFF059669),
    success = Color(0xFF047857),
    onAccent = Color(0xFFFFFFFF),
    shadow = Color(0x14000000),
    error = Color(0xFFD32F2F),
)

val lightIndigoColors = AppColors(
    background = Color(0xFFF5F5FC),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFEBEBF8),
    border = Color(0xFFD6D6EE),
    textPrimary = Color(0xFF1B1B33),
    textSecondary = Color(0xFF55556E),
    textTertiary = Color(0xFF9394AD),
    accent = Color(0xFF4F46E5),
    success = Color(0xFF1F9D46),
    onAccent = Color(0xFFFFFFFF),
    shadow = Color(0x14000000),
    error = Color(0xFFD32F2F),
)

val darkGreenColors = AppColors(
    background = Color(0xFF1B1B21),
    surface = Color(0xFF26262E),
    surfaceHigh = Color(0xFF31313A),
    border = Color(0xFF3C3C46),
    textPrimary = Color(0xFFF2F2F5),
    textSecondary = Color(0xFFAAAAB4),
    textTertiary = Color(0xFF7F7F8A),
    accent = Color(0xFF30D158),
    success = Color(0xFF32D74B),
    onAccent = Color(0xFF0F2010),
    shadow = Color(0x26000000),
    error = Color(0xFFFF453A),
)

val darkOrangeColors = AppColors(
    background = Color(0xFF1B1B21),
    surface = Color(0xFF26262E),
    surfaceHigh = Color(0xFF31313A),
    border = Color(0xFF3C3C46),
    textPrimary = Color(0xFFF2F2F5),
    textSecondary = Color(0xFFAAAAB4),
    textTertiary = Color(0xFF7F7F8A),
    accent = Color(0xFFFF9F0A),
    success = Color(0xFF32D74B),
    onAccent = Color(0xFF201000),
    shadow = Color(0x26000000),
    error = Color(0xFFFF453A),
)

val darkPurpleColors = AppColors(
    background = Color(0xFF1B1B21),
    surface = Color(0xFF26262E),
    surfaceHigh = Color(0xFF31313A),
    border = Color(0xFF3C3C46),
    textPrimary = Color(0xFFF2F2F5),
    textSecondary = Color(0xFFAAAAB4),
    textTertiary = Color(0xFF7F7F8A),
    accent = Color(0xFFBF5AF2),
    success = Color(0xFF32D74B),
    onAccent = Color(0xFF1F0A2E),
    shadow = Color(0x26000000),
    error = Color(0xFFFF453A),
)
