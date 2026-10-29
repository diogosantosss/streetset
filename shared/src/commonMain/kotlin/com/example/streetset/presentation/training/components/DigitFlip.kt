package com.example.streetset.presentation.training.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun DigitFlip(
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (fadeIn(tween(160)) + slideInVertically(tween(160), initialOffsetY = { -it / 3 }))
                .togetherWith(fadeOut(tween(140)) + slideOutVertically(tween(140), targetOffsetY = { it / 3 }))
        },
        label = "digit_flip",
        modifier = modifier,
    ) { digit ->
        Text(
            text = digit,
            fontSize = 72.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-3).sp,
            color = colors.textPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DigitFlipPreview() {
    DigitFlip(value = "14")
}
