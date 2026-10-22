package com.example.streetset.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.streetset.ui.theme.LocalAppColors

private const val ModalAnimationDurationMillis = 300

@Composable
fun ModalCard(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    canDismiss: Boolean = true,
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
            onDismissRequest = { if (canDismiss) onDismiss() },
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
                        .pointerInput(canDismiss) {
                            if (canDismiss) {
                                detectTapGestures { onDismiss() }
                            }
                        },
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
                        .background(colors.surfaceHigh.copy(alpha = 0.94f))
                        .border(1.dp, colors.border.copy(alpha = 0.6f), shape)
                        .padding(24.dp)
                        .pointerInput(Unit) { }
                        .then(modifier),
                    content = content,
                )
            }
        }
    }
}

@Composable
fun ProcessingModal(text: String) {
    val colors = LocalAppColors.current

    ModalCard(
        visible = true,
        onDismiss = {},
        canDismiss = false,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colors.accent,
                strokeWidth = 2.dp,
            )
            Text(
                text = text,
                fontSize = 16.sp,
                color = colors.textPrimary,
            )
        }
    }
}