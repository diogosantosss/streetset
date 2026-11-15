package com.example.streetset.presentation.training.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors

/**
 * Compact "Up next" card previewing the following step, so the user can get
 * ready without opening the full workout.
 *
 * @param step the next step, or `null` when the current one is the last.
 */
@Composable
fun NextUpCard(
    step: WorkoutStep?,
    modifier: Modifier = Modifier,
    label: String = "UP NEXT",
) {
    val colors = LocalAppColors.current
    val accent = step?.let { TrainingProtocolColors.colorFor(it.protocol.name, colors.accent) }
        ?: colors.accent
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceHigh.copy(alpha = 0.45f))
            .border(1.dp, colors.border.copy(alpha = 0.5f), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp,
                color = colors.textTertiary,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (step != null) {
                Text(
                    text = step.protocolDisplay.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = accent,
                )
            }
        }

        if (step == null) {
            Text(
                text = "Last set — finish strong",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    text = if (step.protocol.isGroup) step.protocolDisplay else step.exerciseName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (step.protocol.isGroup) {
                        "Round ${step.setIndex} / ${step.totalSetsInBlock}"
                    } else {
                        step.repsLoadDisplay
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
