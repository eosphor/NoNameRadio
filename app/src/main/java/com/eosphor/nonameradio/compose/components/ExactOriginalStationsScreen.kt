package com.eosphor.nonameradio.compose.components

import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.eosphor.nonameradio.R
import com.eosphor.nonameradio.RadioDroidApp
import com.eosphor.nonameradio.Utils
import com.eosphor.nonameradio.compose.util.findFragmentActivity
import com.eosphor.nonameradio.players.PlayStationTask
import com.eosphor.nonameradio.players.selector.PlayerType
import com.eosphor.nonameradio.station.DataRadioStation
import com.eosphor.nonameradio.station.StationActions

@Composable
fun ExactOriginalStationsScreen(
    stations: List<DataRadioStation>,
    isLoading: Boolean = false,
    onStationClick: (DataRadioStation) -> Unit = {},
    onStationFavoriteClick: (DataRadioStation) -> Unit = {},
    onRefresh: () -> Unit = {},
    favoriteStationIds: Set<String> = emptySet(),
    onFavoritesChanged: () -> Unit = {},
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
                        text = "No stations found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pull down to refresh",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(stations) { station ->
                    ExactOriginalStationItem(
                        station = station,
                        onClick = { onStationClick(station) },
                        onFavoriteClick = { onStationFavoriteClick(station) },
                        onPlayClick = { onStationClick(station) },
                        isFavorite = favoriteStationIds.contains(station.StationUuid),
                        isPlaying = false, // TODO: Get from player state
                        onFavoritesChanged = onFavoritesChanged
                    )
                }
            }
        }
    }
}

@Composable
fun ExactOriginalStationItem(
    station: DataRadioStation,
    onClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    isFavorite: Boolean = false,
    isPlaying: Boolean = false,
    onFavoritesChanged: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val anchorView = LocalView.current
    val activity = context.findFragmentActivity()
    var menuExpanded by remember { mutableStateOf(false) }
    val sharedPrefs = remember { PreferenceManager.getDefaultSharedPreferences(context.applicationContext) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station icon (точно как в оригинале - 70dp)
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (station.IconUrl.isNullOrEmpty()) {
                    Icon(
                        Icons.Default.Radio,
                        contentDescription = "Station icon",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(station.IconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Station icon",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop,
                        onSuccess = {
                            android.util.Log.d("ExactOriginalStationItem", "Image loaded successfully: ${station.IconUrl}")
                        },
                        onError = { error ->
                            android.util.Log.e("ExactOriginalStationItem", "Image load error: ${station.IconUrl}, error: ${error.result.throwable}")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Station information (точно как в оригинале)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Station name
                Text(
                    text = station.Name ?: "Unknown Station",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Country and language (точно как в оригинале)
                Text(
                    text = "${station.Country ?: "Unknown"} • ${station.Language ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tags (точно как в оригинале)
                if (!station.TagsAll.isNullOrEmpty()) {
                    Text(
                        text = station.TagsAll ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // More button (точно как в оригинале)
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                if (sharedPrefs.getBoolean("play_external", false)) {
                    DropdownMenuItem(
                        text = { Text(text = context.getString(R.string.context_menu_play_in_radiodroid)) },
                        onClick = {
                            StationActions.playInRadioDroid(context, station)
                            menuExpanded = false
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(text = context.getString(R.string.context_menu_play_in_external_player)) },
                        onClick = {
                            val app = context.applicationContext as RadioDroidApp
                            Utils.playAndWarnIfMetered(app, station, PlayerType.EXTERNAL) {
                                PlayStationTask.playExternal(station, context).execute()
                            }
                            menuExpanded = false
                        }
                    )
                }

                if (activity != null) {
                    DropdownMenuItem(
                        text = { Text(text = context.getString(R.string.context_menu_visit_homepage)) },
                        onClick = {
                            StationActions.openStationHomeUrl(activity, station)
                            menuExpanded = false
                        }
                    )
                }

                DropdownMenuItem(
                    text = { Text(text = context.getString(R.string.context_menu_share)) },
                    onClick = {
                        if (activity != null) {
                            StationActions.share(activity, station)
                        }
                        menuExpanded = false
                    }
                )

                if (activity != null) {
                    DropdownMenuItem(
                        text = { Text(text = context.getString(R.string.context_menu_add_alarm)) },
                        onClick = {
                            StationActions.setAsAlarm(activity, station)
                            menuExpanded = false
                        }
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    val shortcutManager: ShortcutManager? = context.getSystemService(ShortcutManager::class.java)
                    if (shortcutManager?.isRequestPinShortcutSupported == true) {
                        DropdownMenuItem(
                            text = { Text(text = context.getString(R.string.context_menu_create_shortcut)) },
                            onClick = {
                                station.prepareShortcut(context, object : DataRadioStation.ShortcutReadyListener {
                                    override fun onShortcutReadyListener(shortcut: ShortcutInfo) {
                                        shortcutManager.requestPinShortcut(shortcut, null)
                                    }
                                })
                                menuExpanded = false
                            }
                        )
                    }
                }

                DropdownMenuItem(
                    text = { Text(text = context.getString(R.string.context_menu_copy_stream_url)) },
                    onClick = {
                        StationActions.copyStreamUrlToClipboard(context, station)
                        menuExpanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text(text = context.getString(R.string.context_menu_vote)) },
                    onClick = {
                        StationActions.vote(context, station)
                        menuExpanded = false
                    }
                )

                if (isFavorite) {
                    DropdownMenuItem(
                        text = { Text(text = context.getString(R.string.context_menu_delete)) },
                        onClick = {
                            StationActions.removeFromFavourites(context, anchorView, station)
                            onFavoritesChanged()
                            menuExpanded = false
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(text = context.getString(R.string.action_favorite)) },
                        onClick = {
                            StationActions.markAsFavourite(context, station)
                            onFavoritesChanged()
                            menuExpanded = false
                        }
                    )
                }
            }
        }
    }
}
