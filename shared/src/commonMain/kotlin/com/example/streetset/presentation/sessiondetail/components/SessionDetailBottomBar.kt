package com.example.streetset.presentation.sessiondetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
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
fun SessionDetailBottomBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    title: String,
    isVisible: Boolean = true,
    onStartClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    BottomBar(modifier, hazeState, isVisible) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Start workout",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Text(
                text = title,
                fontSize = 13.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(
            onClick = onStartClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.accent),
        ) {
            Icon(
                imageVector = Lucide.Play,
                contentDescription = "Start workout",
                tint = colors.onAccent,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview
@Composable
fun SessionDetailBottomBarPreview() {
    SessionDetailBottomBar(
        hazeState = HazeState(),
        title = "Test",
        onStartClick = {},
    )
}