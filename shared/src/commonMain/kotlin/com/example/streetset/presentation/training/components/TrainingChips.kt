package com.example.streetset.presentation.training.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Hourglass
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Repeat
import com.example.streetset.presentation.sessiondetail.label
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors

/** Colored pill with the protocol name, tinted with its protocol color. */
@Composable
fun ProtocolChip(
    protocol: TrainingProtocol,
    modifier: Modifier = Modifier,
    label: String = protocol.label,
) {
    val colors = LocalAppColors.current
    val accent = TrainingProtocolColors.colorFor(protocol.name, colors.accent)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent.copy(alpha = 0.35f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Icon(
            imageVector = protocolIcon(protocol),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp,
            color = accent,
        )
    }
}

/** Neutral pill with an icon and a short value (rest, RPE, BPM...). */
@Composable
fun MetaChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(CircleShape)
            .background(colors.surfaceHigh.copy(alpha = 0.55f))
            .border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary,
            maxLines = 1,
        )
    }
}

fun protocolIcon(protocol: TrainingProtocol): ImageVector = when (protocol) {
    TrainingProtocol.WARM_UP -> Lucide.Flame
    TrainingProtocol.TOP_SET, TrainingProtocol.BACK_OFF, TrainingProtocol.NORMAL -> Lucide.Dumbbell
    TrainingProtocol.EMOM -> Lucide.Repeat
    TrainingProtocol.HOLD -> Lucide.Hourglass
    TrainingProtocol.CIRCUIT -> Lucide.Repeat
    TrainingProtocol.SUPERSET -> Lucide.Layers
    TrainingProtocol.METRONOME -> Lucide.Activity
}
