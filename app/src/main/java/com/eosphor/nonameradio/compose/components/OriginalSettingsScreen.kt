package com.eosphor.nonameradio.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OriginalSettingsScreen(
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
            OriginalSettingsSection(
                title = "Playback",
                items = listOf(
                    OriginalSettingsItem(
                        title = "Audio Quality",
                        subtitle = "High (128 kbps)",
                        icon = Icons.Default.VolumeUp,
                        onClick = { /* TODO: Open quality settings */ }
                    ),
                    OriginalSettingsItem(
                        title = "Auto-play",
                        subtitle = "Enabled",
                        icon = Icons.Default.PlayArrow,
                        onClick = { /* TODO: Toggle autoplay */ }
                    ),
                    OriginalSettingsItem(
                        title = "Equalizer",
                        subtitle = "Disabled",
                        icon = Icons.Default.GraphicEq,
                        onClick = { /* TODO: Open equalizer */ }
                    )
                )
            )
        }

        item {
            OriginalSettingsSection(
                title = "Interface",
                items = listOf(
                    OriginalSettingsItem(
                        title = "Theme",
                        subtitle = "System",
                        icon = Icons.Default.Palette,
                        onClick = { /* TODO: Open theme settings */ }
                    ),
                    OriginalSettingsItem(
                        title = "Language",
                        subtitle = "English",
                        icon = Icons.Default.Language,
                        onClick = { /* TODO: Open language settings */ }
                    ),
                    OriginalSettingsItem(
                        title = "Font Size",
                        subtitle = "Medium",
                        icon = Icons.Default.TextFields,
                        onClick = { /* TODO: Open font size settings */ }
                    )
                )
            )
        }

        item {
            OriginalSettingsSection(
                title = "Data",
                items = listOf(
                    OriginalSettingsItem(
                        title = "Cache",
                        subtitle = "Clear cache",
                        icon = Icons.Default.Storage,
                        onClick = { /* TODO: Clear cache */ }
                    ),
                    OriginalSettingsItem(
                        title = "Backup",
                        subtitle = "Export settings",
                        icon = Icons.Default.Save,
                        onClick = { /* TODO: Export settings */ }
                    )
                )
            )
        }

        item {
            OriginalSettingsSection(
                title = "About",
                items = listOf(
                    OriginalSettingsItem(
                        title = "Version",
                        subtitle = "1.0.0",
                        icon = Icons.Default.Info,
                        onClick = { /* TODO: Show version info */ }
                    ),
                    OriginalSettingsItem(
                        title = "License",
                        subtitle = "GPL v3",
                        icon = Icons.Default.Description,
                        onClick = { /* TODO: Show license */ }
                    )
                )
            )
        }
    }
}

@Composable
fun OriginalSettingsSection(
    title: String,
    items: List<OriginalSettingsItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        items.forEach { item ->
            OriginalSettingsItemRow(
                item = item,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun OriginalSettingsItemRow(
    item: OriginalSettingsItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall
                )
                
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

data class OriginalSettingsItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)