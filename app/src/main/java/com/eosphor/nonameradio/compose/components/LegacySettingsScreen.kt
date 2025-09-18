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
fun LegacySettingsScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            SettingsSection(
                title = "Воспроизведение",
                items = listOf(
                    SettingsItem(
                        title = "Качество звука",
                        subtitle = "Высокое (128 kbps)",
                        icon = Icons.Default.VolumeUp,
                        onClick = { /* TODO: Open quality settings */ }
                    ),
                    SettingsItem(
                        title = "Автовоспроизведение",
                        subtitle = "Включено",
                        icon = Icons.Default.PlayArrow,
                        onClick = { /* TODO: Toggle autoplay */ }
                    ),
                    SettingsItem(
                        title = "Эквалайзер",
                        subtitle = "Отключен",
                        icon = Icons.Default.GraphicEq,
                        onClick = { /* TODO: Open equalizer */ }
                    )
                )
            )
        }

        item {
            SettingsSection(
                title = "Интерфейс",
                items = listOf(
                    SettingsItem(
                        title = "Тема",
                        subtitle = "Системная",
                        icon = Icons.Default.Palette,
                        onClick = { /* TODO: Open theme settings */ }
                    ),
                    SettingsItem(
                        title = "Язык",
                        subtitle = "Русский",
                        icon = Icons.Default.Language,
                        onClick = { /* TODO: Open language settings */ }
                    ),
                    SettingsItem(
                        title = "Размер шрифта",
                        subtitle = "Средний",
                        icon = Icons.Default.TextFields,
                        onClick = { /* TODO: Open font size settings */ }
                    )
                )
            )
        }

        item {
            SettingsSection(
                title = "Данные",
                items = listOf(
                    SettingsItem(
                        title = "Кэш",
                        subtitle = "Очистить кэш",
                        icon = Icons.Default.Storage,
                        onClick = { /* TODO: Clear cache */ }
                    ),
                    SettingsItem(
                        title = "Сохранение",
                        subtitle = "Экспорт настроек",
                        icon = Icons.Default.Save,
                        onClick = { /* TODO: Export settings */ }
                    )
                )
            )
        }

        item {
            SettingsSection(
                title = "О приложении",
                items = listOf(
                    SettingsItem(
                        title = "Версия",
                        subtitle = "1.0.0",
                        icon = Icons.Default.Info,
                        onClick = { /* TODO: Show version info */ }
                    ),
                    SettingsItem(
                        title = "Лицензия",
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
fun SettingsSection(
    title: String,
    items: List<SettingsItem>,
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
            SettingsItemRow(
                item = item,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SettingsItemRow(
    item: SettingsItem,
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

data class SettingsItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)
