@file:OptIn(ExperimentalMaterial3Api::class)

package com.eosphor.nonameradio.compose.components

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eosphor.nonameradio.R
import com.eosphor.nonameradio.RadioDroidApp
import com.eosphor.nonameradio.alarm.DataRadioStationAlarm
import com.eosphor.nonameradio.alarm.RadioAlarmManager
import com.eosphor.nonameradio.compose.viewmodels.AlarmScreenViewModel
import com.eosphor.nonameradio.station.DataRadioStation
import java.util.*

@Composable
fun ExactOriginalAlarmScreen(
    modifier: Modifier = Modifier,
    viewModel: AlarmScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddAlarmDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var selectedStation by remember { mutableStateOf<DataRadioStation?>(null) }

    // ViewModel уже инициализирован в конструкторе

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Заголовок с кнопкой добавления
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.nav_item_alarm),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                FloatingActionButton(
                    onClick = { showAddAlarmDialog = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add alarm"
                    )
                }
            }

            if (uiState.alarms.isEmpty()) {
                // Пустое состояние
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "No alarms set",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Tap the + button to add an alarm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                // Список будильников
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.alarms) { alarm ->
                        AlarmItem(
                            alarm = alarm,
                            onToggle = { enabled -> viewModel.toggleEnabled(alarm.id, enabled) },
                            onEdit = { 
                                selectedStation = alarm.station
                                showTimePickerDialog = true
                            },
                            onDelete = { viewModel.deleteAlarm(alarm.id) }
                        )
                    }
                }
            }
        }

        // Диалог выбора станции для будильника
        if (showAddAlarmDialog) {
            AddAlarmDialog(
                onDismiss = { showAddAlarmDialog = false },
                onStationSelected = { station ->
                    selectedStation = station
                    showAddAlarmDialog = false
                    showTimePickerDialog = true
                }
            )
        }

        // TimePicker диалог
        if (showTimePickerDialog && selectedStation != null) {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    // Добавляем будильник через AlarmManager напрямую
                    val app = context.applicationContext as RadioDroidApp
                    app.getAlarmManager().add(selectedStation!!, hour, minute)
                    showTimePickerDialog = false
                    selectedStation = null
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).apply {
                setTitle(selectedStation?.Name ?: context.getString(R.string.settings_alarm))
                show()
            }
        }
    }
}

@Composable
private fun AlarmItem(
    alarm: DataRadioStationAlarm,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.enabled) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Переключатель включения/выключения
            Switch(
                checked = alarm.enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.padding(end = 16.dp)
            )

            // Информация о будильнике
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Время
                Text(
                    text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.enabled) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Название станции
                Text(
                    text = alarm.station.Name ?: "Unknown Station",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (alarm.enabled) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Дни повторения
                if (alarm.repeating && alarm.weekDays.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getWeekDaysText(alarm.weekDays),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (alarm.enabled) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            // Кнопка меню
            IconButton(
                onClick = { showMenu = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        onEdit()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDelete()
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onStationSelected: (DataRadioStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var stations by remember { mutableStateOf<List<DataRadioStation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Загрузка станций из истории и избранного
    LaunchedEffect(Unit) {
        val app = context.applicationContext as RadioDroidApp
        val historyManager = app.getHistoryManager()
        val favoriteManager = app.getFavouriteManager()
        
        val historyStations = historyManager.getList()
        val favoriteStations = favoriteManager.getList()
        
        // Объединяем и убираем дубликаты
        val allStations = (historyStations + favoriteStations)
            .distinctBy { it.StationUuid }
            .take(50) // Ограничиваем для производительности
        
        stations = allStations
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Station for Alarm") },
        text = {
            Column {
                // Поле поиска
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search stations") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Список станций
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val filteredStations = stations.filter { station ->
                        station.Name?.contains(searchQuery, ignoreCase = true) == true ||
                        station.Country?.contains(searchQuery, ignoreCase = true) == true ||
                        station.TagsAll?.contains(searchQuery, ignoreCase = true) == true
                    }

                    items(filteredStations) { station ->
                        StationSelectionItem(
                            station = station,
                            onClick = { onStationSelected(station) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StationSelectionItem(
    station: DataRadioStation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = station.Name ?: "Unknown Station",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!station.Country.isNullOrEmpty()) {
                    Text(
                        text = station.Country ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun getWeekDaysText(weekDays: List<Int>): String {
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    return weekDays.map { dayNames[it] }.joinToString(", ")
}