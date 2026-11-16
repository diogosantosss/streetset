package com.example.streetset.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.HazeMaterials
import kotlinx.coroutines.launch
import kotlin.math.max

private const val SheetAnimationDurationMillis = 300

/**
 * Full-width sheet anchored to the bottom of the screen, with a drag handle.
 *
 * Unlike [ModalCard] it is **not** a [androidx.compose.ui.window.Dialog]: it is
 * rendered as an in-composition overlay so it can sample [hazeState] and blur
 * the content behind it, like the bottom bar does.
 *
 * The handle can be grabbed and pulled down to dismiss the sheet.
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
    // Extra downward translation while the user drags the handle.
    val dragOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    // Dragged further than this (or flung faster) and the sheet closes.
    val dismissThreshold = with(LocalDensity.current) { 120.dp.toPx() }

    LaunchedEffect(visible) {
        if (visible) {
            render = true
            dragOffset.snapTo(0f)
        }

        progress.animateTo(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(SheetAnimationDurationMillis),
        )

        if (!visible) {
            render = false
            dragOffset.snapTo(0f)
        }
    }

    if (!render) return

    BackHandler(enabled = canDismiss) { onDismiss() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawRect(Color.Black, alpha = 0.32f * progress.value) }
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

            val dragState = rememberDraggableState { delta ->
                scope.launch { dragOffset.snapTo((dragOffset.value + delta).coerceAtLeast(0f)) }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight * maxHeightFraction)
                    .graphicsLayer {
                        // max(): when closing, the sheet keeps sliding from where the finger left it.
                        translationY = max((1f - progress.value) * size.height, dragOffset.value)
                    }
                    .shadow(16.dp, shape, clip = false, ambientColor = shadowColor, spotColor = shadowColor)
                    .clip(shape)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), shape)
                    // Swallow taps on the sheet so they don't reach the scrim and dismiss it.
                    .pointerInput(Unit) { detectTapGestures { } }
                    .then(modifier),
            ) {
                // The blur sits on its own layer behind the content, so scrolling the
                // list only repaints the content and never re-runs the blur effect.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer()
                        .hazeBlur(
                            input = HazeInput.Backdrop(hazeState),
                            style = blurStyle,
                            performanceMode = HazePerformanceMode.Fixed(0.35f),
                        )
                        .background(overlayColor),
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(top = 2.dp),
                ) {
                    // Grab zone: drag this area down to close the sheet.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .draggable(
                                state = dragState,
                                orientation = Orientation.Vertical,
                                enabled = canDismiss,
                                onDragStopped = { velocity ->
                                    if (dragOffset.value > dismissThreshold || velocity > 1_500f) {
                                        onDismiss()
                                    } else {
                                        scope.launch { dragOffset.animateTo(0f, tween(200)) }
                                    }
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 4.dp)
                                .clip(CircleShape)
                                .background(colors.textTertiary.copy(alpha = 0.4f)),
                        )
                    }

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
}
