package com.example.streetset.presentation.settings

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streetset.ui.components.TopBar
import com.example.streetset.ui.components.TopBarDefaults
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.TrainingProtocolColors
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import streetset.shared.generated.resources.Res
import streetset.shared.generated.resources.app_icon

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
) {
    val colors = LocalAppColors.current
    val selectedTheme by settingsViewModel.theme.collectAsState()
    val keepScreenOn by settingsViewModel.keepScreenOn.collectAsState()
    val appInfo = settingsViewModel.appInfo
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
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
                SectionTitle(text = "Display")
            }

            item {
                KeepScreenOnRow(
                    enabled = keepScreenOn,
                    onChange = settingsViewModel::setKeepScreenOn,
                )
            }

            item {
                SectionTitle(text = "About")
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.app_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        Text(
                            text = "Street",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                        )
                        Text(
                            text = "Set",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrainingProtocolColors.circuit,
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.7f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = "Version ${appInfo.versionName}",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                        )
                    }
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
private fun KeepScreenOnRow(
    enabled: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(18.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.6f), shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Keep screen on",
                fontSize = 15.sp,
                color = colors.textPrimary,
            )
            Text(
                text = "Prevent the screen from sleeping while the app is open",
                fontSize = 12.sp,
                color = colors.textTertiary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.accent,
                checkedTrackColor = colors.accent.copy(alpha = 0.5f),
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.textTertiary.copy(alpha = 0.3f),
            ),
        )
    }
}

@Preview(showBackground = true, name = "Settings - Keep screen on")
@Composable
private fun KeepScreenOnRowPreview() {
    AppTheme(theme = AppTheme.Graphite) {
        Column(
            modifier = Modifier
                .background(LocalAppColors.current.background)
                .padding(16.dp),
        ) {
            KeepScreenOnRow(enabled = true, onChange = {})
        }
    }
}
