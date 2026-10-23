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
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Lucide
import com.example.streetset.data.entities.Program
import com.example.streetset.data.entities.currentWeek
import com.example.streetset.ui.components.ProgressBar
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import kotlinx.datetime.LocalDate

@Composable
fun ProgramCard(
    program: Program,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(18.dp)

    Column(
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
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.12f))
                    .border(1.dp, colors.accent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.Dumbbell,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = program.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                program.goal?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Lucide.Calendar,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = program.formatDateRange(),
                fontSize = 12.sp,
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.weight(1f))
            program.weeksCount?.let { total ->
                Text(
                    text = "${program.currentWeek()}/$total",
                    fontSize = 12.sp,
                    color = colors.textTertiary
                )
            }
        }

        if (program.weeksCount != null && program.weeksCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            ProgressBar(
                progress = program.currentWeek().toFloat() / program.weeksCount.toFloat()
            )
        }
    }
}

@Preview
@Composable
private fun ProgramCardPreview() {
    AppTheme(theme = AppTheme.Dark) {
        Column(
            modifier = Modifier
                .background(Color(0xFF1B1B21))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
