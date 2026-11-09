package com.example.streetset.presentation.training.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Volume1
import com.composables.icons.lucide.Volume2
import com.composables.icons.lucide.VolumeX
import com.example.streetset.ui.components.ModalCard
import com.example.streetset.ui.theme.LocalAppColors
import kotlin.math.roundToInt

@Composable
fun VolumeSheet(
    visible: Boolean,
    volume: Int,
    onVolumeChange: (Int) -> Unit,
    onVolumeChangeFinished: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current

    ModalCard(visible = visible, onDismiss = onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Sound",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$volume%",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Icon(Lucide.VolumeX, null, tint = colors.textTertiary, modifier = Modifier.size(18.dp))
            Slider(
                value = volume.toFloat(),
                onValueChange = { onVolumeChange(it.roundToInt()) },
                onValueChangeFinished = onVolumeChangeFinished,
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = colors.accent,
                    activeTrackColor = colors.accent,
                    inactiveTrackColor = colors.textTertiary.copy(alpha = 0.25f),
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )
            Icon(Lucide.Volume2, null, tint = colors.textTertiary, modifier = Modifier.size(18.dp))
        }
    }
}

/** The icon for the top bar button, which follows the level. */
fun volumeIcon(volume: Int): ImageVector = when {
    volume <= 0 -> Lucide.VolumeX
    volume < 50 -> Lucide.Volume1
    else -> Lucide.Volume2
}

@Preview
@Composable
private fun VolumeSheetPreview() {
    VolumeSheet(
        visible = true,
        volume = 50,
        onVolumeChange = {},
        onVolumeChangeFinished = {},
        onDismiss = {},
    )
}