package com.example.streetset.presentation.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Lucide
import com.example.streetset.data.dao.SessionLogDao
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.util.fullDisplay
import kotlinx.datetime.LocalDateTime

@Composable
fun HistorySection(
    history: List<SessionLogDao.HistoryEntry>,
    onLongClick: (SessionLogDao.HistoryEntry) -> Unit,
) {
    val colors = LocalAppColors.current

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SectionCaption("HISTORY", modifier = Modifier.weight(1f))
            Text(
                text = "${history.size} sessions",
                fontSize = 11.sp,
                color = colors.textTertiary,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        history.forEach { entry ->
            HistoryCard(entry = entry, onLongClick = onLongClick)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun HistoryCard(
    entry: SessionLogDao.HistoryEntry,
    onLongClick: (SessionLogDao.HistoryEntry) -> Unit,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.6f), shape)
            .pointerInput(entry.logId) {
                detectTapGestures(onLongPress = { onLongClick(entry) })
            }
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceHigh),
            ) {
                Icon(
                    imageVector = Lucide.Dumbbell,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.sessionName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatHistoryDate(entry.completedAt),
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.surfaceHigh)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = formatDuration(entry.durationSeconds.toLong()),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                )
            }
        }

        entry.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.border.copy(alpha = 0.6f)),
            )
            Text(
                text = notes,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

private fun formatHistoryDate(dateTime: LocalDateTime): String {
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.date.fullDisplay()} · $hour:$minute"
}
