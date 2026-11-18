package com.example.streetset.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.components.TopBar
import com.example.streetset.ui.components.TopBarDefaults
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.backgroundBrush
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
) {
    val colors = LocalAppColors.current
    val selectedTheme by settingsViewModel.theme.collectAsState()
    val appInfo = settingsViewModel.appInfo
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBrush),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = TopBarDefaults.Height + statusBarPadding + 8.dp,
                bottom = 128.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionTitle(text = "Appearance")
            }

            item {
                ThemeSelector(
                    selected = selectedTheme,
                    onSelect = settingsViewModel::selectTheme,
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                SectionTitle(text = "About")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    AboutRow(label = "Name", value = appInfo.name)
                    AboutDivider()
                    AboutRow(label = "Version", value = appInfo.versionName)
                    AboutDivider()
                    AboutRow(label = "Build", value = appInfo.buildNumber)
                    AboutDivider()
                    AboutRow(label = "Last update", value = appInfo.lastUpdated)
                }
            }
        }

        TopBar(
            title = "Settings",
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    val colors = LocalAppColors.current

    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = colors.textTertiary,
        textAlign = TextAlign.Start,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun ThemeSelector(
    selected: AppTheme,
    onSelect: (AppTheme) -> Unit,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(16.dp)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppTheme.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { theme ->
                    val isSelected = theme == selected

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(colors.surface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) colors.accent else colors.border.copy(alpha = 0.6f),
                                shape = shape,
                            )
                            .clickable { onSelect(theme) }
                            .padding(12.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(theme.colors.background, theme.colors.surfaceHigh, theme.colors.accent)
                                .forEach { swatch ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(swatch)
                                            .border(
                                                1.dp,
                                                colors.border.copy(alpha = 0.4f),
                                                RoundedCornerShape(5.dp),
                                            ),
                                    )
                                }
                        }
                        Text(
                            text = theme.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) colors.textPrimary else colors.textSecondary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutRow(
    label: String,
    value: String,
) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = colors.textSecondary,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun AboutDivider() {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.border.copy(alpha = 0.4f)),
    )
}
