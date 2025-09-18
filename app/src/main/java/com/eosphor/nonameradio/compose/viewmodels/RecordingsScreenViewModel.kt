package com.eosphor.nonameradio.compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eosphor.nonameradio.recording.DataRecording
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RecordingsScreenUiState(
    val recordings: List<DataRecording> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val selectedRecording: DataRecording? = null,
    val showDeleteDialog: Boolean = false
)

class RecordingsScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecordingsScreenUiState())
    val uiState: StateFlow<RecordingsScreenUiState> = _uiState.asStateFlow()

    private var recordingsManager: com.eosphor.nonameradio.recording.RecordingsManager? = null

    fun initializeRecordingsManager(recordingsManager: com.eosphor.nonameradio.recording.RecordingsManager) {
        this.recordingsManager = recordingsManager
        loadRecordings()
    }

    private fun loadRecordings() {
        recordingsManager?.let { manager ->
            _uiState.value = _uiState.value.copy(
                recordings = manager.getSavedRecordings(),
                isLoading = false
            )
        }
    }

    fun refreshRecordings() {
        _uiState.value = _uiState.value.copy(isRefreshing = true, hasError = false)
        
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Update recordings list from disk
                    recordingsManager?.updateRecordingsList()
                }
                
                recordingsManager?.let { manager ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        recordings = manager.getSavedRecordings()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    hasError = true,
                    errorMessage = e.message ?: "Failed to refresh recordings"
                )
            }
        }
    }

    fun onRecordingClick(recording: DataRecording) {
        // TODO: Implement recording click logic - open recording file
        _uiState.value = _uiState.value.copy(selectedRecording = recording)
    }

    fun onRecordingLongClick(recording: DataRecording) {
        // TODO: Implement recording long click logic - show context menu
        _uiState.value = _uiState.value.copy(
            selectedRecording = recording,
            showDeleteDialog = true
        )
    }

    fun deleteRecording(recording: DataRecording) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // TODO: Implement actual file deletion
                    // For now, just refresh the list
                    recordingsManager?.updateRecordingsList()
                }
                
                recordingsManager?.let { manager ->
                    _uiState.value = _uiState.value.copy(
                        recordings = manager.getSavedRecordings(),
                        showDeleteDialog = false,
                        selectedRecording = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    hasError = true,
                    errorMessage = e.message ?: "Failed to delete recording"
                )
            }
        }
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            selectedRecording = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            hasError = false,
            errorMessage = ""
        )
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    fun formatDate(date: java.util.Date): String {
        val formatter = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}
