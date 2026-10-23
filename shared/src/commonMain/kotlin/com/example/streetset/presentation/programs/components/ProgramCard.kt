package com.example.streetset.presentation.programs.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Lucide
import com.example.streetset.data.entities.Program
import com.example.streetset.data.entities.currentWeek
import com.example.streetset.ui.components.ProgressBar
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt
import kotlin.time.Clock

private enum class ProgramStatus {
    PENDING,
    IN_PROGRESS,
    FINISHED,
}

@Composable
fun ProgramCard(
    program: Program,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(18.dp)
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val status = remember(program, today) { getProgramStatus(program, today) }

    val totalSessions = program.weeksCount ?: 0
    val doneSessions = if (totalSessions == 0) 0 else program.currentWeek(today).coerceIn(0, totalSessions)
    val progress = if (totalSessions == 0) 0f else (doneSessions.toFloat() / totalSessions.toFloat())
    val percentage = (progress * 100f).roundToInt()
    val weeksLabel = if (totalSessions == 1) "1 week" else "$totalSessions weeks"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = Color.Black.copy(alpha = 0.4f), spotColor = Color.Black.copy(alpha = 0.4f))
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.surfaceHigh.copy(alpha = 0.98f),
                        colors.surfaceHigh.copy(alpha = 0.92f),
                    ),
                ),
            )
            .border(1.dp, colors.border.copy(alpha = 0.5f), shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 76.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent.copy(alpha = 0.12f))
                        .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Lucide.Dumbbell,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = program.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                    )
                    program.goal?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Lucide.Calendar,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = program.formatDateRange(),
                    fontSize = 12.sp,
                    color = colors.textTertiary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .width(1.dp)
                        .background(colors.border.copy(alpha = 0.6f)),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Lucide.Clock,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = weeksLabel,
                    fontSize = 12.sp,
                    color = colors.textTertiary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.border.copy(alpha = 0.4f)),
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "$doneSessions/$totalSessions sessions",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$percentage%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                )
            }

            if (totalSessions > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                ProgressBar(progress = progress) // largura total
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        ) {
            StatusPill(status = status)
        }

        Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .size(18.dp),
        )
    }
}

@Composable
private fun StatusPill(status: ProgramStatus) {
    val colors = LocalAppColors.current

    val (label, dotColor, bgColor, borderColor) = when (status) {
        ProgramStatus.PENDING -> Quadruple(
            "Pending",
            Color(0xFFF59E0B),
            Color(0xFFF59E0B).copy(alpha = 0.12f),
            Color(0xFFF59E0B).copy(alpha = 0.35f),
        )
        ProgramStatus.IN_PROGRESS -> Quadruple(
            "In progress",
            colors.accent,
            colors.accent.copy(alpha = 0.12f),
            colors.accent.copy(alpha = 0.35f),
        )
        ProgramStatus.FINISHED -> Quadruple(
            "Finished",
            Color(0xFF22C55E),
            Color(0xFF22C55E).copy(alpha = 0.12f),
            Color(0xFF22C55E).copy(alpha = 0.35f),
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

private fun getProgramStatus(program: Program, today: LocalDate): ProgramStatus {
    val start = program.startDate
    val end = program.endDate
    val total = program.weeksCount ?: 0
    if (total == 0) return ProgramStatus.PENDING
    if (start != null && today < start) return ProgramStatus.PENDING
    if (end != null && today > end) return ProgramStatus.FINISHED
    val current = program.currentWeek(today)
    if (current >= total) return ProgramStatus.FINISHED
    if (current <= 0) return ProgramStatus.PENDING
    return ProgramStatus.IN_PROGRESS
}

@Preview
@Composable
private fun ProgramCardPreview() {
    AppTheme(theme = AppTheme.Dark) {
        Column(
            modifier = Modifier
                .background(Color(0xFF1B1B21))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProgramCard(
                program = Program(
                    id = 1,
                    name = "Strength Base",
                    goal = "Build foundational strength",
                    startDate = LocalDate(2026, 10, 20),
                    endDate = LocalDate(2026, 11, 17),
                    weeksCount = 4,
                ),
                onClick = {},
                onLongPress = {},
            )
        }
    }
}