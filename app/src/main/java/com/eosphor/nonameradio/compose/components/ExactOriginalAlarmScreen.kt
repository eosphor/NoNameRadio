package com.eosphor.nonameradio.compose.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eosphor.nonameradio.R
import com.eosphor.nonameradio.alarm.DataRadioStationAlarm
import com.eosphor.nonameradio.compose.viewmodels.AlarmScreenViewModel
import java.util.Locale

@Composable
fun ExactOriginalAlarmScreen(
    modifier: Modifier = Modifier,
    viewModel: AlarmScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_alarm),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (uiState.alarms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.alarm_no_items),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggleEnabled = { enabled -> viewModel.toggleEnabled(alarm.id, enabled) },
                        onDelete = { viewModel.deleteAlarm(alarm.id) },
                        onChangeTime = { hour, minute -> viewModel.updateTime(alarm.id, hour, minute) },
                        onToggleRepeating = { viewModel.toggleRepeating(alarm.id) },
                        onToggleWeekDay = { day -> viewModel.toggleWeekDay(alarm.id, day) }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun AlarmCard(
    alarm: DataRadioStationAlarm,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onChangeTime: (Int, Int) -> Unit,
    onToggleRepeating: () -> Unit,
    onToggleWeekDay: (Int) -> Unit
) {
    val context = LocalContext.current
    val weekDays = stringArrayResource(id = R.array.weekdays)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alarm.station?.Name ?: context.getString(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            alarm.hour,
                            alarm.minute
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = onToggleEnabled
                )
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onChangeTime(hour, minute) },
                        alarm.hour,
                        alarm.minute,
                        true
                    ).show()
                }) {
                    Text(text = stringResource(R.string.alarm_change_time))
                }
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = onToggleRepeating,
                    label = {
                        Text(
                            text = if (alarm.repeating) {
                                stringResource(R.string.image_button_dont_repeat)
                            } else {
                                stringResource(R.string.image_button_repeat)
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Repeat, contentDescription = null)
                    }
                )
            }

            if (alarm.repeating) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weekDays.forEachIndexed { index, label ->
                        val selected = alarm.weekDays?.contains(index) == true
                        FilterChip(
                            selected = selected,
                            onClick = { onToggleWeekDay(index) },
                            label = { Text(text = label) }
                        )
                    }
                }
            }
        }
    }
}
