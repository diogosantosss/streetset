package com.example.streetset.presentation.training.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.example.streetset.ui.components.BottomBar
import com.example.streetset.ui.components.topFade
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.HazeMaterials

@Composable
fun TrainingBottomBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    isSessionPaused: Boolean,
    actionText: String,
    isVisible: Boolean = true,
    onPauseClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    BottomBar(modifier, hazeState, isVisible) {
        IconButton(
            onClick = onPauseClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.surfaceHigh),
        ) {
            Icon(
                imageVector = if (isSessionPaused) Lucide.Play else Lucide.Pause,
                contentDescription = if (isSessionPaused) "Resume" else "Pause",
                tint = colors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.accent)
                .clickable { onActionClick() }
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = actionText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onAccent,
            )
            Icon(
                imageVector = Lucide.ChevronRight,
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(18.dp),
            )
        }
    }
}

@Preview
@Composable
fun TrainingBottomBarPreview() {
    TrainingBottomBar(
        hazeState = HazeState(),
        isSessionPaused = false,
        actionText = "Start",
        onPauseClick = {},
        onActionClick = {},
    )
}