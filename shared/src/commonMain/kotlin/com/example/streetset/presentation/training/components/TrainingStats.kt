package com.example.streetset.presentation.training.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Gauge
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Timer
import com.example.streetset.presentation.training.model.formatRestSecondsShort
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun RestMetaRow(
    restSeconds: Int,
    rpe: Int?,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        if (restSeconds > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Lucide.Timer,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = formatRestSecondsShort(restSeconds),
                    fontSize = 13.sp,
                    color = colors.textTertiary,
                )
            }
        }
        rpe?.let { value ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Lucide.Gauge,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "RPE $value",
                    fontSize = 13.sp,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

@Composable
fun StatCard(
    value: String,
    unit: String?,
    label: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = LocalAppColors.current
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = if (compact) 36.sp else 52.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = if (compact) TextUnit.Unspecified else (-1).sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            unit?.let {
                Text(
                    text = it,
                    fontSize = if (compact) 16.sp else 18.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(
                        start = 4.dp,
                        bottom = if (compact) 6.dp else 9.dp,
                    ),
                )
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
            color = colors.textTertiary,
        )
    }
}

@Composable
internal fun MetaIconText(
    icon: ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 17.sp,
    iconSize: Dp = 14.dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LocalAppColors.current.textTertiary,
            modifier = Modifier.size(iconSize),
        )
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            color = color,
            maxLines = 1,
        )
    }
}


