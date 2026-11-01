package com.example.streetset.presentation.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.presentation.sessiondetail.label
import com.example.streetset.presentation.stats.MixSlice
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors
import kotlin.math.roundToInt

@Composable
fun MixSection(mix: List<MixSlice>) {
    val colors = LocalAppColors.current

    Column {
        SectionCaption("TRAINING MIX")
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            mix.forEach { slice ->
                Box(
                    modifier = Modifier
                        .weight(slice.fraction.coerceAtLeast(0.02f))
                        .fillMaxHeight()
                        .background(TrainingProtocolColors.colorFor(slice.protocol.name, colors.accent)),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        mix.forEachIndexed { index, slice ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .padding(horizontal = 4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(TrainingProtocolColors.colorFor(slice.protocol.name, colors.accent)),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = slice.protocol.label,
                    fontSize = 15.sp,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${(slice.fraction * 100).roundToInt()}%",
                    fontSize = 15.sp,
                    color = colors.textSecondary,
                )
            }
            if (index < mix.lastIndex) Hairline()
        }
    }
}