package com.example.streetset.presentation.training.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.example.streetset.ui.components.ActionButton
import com.example.streetset.ui.components.ActionButtonHeight
import com.example.streetset.ui.components.BottomBar
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeState

@Composable
fun TrainingBottomBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    isSessionPaused: Boolean,
    actionText: String,
    isVisible: Boolean = true,
    showPause: Boolean = true,
    onPauseClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    BottomBar(modifier, hazeState, isVisible) {
        if (showPause) {
            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(ActionButtonHeight)
                    .clip(CircleShape)
                    .background(colors.surfaceHigh.copy(alpha = 0.55f))
                    .border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape),
            ) {
                Icon(
                    imageVector = if (isSessionPaused) Lucide.Play else Lucide.Pause,
                    contentDescription = if (isSessionPaused) "Resume" else "Pause",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        ActionButton(
            text = actionText,
            onClick = onActionClick,
            icon = Lucide.ChevronRight,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
fun TrainingBottomBarPreview() {
    TrainingBottomBar(
        hazeState = HazeState(),
        isSessionPaused = false,
        actionText = "Complete set",
        onPauseClick = {},
        onActionClick = {},
    )
}
