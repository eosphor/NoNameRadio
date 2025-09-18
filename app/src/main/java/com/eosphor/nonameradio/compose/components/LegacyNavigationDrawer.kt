package com.eosphor.nonameradio.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegacyNavigationDrawer(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        LegacyNavigationItem(
            id = "stations",
            title = "Станции",
            icon = Icons.Default.List,
            description = "Все радиостанции"
        ),
        LegacyNavigationItem(
            id = "starred",
            title = "Избранные",
            icon = Icons.Default.Star,
            description = "Избранные станции"
        ),
        LegacyNavigationItem(
            id = "history",
            title = "История",
            icon = Icons.Default.History,
            description = "История прослушивания"
        ),
        LegacyNavigationItem(
            id = "recordings",
            title = "Записи",
            icon = Icons.Default.Mic,
            description = "Записанные передачи"
        ),
        LegacyNavigationItem(
            id = "settings",
            title = "Настройки",
            icon = Icons.Default.Settings,
            description = "Настройки приложения"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Radio,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "NoNameRadio",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Navigation Items
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(navigationItems) { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(item.title) },
                    selected = selectedItem == item.id,
                    onClick = { onItemSelected(item.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

data class LegacyNavigationItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String
)
