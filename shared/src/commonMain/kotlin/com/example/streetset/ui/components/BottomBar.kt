package com.example.streetset.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.util.StreetSetNav.bottomBarEnter
import com.example.streetset.util.StreetSetNav.bottomBarExit
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.HazeMaterials


val BOTTOM_BAR_HEIGTH: Dp = 128.dp

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState = HazeState(),
    visible: Boolean = true,
    enter: EnterTransition = bottomBarEnter,
    exit: ExitTransition = bottomBarExit,
    content: @Composable RowScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = enter,
        exit = exit,
        modifier = modifier,
    ) {
        val colors = LocalAppColors.current
        val shape = RoundedCornerShape(28.dp)
        val isLightBackground = colors.background.luminance() > 0.5f
        val material = if (isLightBackground) HazeMaterials.thin(colors.surface) else HazeMaterials.thin(colors.background)
        val blurStyle = material.then { progressive(HazeProgressive.topFade()) }
        val overlayColor = if (isLightBackground) colors.surface.copy(alpha = 0.72f) else colors.surfaceHigh.copy(alpha = 0.45f)
        val shadowColor = if (isLightBackground) colors.shadow else Color.Black

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, shape, clip = false, ambientColor = shadowColor, spotColor = shadowColor)
                .clip(shape)
                .hazeBlur(input = HazeInput.Backdrop(hazeState), style = blurStyle)
                .background(overlayColor)
                .border(1.dp, colors.border.copy(alpha = 0.5f), shape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    BottomBar { Text("Test") }
}