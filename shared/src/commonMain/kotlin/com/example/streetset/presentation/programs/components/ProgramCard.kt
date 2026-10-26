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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.example.streetset.data.entities.Program
import com.example.streetset.data.entities.ProgramStatus
import com.example.streetset.data.entities.status
import com.example.streetset.data.entities.weeksLabel
import com.example.streetset.ui.components.ProgressBar
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

@Composable
fun ProgramCard(
    modifier: Modifier = Modifier,
    program: Program,
    completedSessions: Int = 0,
    totalSessions: Int = 0,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(16.dp)

    val done = completedSessions.coerceIn(0, totalSessions)
    val progress = if (totalSessions == 0) 0f else done.toFloat() / totalSessions.toFloat()
    val percentage = (progress * 100f).roundToInt()

    val status = remember(program, done, totalSessions) { status(done, totalSessions) }
    val statusColor = status.color(colors.accent)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                4.dp,
                shape,
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        statusColor.copy(alpha = 0.10f).compositeOver(colors.surfaceHigh),
                        colors.surfaceHigh,
                    ),
                ),
            )
            .border(1.dp, colors.border.copy(alpha = 0.5f), shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = program.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    program.goal?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Icon(
                            imageVector = Lucide.Calendar,
                            contentDescription = null,
                            tint = colors.textTertiary,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = program.formatDateRange(),
                            fontSize = 11.sp,
                            color = colors.textTertiary,
                            maxLines = 1,
                        )
                        program.weeksLabel()?.let { label ->
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                imageVector = Lucide.Clock,
                                contentDescription = null,
                                tint = colors.textTertiary,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                color = colors.textTertiary,
                                maxLines = 1,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                StatusPill(status = status, color = statusColor)
            }

            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.textTertiary.copy(alpha = 0.15f)),
            )

            Text(
                text = "$done/$totalSessions sessions",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 10.dp),
            )

            if (totalSessions > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    ProgressBar(
                        progress = progress,
                        height = 3.dp,
                        color = statusColor,
                        trackColor = colors.border.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$percentage%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                    )
                }
            }
        }

        Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .size(18.dp),
        )
    }
}

@Composable
private fun StatusPill(status: ProgramStatus, color: Color) {
    val label = when (status) {
        ProgramStatus.PENDING -> "Pending"
        ProgramStatus.IN_PROGRESS -> "In progress"
        ProgramStatus.FINISHED -> "Finished"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            maxLines = 1,
        )
    }
}

private fun ProgramStatus.color(accent: Color): Color = when (this) {
    ProgramStatus.PENDING -> Color(0xFFF59E0B)
    ProgramStatus.IN_PROGRESS -> accent
    ProgramStatus.FINISHED -> Color(0xFF22C55E)
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
                completedSessions = 3,
                totalSessions = 12,
                onClick = {},
                onLongPress = {},
            )
        }
    }
}
