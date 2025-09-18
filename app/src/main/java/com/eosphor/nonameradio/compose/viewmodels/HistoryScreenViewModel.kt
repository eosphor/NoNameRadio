package com.eosphor.nonameradio.compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eosphor.nonameradio.station.DataRadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HistoryScreenUiState(
    val stations: List<DataRadioStation> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val deletedStation: DataRadioStation? = null,
    val showUndoSnackbar: Boolean = false
)

class HistoryScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryScreenUiState())
    val uiState: StateFlow<HistoryScreenUiState> = _uiState.asStateFlow()

    private var historyManager: com.eosphor.nonameradio.HistoryManager? = null

    fun initializeHistoryManager(historyManager: com.eosphor.nonameradio.HistoryManager) {
        this.historyManager = historyManager
        loadHistory()
    }

    private fun loadHistory() {
        historyManager?.let { manager ->
            _uiState.value = _uiState.value.copy(
                stations = manager.getList(),
                isLoading = false
            )
        }
    }

    fun refreshHistory() {
        _uiState.value = _uiState.value.copy(isRefreshing = true, hasError = false)
        
        viewModelScope.launch {
            try {
                // TODO: Implement refresh logic similar to RefreshDownloadList()
                // For now, just reload from history manager
                withContext(Dispatchers.IO) {
                    // Simulate network call
                    kotlinx.coroutines.delay(1000)
                }
                
                historyManager?.let { manager ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        stations = manager.getList()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    hasError = true,
                    errorMessage = e.message ?: "Failed to refresh history"
                )
            }
        }
    }

    fun onStationClick(station: DataRadioStation) {
        // TODO: Implement station click logic
        // This should trigger play selection dialog
    }

    fun onStationSwipe(station: DataRadioStation) {
        historyManager?.let { manager ->
            val removedIdx = manager.remove(station.StationUuid)
            if (removedIdx != -1) {
                _uiState.value = _uiState.value.copy(
                    stations = manager.getList(),
                    deletedStation = station,
                    showUndoSnackbar = true
                )
            }
        }
    }

    fun undoStationDeletion() {
        _uiState.value.deletedStation?.let { station ->
            historyManager?.let { manager ->
                val restoredIdx = _uiState.value.stations.size // Approximate position
                manager.restore(station, restoredIdx)
                _uiState.value = _uiState.value.copy(
                    stations = manager.getList(),
                    showUndoSnackbar = false,
                    deletedStation = null
                )
            }
        }
    }

    fun dismissUndoSnackbar() {
        _uiState.value = _uiState.value.copy(
            showUndoSnackbar = false,
            deletedStation = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            hasError = false,
            errorMessage = ""
        )
    }
}
