package com.example.streetset.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.streetset.ui.theme.LocalAppColors

private const val ModalAnimationDurationMillis = 300

@Composable
fun ModalCard(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(24.dp)

    var render by remember { mutableStateOf(visible) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) render = true
        progress.animateTo(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(ModalAnimationDurationMillis),
        )
        if (!visible) render = false
    }

    if (render) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f * progress.value))
                        .pointerInput(Unit) { detectTapGestures { onDismiss() } },
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .graphicsLayer {
                            alpha = progress.value
                            val scale = 0.92f + 0.08f * progress.value
                            scaleX = scale
                            scaleY = scale
                        }
                        .shadow(24.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
                        .clip(shape)
                        .background(colors.surfaceHigh.copy(alpha = 0.82f))
                        .border(1.dp, Color.White.copy(alpha = 0.20f), shape)
                        .padding(24.dp)
                        .pointerInput(Unit) { detectTapGestures { } }
                        .then(modifier),
                    content = content,
                )
            }
        }
    }
}
