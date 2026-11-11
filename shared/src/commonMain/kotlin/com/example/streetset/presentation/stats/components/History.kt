package com.example.streetset.presentation.stats.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Clock
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
        SectionCaption("HISTORY")
        Spacer(modifier = Modifier.height(4.dp))
        history.forEachIndexed { index, entry ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .pointerInput(entry.logId) {
                        detectTapGestures(onLongPress = { onLongClick(entry) })
                    }
                    .padding(vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.sessionName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconLabel(
                        icon = Lucide.Clock,
                        text = formatDuration(entry.durationSeconds.toLong()),
                    )
                }
                Text(
                    text = formatHistoryDate(entry.completedAt),
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 3.dp),
                )
                entry.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Text(
                        text = notes,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            if (index < history.lastIndex) Hairline()
        }
    }
}

private fun formatHistoryDate(dateTime: LocalDateTime): String {
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.date.fullDisplay()} · $hour:$minute"
}
