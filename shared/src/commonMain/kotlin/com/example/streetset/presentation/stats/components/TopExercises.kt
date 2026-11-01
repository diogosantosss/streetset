package com.example.streetset.presentation.stats.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Repeat
import com.example.streetset.data.dao.SessionLogDao
import com.example.streetset.ui.theme.LocalAppColors

@Composable
fun TopExercisesSection(exercises: List<SessionLogDao.ExerciseSets>) {
    val colors = LocalAppColors.current

    Column {
        SectionCaption("TOP EXERCISES")
        Spacer(modifier = Modifier.height(4.dp))
        exercises.forEachIndexed { index, exercise ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 4.dp),
            ) {
                Text(
                    text = exercise.name,
                    fontSize = 16.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconLabel(
                    icon = Lucide.Repeat,
                    text = "${exercise.sets} sets",
                    fontSize = 14.sp,
                )
            }
            if (index < exercises.lastIndex) Hairline()
        }
    }
}