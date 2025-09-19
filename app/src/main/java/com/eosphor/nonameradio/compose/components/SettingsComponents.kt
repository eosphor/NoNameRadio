package com.eosphor.nonameradio.compose.components

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eosphor.nonameradio.R
import com.eosphor.nonameradio.RadioDroidApp
import com.eosphor.nonameradio.compose.viewmodels.SettingsUiState
import com.eosphor.nonameradio.compose.viewmodels.SettingsViewModel

// Appearance Settings
fun LazyListScope.appearanceSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    item {
        SettingsSectionTitle("Theme")
    }
    
    item {
        ThemeSelector(
            currentTheme = uiState.themeName,
            onThemeSelected = viewModel::updateTheme
        )
    }

    item {
        SettingsSwitch(
            title = "Circular Icons",
            subtitle = "Use circular icons for stations",
            checked = uiState.circularIcons,
            onCheckedChange = viewModel::updateCircularIcons
        )
    }

    item {
        SettingsSwitch(
            title = "Compact Style",
            subtitle = if (uiState.compactStyle) "Compact view enabled" else "Compact view disabled",
            checked = uiState.compactStyle,
            onCheckedChange = viewModel::updateCompactStyle
        )
    }

    item {
        SettingsSwitch(
            title = "Bottom Navigation",
            subtitle = "Show bottom navigation bar",
            checked = uiState.bottomNavigation,
            onCheckedChange = viewModel::updateBottomNavigation
        )
    }
}

// Startup Settings
fun LazyListScope.startupSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    item {
        SettingsSectionTitle("Startup Action")
    }
    
    item {
        StartupActionSelector(
            currentAction = uiState.startupAction,
            onActionSelected = viewModel::updateStartupAction
        )
    }

    item {
        SettingsSwitch(
            title = "Auto Play on Startup",
            subtitle = if (uiState.autoPlayOnStartup) "Will auto play on startup" else "Manual play required",
            checked = uiState.autoPlayOnStartup,
            onCheckedChange = viewModel::updateAutoPlayOnStartup
        )
    }

    item {
        SettingsSwitch(
            title = "Auto Off on Startup",
            subtitle = if (uiState.autoOffOnStartup) "Will auto stop after timeout" else "Manual stop required",
            checked = uiState.autoOffOnStartup,
            enabled = uiState.autoPlayOnStartup,
            onCheckedChange = viewModel::updateAutoOffOnStartup
        )
    }

    if (uiState.autoOffOnStartup && uiState.autoPlayOnStartup) {
        item {
            TimeoutSelector(
                title = "Auto Off Timeout",
                currentTimeout = uiState.autoOffTimeout,
                onTimeoutSelected = viewModel::updateAutoOffTimeout
            )
        }
    }
}

// Interaction Settings
fun LazyListScope.interactionSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    item {
        SettingsSwitch(
            title = "Load Icons",
            subtitle = if (uiState.loadIcons) "Station icons will be loaded" else "No icons will be loaded",
            checked = uiState.loadIcons,
            onCheckedChange = viewModel::updateLoadIcons
        )
    }

    item {
        SettingsSwitch(
            title = "Icon Click Toggles Favorite",
            subtitle = if (uiState.iconClickTogglesFavorite) "Click icon to toggle favorite" else "Click icon does nothing",
            checked = uiState.iconClickTogglesFavorite,
            enabled = uiState.loadIcons,
            onCheckedChange = viewModel::updateIconClickTogglesFavorite
        )
    }

    item {
        SettingsSwitch(
            title = "Auto Favorite",
            subtitle = if (uiState.autoFavorite) "Automatically add to favorites" else "Manual favorite required",
            checked = uiState.autoFavorite,
            onCheckedChange = viewModel::updateAutoFavorite
        )
    }

    item {
        SettingsSwitch(
            title = "Show Broken Stations",
            subtitle = if (uiState.showBroken) "Show broken stations" else "Hide broken stations",
            checked = uiState.showBroken,
            onCheckedChange = viewModel::updateShowBroken
        )
    }

    item {
        SettingsSwitch(
            title = "Click Trend Icon Visible",
            subtitle = if (uiState.clickTrendIconVisible) "Show click trend icons" else "Hide click trend icons",
            checked = uiState.clickTrendIconVisible,
            onCheckedChange = viewModel::updateClickTrendIconVisible
        )
    }

    item {
        SettingsSwitch(
            title = "Single Use Tags",
            subtitle = if (uiState.singleUseTags) "Use single tags only" else "Use multiple tags",
            checked = uiState.singleUseTags,
            onCheckedChange = viewModel::updateSingleUseTags
        )
    }
}

// Player Settings
fun LazyListScope.playerSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    item {
        SettingsSwitch(
            title = "Play in External Player",
            subtitle = if (uiState.playExternal) "Use external player" else "Use internal player",
            checked = uiState.playExternal,
            onCheckedChange = viewModel::updatePlayExternal
        )
    }

    item {
        SettingsSwitch(
            title = "Warn on No WiFi",
            subtitle = if (uiState.warnNoWifi) "Warn when not on WiFi" else "No WiFi warning",
            checked = uiState.warnNoWifi,
            onCheckedChange = viewModel::updateWarnNoWifi
        )
    }

    item {
        SettingsSwitch(
            title = "Pause When Noisy",
            subtitle = "Pause playback when device is noisy",
            checked = uiState.pauseWhenNoisy,
            onCheckedChange = viewModel::updatePauseWhenNoisy
        )
    }

    item {
        SettingsSwitch(
            title = "Auto Resume on Wired Headset",
            subtitle = if (uiState.autoResumeOnWiredHeadset) "Auto resume when headset connected" else "Manual resume required",
            checked = uiState.autoResumeOnWiredHeadset,
            onCheckedChange = viewModel::updateAutoResumeOnWiredHeadset
        )
    }

    item {
        SettingsSwitch(
            title = "Auto Resume on Bluetooth A2DP",
            subtitle = if (uiState.autoResumeOnBluetoothA2dp) "Auto resume when Bluetooth connected" else "Manual resume required",
            checked = uiState.autoResumeOnBluetoothA2dp,
            onCheckedChange = viewModel::updateAutoResumeOnBluetoothA2dp
        )
    }

    item {
        EqualizerSettings()
    }
}

// Alarm Settings
fun LazyListScope.alarmSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    item {
        SettingsSwitch(
            title = "External Alarm",
            subtitle = if (uiState.alarmExternal) "Use external alarm app" else "Use internal alarm",
            checked = uiState.alarmExternal,
            onCheckedChange = viewModel::updateAlarmExternal
        )
    }

    if (uiState.alarmExternal) {
        item {
            TimeoutSelector(
                title = "Alarm Sleep Timer",
                currentTimeout = uiState.alarmTimeout,
                onTimeoutSelected = viewModel::updateAlarmTimeout
            )
        }
    }
}

// Recordings Settings
fun LazyListScope.recordingsSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    item {
        RecordNameFormattingSelector(
            currentFormat = uiState.recordNameFormatting,
            onFormatSelected = viewModel::updateRecordNameFormatting
        )
    }
}

// Connectivity Settings
fun LazyListScope.connectivitySettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    item {
        SettingsSectionTitle("Timeouts")
    }

    item {
        TimeoutInput(
            title = "Connect Timeout",
            subtitle = "Connection timeout in seconds",
            value = uiState.streamConnectTimeout,
            onValueChange = viewModel::updateStreamConnectTimeout
        )
    }

    item {
        TimeoutInput(
            title = "Read Timeout",
            subtitle = "Read timeout in seconds",
            value = uiState.streamReadTimeout,
            onValueChange = viewModel::updateStreamReadTimeout
        )
    }

    item {
        TimeoutInput(
            title = "Retry Timeout",
            subtitle = "Retry timeout in seconds",
            value = uiState.retryTimeout,
            onValueChange = viewModel::updateRetryTimeout
        )
    }

    item {
        TimeoutInput(
            title = "Retry Delay",
            subtitle = "Delay between retries in milliseconds",
            value = uiState.retryDelay,
            onValueChange = viewModel::updateRetryDelay
        )
    }

    item {
        TimeoutInput(
            title = "Resume Within",
            subtitle = "Resume within seconds",
            value = uiState.resumeWithin,
            onValueChange = viewModel::updateResumeWithin
        )
    }

    item {
        ProxySettings()
    }
}

// MPD Settings
fun LazyListScope.mpdSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    item {
        SettingsButton(
            title = "View MPD Servers",
            subtitle = "Manage MPD server connections",
            onClick = {
                // TODO: Navigate to MPD servers screen
                // This will be handled by the main navigation
            }
        )
    }
}

// Other Settings
fun LazyListScope.otherSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    item {
        SettingsSectionTitle("API Keys")
    }

    item {
        SettingsTextInput(
            title = "Last.fm API Key",
            subtitle = "Enter your Last.fm API key",
            value = uiState.lastFmApiKey,
            onValueChange = viewModel::updateLastFmApiKey
        )
    }

    item {
        SettingsSectionTitle("System")
    }

    item {
        BatteryOptimizationSettings(
            enabled = uiState.ignoreBatteryOptimization,
            onToggle = viewModel::updateIgnoreBatteryOptimization
        )
    }

    item {
        StatisticsSettings()
    }

    item {
        AboutSettings()
    }
}

// Common Components
@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun SettingsButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
    }
}

@Composable
private fun ThemeSelector(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    val themes = listOf("light", "dark", "auto")
    val themeNames = listOf("Light", "Dark", "Auto")
    
    SettingsButton(
        title = "Theme",
        subtitle = themeNames[themes.indexOf(currentTheme)],
        onClick = { showDialog = true }
    )
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    themes.forEachIndexed { index, theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeSelected(theme)
                                    showDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == theme,
                                onClick = {
                                    onThemeSelected(theme)
                                    showDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = themeNames[index])
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StartupActionSelector(
    currentAction: String,
    onActionSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    val actions = listOf("show_history", "show_favorites", "show_stations")
    val actionNames = listOf("Show History", "Show Favorites", "Show Stations")
    
    SettingsButton(
        title = "Startup Action",
        subtitle = actionNames[actions.indexOf(currentAction)],
        onClick = { showDialog = true }
    )
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Startup Action") },
            text = {
                Column {
                    actions.forEachIndexed { index, action ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onActionSelected(action)
                                    showDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentAction == action,
                                onClick = {
                                    onActionSelected(action)
                                    showDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = actionNames[index])
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TimeoutSelector(
    title: String,
    currentTimeout: String,
    onTimeoutSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    val timeouts = listOf("5", "10", "15", "30", "60", "120", "240")
    
    SettingsButton(
        title = title,
        subtitle = "$currentTimeout minutes",
        onClick = { showDialog = true }
    )
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Timeout") },
            text = {
                Column {
                    timeouts.forEach { timeout ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTimeoutSelected(timeout)
                                    showDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTimeout == timeout,
                                onClick = {
                                    onTimeoutSelected(timeout)
                                    showDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "$timeout minutes")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RecordNameFormattingSelector(
    currentFormat: String,
    onFormatSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    val formats = listOf("default", "station_name", "station_name_date", "station_name_time")
    val formatNames = listOf("Default", "Station Name", "Station Name + Date", "Station Name + Time")
    
    SettingsButton(
        title = "Record Name Formatting",
        subtitle = formatNames[formats.indexOf(currentFormat)],
        onClick = { showDialog = true }
    )
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Format") },
            text = {
                Column {
                    formats.forEachIndexed { index, format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onFormatSelected(format)
                                    showDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentFormat == format,
                                onClick = {
                                    onFormatSelected(format)
                                    showDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = formatNames[index])
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TimeoutInput(
    title: String,
    subtitle: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputValue by remember(value) { mutableStateOf(value.toString()) }
    
    SettingsButton(
        title = title,
        subtitle = subtitle,
        onClick = { showDialog = true }
    )
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text("Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        inputValue.toIntOrNull()?.let { onValueChange(it) }
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsTextInput(
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputValue by remember(value) { mutableStateOf(value) }
    
    SettingsButton(
        title = title,
        subtitle = if (value.isEmpty()) "Not set" else "Set",
        onClick = { showDialog = true }
    )
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(inputValue)
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EqualizerSettings() {
    val context = LocalContext.current
    
    SettingsButton(
        title = "Equalizer",
        subtitle = "Audio equalizer settings",
        onClick = {
            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            if (context.packageManager.resolveActivity(intent, 0) == null) {
                // Show error toast
            } else {
                intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                context.startActivity(intent)
            }
        }
    )
}

@Composable
private fun ProxySettings() {
    SettingsButton(
        title = "Proxy Settings",
        subtitle = "Configure proxy connection",
        onClick = {
            // TODO: Open proxy settings dialog
        }
    )
}

@Composable
private fun BatteryOptimizationSettings(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    
    SettingsSwitch(
        title = "Ignore Battery Optimization",
        subtitle = if (enabled) "Battery optimization disabled" else "Battery optimization enabled",
        checked = enabled,
        onCheckedChange = { checked ->
            onToggle(checked)
            if (checked) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        }
    )
}

@Composable
private fun StatisticsSettings() {
    SettingsButton(
        title = "Statistics",
        subtitle = "View app statistics",
        onClick = {
            // TODO: Open statistics dialog
        }
    )
}

@Composable
private fun AboutSettings() {
    SettingsButton(
        title = "About",
        subtitle = "App information and version",
        onClick = {
            // TODO: Open about dialog
        }
    )
}
