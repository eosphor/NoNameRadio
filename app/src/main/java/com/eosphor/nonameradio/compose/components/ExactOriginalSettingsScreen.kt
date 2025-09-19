package com.eosphor.nonameradio.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class ExactOriginalSettingsItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ExactOriginalSettingsSection(
    title: String,
    items: List<ExactOriginalSettingsItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = item.onClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.title, style = MaterialTheme.typography.bodyLarge)
                    Text(text = item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Divider()
        }
    }
}

@Composable
fun ExactOriginalSettingsScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Appearance (как в оригинале)
        item {
            ExactOriginalSettingsSection(
                title = "Appearance",
                items = listOf(
                    ExactOriginalSettingsItem(
                        title = "Theme Selection",
                        subtitle = "Light",
                        icon = Icons.Default.Palette,
                        onClick = { /* TODO: Open theme selection */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Circular Icons",
                        subtitle = "Disabled",
                        icon = Icons.Default.Circle,
                        onClick = { /* TODO: Toggle circular icons */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Compact Style",
                        subtitle = "Disabled",
                        icon = Icons.Default.Compress,
                        onClick = { /* TODO: Toggle compact style */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Bottom Navigation",
                        subtitle = "Enabled",
                        icon = Icons.Default.Navigation,
                        onClick = { /* TODO: Toggle bottom navigation */ }
                    )
                )
            )
        }

        // Startup Behavior (как в оригинале)
        item {
            ExactOriginalSettingsSection(
                title = "Startup Behavior",
                items = listOf(
                    ExactOriginalSettingsItem(
                        title = "Startup Action",
                        subtitle = "Show History",
                        icon = Icons.Default.PlayArrow,
                        onClick = { /* TODO: Open startup action settings */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Auto-play",
                        subtitle = "Disabled",
                        icon = Icons.Default.PlayCircleOutline,
                        onClick = { /* TODO: Toggle autoplay */ }
                    )
                )
            )
        }

        // Playback (как в оригинале)
        item {
            ExactOriginalSettingsSection(
                title = "Playback",
                items = listOf(
                    ExactOriginalSettingsItem(
                        title = "Audio Quality",
                        subtitle = "High (128 kbps)",
                        icon = Icons.Default.VolumeUp,
                        onClick = { /* TODO: Open quality settings */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Equalizer",
                        subtitle = "Disabled",
                        icon = Icons.Default.GraphicEq,
                        onClick = { /* TODO: Open equalizer */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Sleep Timer",
                        subtitle = "Disabled",
                        icon = Icons.Default.HourglassEmpty,
                        onClick = { /* TODO: Open sleep timer settings */ }
                    )
                )
            )
        }

        // Network (как в оригинале)
        item {
            ExactOriginalSettingsSection(
                title = "Network",
                items = listOf(
                    ExactOriginalSettingsItem(
                        title = "Connection Timeout",
                        subtitle = "30 seconds",
                        icon = Icons.Default.Timer,
                        onClick = { /* TODO: Open timeout settings */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Metered Connection Warning",
                        subtitle = "Enabled",
                        icon = Icons.Default.Warning,
                        onClick = { /* TODO: Toggle metered connection warning */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Proxy Settings",
                        subtitle = "Disabled",
                        icon = Icons.Default.Settings,
                        onClick = { /* TODO: Open proxy settings */ }
                    )
                )
            )
        }

        // MPD (Music Player Daemon) (как в оригинале)
        item {
            ExactOriginalSettingsSection(
                title = "MPD",
                items = listOf(
                    ExactOriginalSettingsItem(
                        title = "MPD Server",
                        subtitle = "Disabled",
                        icon = Icons.Default.SettingsInputComponent,
                        onClick = { /* TODO: Open MPD settings */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "MPD Host",
                        subtitle = "localhost",
                        icon = Icons.Default.Computer,
                        onClick = { /* TODO: Open MPD host settings */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "MPD Port",
                        subtitle = "6600",
                        icon = Icons.Default.Numbers,
                        onClick = { /* TODO: Open MPD port settings */ }
                    )
                )
            )
        }

        // Data Management (как в оригинале)
        item {
            ExactOriginalSettingsSection(
                title = "Data Management",
                items = listOf(
                    ExactOriginalSettingsItem(
                        title = "Clear Cache",
                        subtitle = "Removes temporary data",
                        icon = Icons.Default.Delete,
                        onClick = { /* TODO: Clear cache */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Export Favorites",
                        subtitle = "Save your favorite stations",
                        icon = Icons.Default.Upload,
                        onClick = { /* TODO: Export favorites */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Import Favorites",
                        subtitle = "Load favorite stations from file",
                        icon = Icons.Default.Download,
                        onClick = { /* TODO: Import favorites */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Clear History",
                        subtitle = "Remove all listening history",
                        icon = Icons.Default.History,
                        onClick = { /* TODO: Clear history */ }
                    )
                )
            )
        }

        // About (как в оригинале)
        item {
            ExactOriginalSettingsSection(
                title = "About",
                items = listOf(
                    ExactOriginalSettingsItem(
                        title = "Version",
                        subtitle = "DEV-0.108",
                        icon = Icons.Default.Info,
                        onClick = { /* TODO: Show version info */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Open Source Licenses",
                        subtitle = "View licenses",
                        icon = Icons.Default.Description,
                        onClick = { /* TODO: Show licenses */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Privacy Policy",
                        subtitle = "View privacy policy",
                        icon = Icons.Default.PrivacyTip,
                        onClick = { /* TODO: Open privacy policy */ }
                    )
                )
            )
        }
    }
}
