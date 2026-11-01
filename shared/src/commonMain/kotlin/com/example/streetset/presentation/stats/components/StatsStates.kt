package com.example.streetset.presentation.stats.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun EmptyStats() {
    Text(
        text = "Complete your first workout to see your stats",
        fontSize = 14.sp,
        color = LocalAppColors.current.textSecondary,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
fun StatsSkeleton() {
    val colors = LocalAppColors.current
    val transition = rememberInfiniteTransition(label = "statsSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "statsSkeletonAlpha",
    )
    val bar = colors.textTertiary.copy(alpha = alpha)

    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) { SkeletonBlock(width = 80.dp, height = 36.dp, color = bar, modifier = Modifier.weight(1f)) }
        }
        SkeletonBlock(width = 0.dp, height = 140.dp, color = bar, modifier = Modifier.fillMaxWidth())
        SkeletonBlock(width = 0.dp, height = 90.dp, color = bar, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun SkeletonBlock(width: Dp, height: Dp, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .then(if (width > 0.dp) Modifier.width(width) else Modifier)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(color),
    )
}

@Composable
fun StatsError(message: String, onRetry: () -> Unit) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = colors.error,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Retry",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.accent,
            modifier = Modifier.clickable(onClick = onRetry),
        )
    }
}

@Preview(showBackground = true, heightDp = 480, name = "Stats - Loading")
@Composable
private fun StatsLoadingPreview() {
    StatsPreviewSurface { StatsSkeleton() }
}

@Preview(showBackground = true, name = "Stats - Error")
@Composable
private fun StatsErrorPreview() {
    StatsPreviewSurface { StatsError(message = "Loading stats failed", onRetry = {}) }
}
