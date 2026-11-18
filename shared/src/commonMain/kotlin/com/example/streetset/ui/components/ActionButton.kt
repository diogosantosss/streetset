package com.example.streetset.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors

/** Fixed height of [ActionButton]. */
val ActionButtonHeight: Dp = 56.dp

/**
 * Filled action button for bottom bars.
 *
 * The fill is a very subtle vertical gradient with a tinted shadow and a hairline
 * inner border, so it reads as a surface instead of a flat rectangle.
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = LocalAppColors.current.accent,
    contentColor: Color = LocalAppColors.current.onAccent,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(24.dp)
    val fill = if (enabled) containerColor else containerColor.copy(alpha = 0.35f)
    val content = if (enabled) contentColor else contentColor.copy(alpha = 0.6f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(ActionButtonHeight)
            .shadow(
                elevation = if (enabled) 10.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = fill.copy(alpha = 0.20f),
                spotColor = fill.copy(alpha = 0.30f),
            )
            .clip(shape)
            .background(Brush.verticalGradient(listOf(fill, lerp(fill, Color.Black, 0.12f))))
            .clickable(enabled = enabled, onClick = onClick)
            .border(1.dp, content.copy(alpha = 0.18f), shape)
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(18.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "ActionButton")
@Composable
private fun ActionButtonPreview() {
    AppTheme(theme = AppTheme.Graphite) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(LocalAppColors.current.background)
                .padding(16.dp),
        ) {
            ActionButton(text = "Complete set", icon = Lucide.ChevronRight, onClick = {})
            ActionButton(text = "Skip rest", onClick = {})
        }
    }
}

@Preview(showBackground = true, name = "ActionButton - States")
@Composable
private fun ActionButtonStatesPreview() {
    AppTheme(theme = AppTheme.Graphite) {
        val colors = LocalAppColors.current

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(LocalAppColors.current.background)
                .padding(16.dp),
        ) {
            ActionButton(
                text = "Finish",
                icon = Lucide.ChevronRight,
                onClick = {},
                containerColor = colors.success,
            )
            ActionButton(
                text = "Skip rest",
                onClick = {},
                containerColor = colors.surfaceHigh,
                contentColor = colors.textPrimary,
            )
            ActionButton(text = "Complete set", onClick = {}, enabled = false)
        }
    }
}

