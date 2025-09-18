package com.eosphor.nonameradio.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eosphor.nonameradio.station.DataRadioStation

@Composable
fun LegacyStationsScreen(
    stations: List<DataRadioStation>,
    isLoading: Boolean = false,
    onStationClick: (DataRadioStation) -> Unit = {},
    onStationFavoriteClick: (DataRadioStation) -> Unit = {},
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (stations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Нет станций",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Потяните вниз для обновления",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(stations) { station ->
                    RadioStationItem(
                        station = station,
                        onClick = { onStationClick(station) },
                        onFavoriteClick = { onStationFavoriteClick(station) },
                        onPlayClick = { onStationClick(station) },
                        isFavorite = false, // TODO: Get from favorite manager
                        isPlaying = false // TODO: Get from player state
                    )
                }
            }
        }
    }
}
