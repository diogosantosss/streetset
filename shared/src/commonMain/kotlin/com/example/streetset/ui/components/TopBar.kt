package com.example.streetset.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.HazeMaterials

private val TopBarHeight = 56.dp
private val TopBarActionSize = 40.dp
private val HorizontalPadding = 16.dp

object TopBarDefaults {
    val Height = TopBarHeight
}

fun HazeProgressive.Companion.bottomFade(
    startY: Float = 200f,
    endY: Float = 320f,
): HazeProgressive.Brush = HazeProgressive.Brush(
    brush = Brush.verticalGradient(
        colors = listOf(Color.White, Color.Transparent),
        startY = startY,
        endY = endY,
    ),
)

fun HazeProgressive.Companion.topFade(
    startY: Float = 0f,
    endY: Float = 160f,
): HazeProgressive.Brush = HazeProgressive.Brush(
    brush = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.White),
        startY = startY,
        endY = endY,
    ),
)

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    scrolled: Boolean = false,
    hazeState: HazeState? = null,
    actionIcon: ImageVector? = null,
    onActionClick: () -> Unit = {},
) {
    val colors = LocalAppColors.current

    val alignmentBias by animateFloatAsState(
        targetValue = if (scrolled) 0f else -1f,
        animationSpec = tween(240),
        label = "topBarTitleBias",
    )

    val actionAlpha by animateFloatAsState(
        targetValue = if (scrolled) 0f else 1f,
        animationSpec = tween(240),
        label = "topBarActionAlpha",
    )

    val blurModifier = if (hazeState != null) {
        val style = HazeMaterials.thin(colors.background).then {
            progressive(HazeProgressive.bottomFade(startY = 120f, endY = 260f))
        }
        Modifier.hazeBlur(
            input = HazeInput.Backdrop(hazeState),
            style = style,
        )
    } else { Modifier }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(blurModifier),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(TopBarHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .align(BiasAlignment(horizontalBias = alignmentBias, verticalBias = 0f))
                    .padding(horizontal = HorizontalPadding),
            )

            if (actionIcon != null) {
                TopBarAction(
                    imageVector = actionIcon,
                    onClick = onActionClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = HorizontalPadding)
                        .alpha(actionAlpha),
                )
            }
        }
    }
}

@Composable
private fun TopBarAction(
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .size(TopBarActionSize)
            .clip(CircleShape)
            .background(colors.surfaceHigh.copy(alpha = 0.80f))
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview
@Composable
private fun TopBarPreview() {
    AppTheme(theme = AppTheme.Dark) {
        TopBar(
            title = "Programs",
            actionIcon = Lucide.Plus,
        )
    }
}
