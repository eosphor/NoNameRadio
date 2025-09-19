package com.eosphor.nonameradio.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class ExactOriginalNavigationItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExactOriginalNavigationDrawer(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        ExactOriginalNavigationItem(
            id = "stations",
            title = "Stations",
            icon = Icons.Default.List,
            description = "All radio stations"
        ),
        ExactOriginalNavigationItem(
            id = "starred",
            title = "Starred",
            icon = Icons.Default.Star,
            description = "Favorite stations"
        ),
        ExactOriginalNavigationItem(
            id = "history",
            title = "History",
            icon = Icons.Default.History,
            description = "Listening history"
        ),
        ExactOriginalNavigationItem(
            id = "alarm",
            title = "Alarm",
            icon = Icons.Default.Alarm,
            description = "Alarms"
        )
    )

    val settingsItem = ExactOriginalNavigationItem(
        id = "settings",
        title = "Settings",
        icon = Icons.Default.Settings,
        description = "App settings"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Spacer(Modifier.height(16.dp))
        
        // Main navigation items (group 1)
        navigationItems.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.description) },
                label = { Text(item.title) },
                selected = item.id == selectedItem,
                onClick = { onItemSelected(item.id) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Settings item (group 2 - separated)
        NavigationDrawerItem(
            icon = { Icon(settingsItem.icon, contentDescription = settingsItem.description) },
            label = { Text(settingsItem.title) },
            selected = settingsItem.id == selectedItem,
            onClick = { onItemSelected(settingsItem.id) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
