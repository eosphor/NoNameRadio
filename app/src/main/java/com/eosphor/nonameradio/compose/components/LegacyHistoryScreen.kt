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
fun LegacyHistoryScreen(
    stations: List<DataRadioStation>,
    onStationClick: (DataRadioStation) -> Unit = {},
    onStationFavoriteClick: (DataRadioStation) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (stations.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "История пуста",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Станции появятся здесь после прослушивания",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(stations) { station ->
                RadioStationItem(
                    station = station,
                    onClick = { onStationClick(station) },
                    onFavoriteClick = { onStationFavoriteClick(station) },
                    onPlayClick = { onStationClick(station) },
                    isFavorite = false,
                    isPlaying = false
                )
            }
        }
    }
}
