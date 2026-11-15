package com.example.streetset.presentation.training.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.theme.LocalAppColors

/**
 * Central card of the set screen: reps and load as big numbers with small units.
 */
@Composable
fun SetMetricsCard(
    reps: String,
    loadValue: String,
    loadUnit: String?,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(24.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceHigh.copy(alpha = 0.55f))
            .border(1.dp, colors.border.copy(alpha = 0.5f), shape)
            .padding(vertical = 24.dp),
    ) {
        MetricBlock(
            value = reps,
            unit = null,
            label = "REPS",
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(56.dp)
                .background(colors.border.copy(alpha = 0.6f)),
        )
        MetricBlock(
            value = loadValue,
            unit = loadUnit,
            label = "LOAD",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetricBlock(
    value: String,
    unit: String?,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 56.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-1).sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            unit?.let {
                Text(
                    text = it,
                    fontSize = 18.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
                )
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
            color = colors.textTertiary,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
