package com.eosphor.nonameradio.compose.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eosphor.nonameradio.RadioDroidApp
import com.eosphor.nonameradio.alarm.DataRadioStationAlarm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Observer

data class AlarmUiState(
    val alarms: List<DataRadioStationAlarm> = emptyList()
)

class AlarmScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val app: RadioDroidApp = application as RadioDroidApp
    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private val alarmsObserver = Observer { _, _ -> refreshAlarms() }

    init {
        app.alarmManager.savedAlarmsObservable.addObserver(alarmsObserver)
        refreshAlarms()
    }

    private fun refreshAlarms() {
        _uiState.update { state ->
            state.copy(alarms = app.alarmManager.list.toList())
        }
    }

    fun toggleEnabled(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            app.alarmManager.setEnabled(id, enabled)
            refreshAlarms()
        }
    }

    fun toggleRepeating(id: Int) {
        viewModelScope.launch {
            app.alarmManager.toggleRepeating(id)
            refreshAlarms()
        }
    }

    fun toggleWeekDay(id: Int, dayIndex: Int) {
        viewModelScope.launch {
            app.alarmManager.changeWeekDays(id, dayIndex)
            refreshAlarms()
        }
    }

    fun deleteAlarm(id: Int) {
        viewModelScope.launch {
            app.alarmManager.remove(id)
            refreshAlarms()
        }
    }

    fun updateTime(id: Int, hour: Int, minute: Int) {
        viewModelScope.launch {
            app.alarmManager.changeTime(id, hour, minute)
            refreshAlarms()
        }
    }

    override fun onCleared() {
        app.alarmManager.savedAlarmsObservable.deleteObserver(alarmsObserver)
        super.onCleared()
    }
}
