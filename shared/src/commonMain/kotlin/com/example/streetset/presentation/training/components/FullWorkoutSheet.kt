package com.example.streetset.presentation.training.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.ListChecks
import com.composables.icons.lucide.Lucide
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.ui.components.BottomSheet
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeState

/** Fixed height of [FullWorkoutTrigger]. */
private val FullWorkoutTriggerHeight: Dp = 62.dp

/** Card that opens the full workout sheet, sitting above the bottom bar. */
@Composable
fun FullWorkoutTrigger(
    currentStep: Int,
    totalSteps: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(20.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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
            imageVector = Lucide.ChevronUp,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * Bottom sheet listing every step of the workout, reusing [UpcomingRow].
 * Opens already scrolled to the current step.
 */
@Composable
fun FullWorkoutSheet(
    visible: Boolean,
    queue: List<WorkoutStep>,
    currentIndex: Int,
    awaitingFinish: Boolean,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
) {
    val colors = LocalAppColors.current
    val listState = rememberLazyListState()

    LaunchedEffect(visible, currentIndex) {
        if (visible && queue.isNotEmpty()) {
            listState.scrollToItem(currentIndex.coerceIn(0, queue.size - 1))
        }
    }

    BottomSheet(visible = visible, onDismiss = onDismiss, hazeState = hazeState) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 14.dp),
        ) {
            Text(
                text = "Full workout",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (queue.isNotEmpty()) {
                Text(
                    text = "${(currentIndex + 1).coerceIn(1, queue.size)} / ${queue.size}",
                    fontSize = 13.sp,
                    color = colors.textTertiary,
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            itemsIndexed(
                items = queue,
            ) { index, step ->
                val rowState = when {
                    index < currentIndex ||
                        (awaitingFinish && index == currentIndex) -> UpcomingRowState.Done
                    index == currentIndex -> UpcomingRowState.Current
                    else -> UpcomingRowState.Upcoming
                }

                UpcomingRow(
                    step = step,
                    state = rowState,
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(220),
                        fadeOutSpec = tween(180),
                        placementSpec = tween(220),
                    ),
                )
            }
        }
    }
}
