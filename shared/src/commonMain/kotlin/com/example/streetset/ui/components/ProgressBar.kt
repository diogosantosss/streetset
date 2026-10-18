package com.example.streetset.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(colors.surfaceHigh)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = animatedProgress)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.accent)
        )
    }
}

@Preview
@Composable
private fun ProgressBarPreview() {
    AppTheme(theme = AppTheme.Dark) {
        ProgressBar(progress = 0.5f)
    }
}
