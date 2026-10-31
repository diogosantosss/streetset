package com.example.streetset.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.navigation.Tab
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.HazeMaterials

@Composable
fun BottomTabBar(
    modifier: Modifier = Modifier,
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    hazeState: HazeState? = null,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(28.dp)
    val isLightBackground = colors.background.luminance() > 0.5f

    val blurModifier = if (hazeState != null) {
        val material = if (isLightBackground) {
            HazeMaterials.thin(colors.surface)
        } else {
            HazeMaterials.thin(colors.background)
        }

        val style = material.then {
            progressive(HazeProgressive.topFade())
        }

        Modifier.hazeBlur(
            input = HazeInput.Backdrop(hazeState),
            style = style,
        )

    } else { Modifier }

    val overlayColor = if (isLightBackground) {
        colors.surface.copy(alpha = 0.72f)
    } else {
        colors.surfaceHigh.copy(alpha = 0.45f)
    }

    val shadowColor = if (isLightBackground) colors.shadow else Color.Black

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = shape,
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .clip(shape)
            .then(blurModifier)
            .background(overlayColor)
            .border(1.dp, colors.border.copy(alpha = 0.5f), shape)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Tab.entries.forEach { tab ->
                val selected = tab == selectedTab

                val scale by animateFloatAsState(
                    if (selected) 1.05f else 1.0f,
                    spring(dampingRatio = 0.5f, stiffness = 300f)
                )

                val color by animateColorAsState(
                    if (selected) colors.accent else colors.textTertiary,
                    spring()
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(16.dp))
                        .pointerInput(tab) {
                            detectTapGestures { onTabSelected(tab) }
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = tab.title,
                        color = color,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun BottomTabBarPreview() {
    AppTheme(theme = AppTheme.Dark) {
        Box(
            modifier = Modifier
                .background(Color(0xFF1B1B21))
                .padding(16.dp)
        ) {
            BottomTabBar(
                selectedTab = Tab.Calendar,
                onTabSelected = {},
            )
        }
    }
}