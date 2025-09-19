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
                        title = "Auto-play",
                        subtitle = "Enabled",
                        icon = Icons.Default.PlayArrow,
                        onClick = { /* TODO: Toggle autoplay */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Equalizer",
                        subtitle = "Disabled",
                        icon = Icons.Default.GraphicEq,
                        onClick = { /* TODO: Open equalizer */ }
                    )
                )
            )
        }

        item {
            ExactOriginalSettingsSection(
                title = "Interface",
                items = listOf(
                    ExactOriginalSettingsItem(
                        title = "Theme",
                        subtitle = "System default",
                        icon = Icons.Default.Palette,
                        onClick = { /* TODO: Open theme settings */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Language",
                        subtitle = "English",
                        icon = Icons.Default.Language,
                        onClick = { /* TODO: Open language settings */ }
                    ),
                    ExactOriginalSettingsItem(
                        title = "Show Icons in List",
                        subtitle = "Enabled",
                        icon = Icons.Default.Image,
                        onClick = { /* TODO: Toggle show icons */ }
                    )
                )
            )
        }

        item {
            ExactOriginalSettingsSection(
                title = "Data",
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
                    )
                )
            )
        }
    }
}
