package com.example.streetset.presentation.programdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import com.example.streetset.data.entities.Session
import com.example.streetset.data.entities.Week
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.components.topFade
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.HazeMaterials

@Composable
fun ProgramSessionBottomBar(
    nextSession: Session?,
    nextWeek: Week?,
    completedCount: Int,
    totalCount: Int,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    if (totalCount == 0) return

    val colors = LocalAppColors.current
    val isComplete = nextSession == null
    val shape = RoundedCornerShape(28.dp)
    val isLightBackground = colors.background.luminance() > 0.5f
    val material = if (isLightBackground) {
        HazeMaterials.thin(colors.surface)
    } else {
        HazeMaterials.thin(colors.background)
    }
    val blurStyle = material.then { progressive(HazeProgressive.topFade()) }
    val overlayColor = if (isLightBackground) {
        colors.surface.copy(alpha = 0.72f)
    } else {
        colors.surfaceHigh.copy(alpha = 0.45f)
    }
    val shadowColor = if (isLightBackground) colors.shadow else Color.Black

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, shape, clip = false, ambientColor = shadowColor, spotColor = shadowColor)
            .clip(shape)
            .hazeBlur(input = HazeInput.Backdrop(hazeState), style = blurStyle)
            .background(overlayColor)
            .border(1.dp, colors.border.copy(alpha = 0.5f), shape)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isComplete) {
                    "Program complete"
                } else {
                    "Week ${nextWeek?.number ?: "—"}  |  ${nextSession?.name.orEmpty()}"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$completedCount of $totalCount sessions done",
                fontSize = 13.sp,
                color = colors.textSecondary,
            )
        }

        IconButton(
            onClick = {},
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isComplete) colors.success else colors.accent),
        ) {
            Icon(
                imageVector = if (isComplete) Lucide.Check else Lucide.Play,
                contentDescription = if (isComplete) "Program complete" else "Start next session",
                tint = colors.onAccent,
                modifier = Modifier.size(if (isComplete) 22.dp else 20.dp),
            )
        }
    }
}

@Preview(name = "Next session")
@Composable
private fun ProgramSessionBottomBarPendingPreview() {
    AppTheme(theme = AppTheme.Dark) {
        Box(
            modifier = Modifier
                .background(Color(0xFF1B1B21))
                .padding(16.dp),
        ) {
            ProgramSessionBottomBar(
                nextSession = Session(weekId = 1, weekday = 1, name = "Push", type = "Strength"),
                nextWeek = Week(programId = 1, number = 1),
                completedCount = 0,
                totalCount = 8,
                hazeState = rememberHazeState(),
            )
        }
    }
}

@Preview(name = "Program complete")
@Composable
private fun ProgramSessionBottomBarCompletePreview() {
    AppTheme(theme = AppTheme.Dark) {
        Box(
            modifier = Modifier
                .background(Color(0xFF1B1B21))
                .padding(16.dp),
        ) {
            ProgramSessionBottomBar(
                nextSession = null,
                nextWeek = null,
                completedCount = 8,
                totalCount = 8,
                hazeState = rememberHazeState(),
            )
        }
    }
}

