package com.example.streetset.presentation.programs.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Dumbbell
import com.example.streetset.data.entities.Program
import com.example.streetset.data.entities.currentWeek
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import com.composables.icons.lucide.Lucide
import com.example.streetset.ui.components.ProgressBar
import kotlinx.datetime.LocalDate

@Composable
fun ProgramCard(
    program: Program,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
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
                    .background(colors.accent.copy(alpha = 0.15f)),
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
                onClick = {}
            )
        }
    }
}
