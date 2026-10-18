package com.example.streetset.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.streetset.ui.theme.LocalAppColors


@Composable
fun Button(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .size(40.dp)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            .background(colors.surfaceHigh.copy(alpha = 0.85f))
            .border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape)
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