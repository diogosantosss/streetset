package com.example.streetset.presentation.training.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.ListChecks
import com.composables.icons.lucide.Lucide
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.ui.theme.LocalAppColors

/** Fixed height of the "Full workout" card. */
private val FullWorkoutTriggerHeight: Dp = 62.dp

/** Duration of the expand/collapse animation. Also used to time the reveal scroll. */
internal const val FullWorkoutListAnimationMillis = 200

/**
 * The whole workout queue under the current phase, expanded in place.
 *
 * Tapping the card reveals every step right below it, inside the same scroll
 * as the phase, so nothing covers the session. The card stays as a header while
 * the list is open and a "Hide" row closes it from the bottom.
 */
@Composable
fun FullWorkoutSection(
    queue: List<WorkoutStep>,
    currentIndex: Int,
    awaitingFinish: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FullWorkoutTrigger(
            currentStep = currentIndex + 1,
            totalSteps = queue.size,
            expanded = expanded,
            onClick = onToggle,
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(FullWorkoutListAnimationMillis)) +
                fadeIn(tween(FullWorkoutListAnimationMillis, delayMillis = 60)),
            exit = shrinkVertically(tween(FullWorkoutListAnimationMillis)) +
                fadeOut(tween(FullWorkoutListAnimationMillis / 2)),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            ) {
                queue.forEachIndexed { index, step ->
                    UpcomingRow(
                        step = step,
                        state = when {
                            index < currentIndex ||
                                (awaitingFinish && index == currentIndex) -> UpcomingRowState.Done
                            index == currentIndex -> UpcomingRowState.Current
                            else -> UpcomingRowState.Upcoming
                        },
                    )
                }

                HideWorkoutRow(onClick = onToggle)
            }
        }
    }
}

/** Card that expands and collapses the list. */
@Composable
private fun FullWorkoutTrigger(
    currentStep: Int,
    totalSteps: Int,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(20.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(FullWorkoutTriggerHeight)
            .clip(shape)
            .background(colors.surfaceHigh.copy(alpha = 0.45f))
            .border(1.dp, colors.border.copy(alpha = 0.5f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.accent.copy(alpha = 0.16f)),
        ) {
            Icon(
                imageVector = Lucide.ListChecks,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Full workout",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
            )
            Text(
                text = "$currentStep of $totalSteps steps",
                fontSize = 12.sp,
                color = colors.textTertiary,
                maxLines = 1,
                modifier = Modifier.padding(top = 1.dp),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = if (expanded) Lucide.ChevronDown else Lucide.ChevronUp,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** Small row at the end of the list so a long workout can be collapsed from there too. */
@Composable
private fun HideWorkoutRow(onClick: () -> Unit) {
    val colors = LocalAppColors.current

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Icon(
            imageVector = Lucide.ChevronUp,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Hide workout",
            fontSize = 13.sp,
            color = colors.textTertiary,
        )
    }
}
