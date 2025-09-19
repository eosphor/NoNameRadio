package com.eosphor.nonameradio.compose.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eosphor.nonameradio.RadioDroidApp
import com.eosphor.nonameradio.station.DataRadioStation
import kotlinx.coroutines.launch
import java.io.InputStreamReader

@Composable
fun SaveLoadFavoritesDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit = {},
    onLoad: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export/Import Favorites") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FavoritesActionItem(
                        title = "Save Favorites",
                        subtitle = "Export favorites to M3U playlist file",
                        icon = Icons.Default.Save,
                        onClick = {
                            onSave()
                            onDismiss()
                        }
                    )
                }
                
                item {
                    FavoritesActionItem(
                        title = "Load Favorites",
                        subtitle = "Import favorites from M3U playlist file",
                        icon = Icons.Default.Upload,
                        onClick = {
                            onLoad()
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun FavoritesActionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SaveFavoritesDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit = { _, _ -> }
) {
    var fileName by remember { mutableStateOf("favorites.m3u") }
    var directory by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Favorites") },
        text = {
            Column {
                Text(
                    text = "Save your favorites to an M3U playlist file",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = directory,
                    onValueChange = { directory = it },
                    label = { Text("Directory (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (fileName.isNotBlank()) {
                        onSave(directory, fileName)
                        onDismiss()
                    }
                },
                enabled = fileName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LoadFavoritesDialog(
    onDismiss: () -> Unit,
    onLoad: (Uri) -> Unit = { _ -> }
) {
    val context = LocalContext.current
    var selectedFile by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedFile = it
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Load Favorites") },
        text = {
            Column {
                Text(
                    text = "Load favorites from an M3U playlist file",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedFile != null) {
                    Text(
                        text = "Selected file: ${selectedFile!!.lastPathSegment}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "No file selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        filePickerLauncher.launch(arrayOf("audio/x-mpegurl", "application/vnd.apple.mpegurl"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select M3U File")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedFile?.let { uri ->
                        onLoad(uri)
                        onDismiss()
                    }
                },
                enabled = selectedFile != null
            ) {
                Text("Load")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FavoritesPreviewDialog(
    stations: List<DataRadioStation>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Preview Favorites") },
        text = {
            Column {
                Text(
                    text = "Found ${stations.size} stations to import:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(stations.take(10)) { station ->
                        Text(
                            text = station.Name,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    if (stations.size > 10) {
                        item {
                            Text(
                                text = "... and ${stations.size - 10} more",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Функции для работы с файлами
fun saveFavoritesToFile(
    context: android.content.Context,
    directory: String,
    fileName: String,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    try {
        val radioDroidApp = context.applicationContext as RadioDroidApp
        val favouriteManager = radioDroidApp.getFavouriteManager()
        
        if (directory.isBlank()) {
            // Используем стандартную директорию
            favouriteManager.SaveM3U(null, fileName)
        } else {
            favouriteManager.SaveM3U(directory, fileName)
        }
        
        onSuccess()
    } catch (e: Exception) {
        onError(e.message ?: "Unknown error")
    }
}

fun loadFavoritesFromFile(
    context: android.content.Context,
    uri: Uri,
    onSuccess: (List<DataRadioStation>) -> Unit = { _ -> },
    onError: (String) -> Unit = {}
) {
    try {
        val radioDroidApp = context.applicationContext as RadioDroidApp
        val favouriteManager = radioDroidApp.getFavouriteManager()
        
        val inputStream = context.contentResolver.openInputStream(uri)
        val reader = InputStreamReader(inputStream)
        
        favouriteManager.LoadM3USimple(reader)
        onSuccess(emptyList()) // TODO: Return actual stations list
    } catch (e: Exception) {
        onError(e.message ?: "Unknown error")
    }
}
