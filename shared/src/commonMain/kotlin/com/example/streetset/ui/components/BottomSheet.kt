package com.example.streetset.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.HazeMaterials

private const val SheetAnimationDurationMillis = 300

/**
 * Full-width sheet anchored to the bottom of the screen, with a drag handle.
 *
 * Unlike [ModalCard] it is **not** a [androidx.compose.ui.window.Dialog]: it is
 * rendered as an in-composition overlay so it can sample [hazeState] and blur
 * the content behind it, like the bottom bar does.
 */
@Suppress("DEPRECATION")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    maxHeightFraction: Float = 0.82f,
    canDismiss: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val isLightBackground = colors.background.luminance() > 0.5f

    var render by remember { mutableStateOf(visible) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) render = true

        progress.animateTo(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(SheetAnimationDurationMillis),
        )

        if (!visible) render = false
    }

    if (!render) return

    BackHandler(enabled = canDismiss) { onDismiss() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f * progress.value))
                .pointerInput(canDismiss) {
                    if (canDismiss) {
                        detectTapGestures { onDismiss() }
                    }
                },
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Same blur recipe as the bottom bar so both read as one shared surface.
            val blurStyle = HazeMaterials.thin(colors.background).then { progressive(HazeProgressive.topFade()) }
            val overlayColor = if (isLightBackground) {
                colors.surface.copy(alpha = 0.72f)
            } else {
                colors.surfaceHigh.copy(alpha = 0.45f)
            }
            val shadowColor = if (isLightBackground) colors.shadow else Color.Black

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight * maxHeightFraction)
                    .graphicsLayer { translationY = (1f - progress.value) * size.height }
                    .shadow(16.dp, shape, clip = false, ambientColor = shadowColor, spotColor = shadowColor)
                    .clip(shape)
                    .hazeBlur(input = HazeInput.Backdrop(hazeState), style = blurStyle)
                    .background(overlayColor)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), shape)
                    .navigationBarsPadding()
                    .padding(top = 10.dp)
                    // Swallow taps on the sheet so they don't reach the scrim and dismiss it.
                    .pointerInput(Unit) { detectTapGestures { } }
                    .then(modifier),
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(colors.textTertiary.copy(alpha = 0.4f)),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    content = content,
                )
            }
        }
    }
}
