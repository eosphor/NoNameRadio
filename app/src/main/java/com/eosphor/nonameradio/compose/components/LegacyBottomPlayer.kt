package com.eosphor.nonameradio.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.eosphor.nonameradio.station.DataRadioStation

@Composable
fun LegacyBottomPlayer(
    currentStation: DataRadioStation?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit = {},
    onExpandClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (currentStation != null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable { onExpandClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Station icon
                if (currentStation.IconUrl.isNullOrEmpty()) {
                    Icon(
                        Icons.Default.Radio,
                        contentDescription = "Station icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentStation.IconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Station icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Station info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentStation.Name ?: "Unknown Station",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "${currentStation.Country ?: "Unknown"} • ${currentStation.Language ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play/Pause button
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Expand button
                IconButton(
                    onClick = onExpandClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = "Expand player",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
