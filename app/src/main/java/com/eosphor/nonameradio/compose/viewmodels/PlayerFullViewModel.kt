package com.eosphor.nonameradio.compose.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eosphor.nonameradio.RadioDroidApp
import com.eosphor.nonameradio.station.DataRadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerFullUiState(
    val currentStation: DataRadioStation? = null,
    val isPlaying: Boolean = false,
    val isRecording: Boolean = false,
    val isFavorite: Boolean = false,
    val timePlayed: String = "00:00",
    val networkUsage: String = "0 KB",
    val timeCached: String = "0 KB",
    val recordingSize: String = "0 MB",
    val recordingName: String = "",
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)

class PlayerFullViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PlayerFullUiState())
    val uiState: StateFlow<PlayerFullUiState> = _uiState.asStateFlow()

    private val radioDroidApp = application as RadioDroidApp
    private val playStationRepository = radioDroidApp.playStationRepository

    init {
        // Initialize with current playing station if any
        loadCurrentStation()
    }

    private fun loadCurrentStation() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // TODO: Get current playing station from PlayerService
                // For now, simulate loading
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e("PlayerFullViewModel", "Error loading current station", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        hasError = true, 
                        errorMessage = e.message
                    ) 
                }
            }
        }
    }

    fun playStation(station: DataRadioStation) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(currentStation = station, isPlaying = true) }
                // TODO: Start playback through PlayerService
            } catch (e: Exception) {
                Log.e("PlayerFullViewModel", "Error playing station", e)
                _uiState.update { 
                    it.copy(
                        hasError = true, 
                        errorMessage = e.message
                    ) 
                }
            }
        }
    }

    fun pausePlayback() {
        _uiState.update { it.copy(isPlaying = false) }
        // TODO: Pause playback through PlayerService
    }

    fun resumePlayback() {
        _uiState.update { it.copy(isPlaying = true) }
        // TODO: Resume playback through PlayerService
    }

    fun togglePlayback() {
        if (_uiState.value.isPlaying) {
            pausePlayback()
        } else {
            resumePlayback()
        }
    }

    fun startRecording() {
        _uiState.update { it.copy(isRecording = true) }
        // TODO: Start recording through PlayerService
    }

    fun stopRecording() {
        _uiState.update { it.copy(isRecording = false) }
        // TODO: Stop recording through PlayerService
    }

    fun toggleRecording() {
        if (_uiState.value.isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    fun toggleFavorite() {
        val currentStation = _uiState.value.currentStation
        if (currentStation != null) {
            val newFavoriteState = !_uiState.value.isFavorite
            _uiState.update { it.copy(isFavorite = newFavoriteState) }
            // TODO: Update favorite status in database
        }
    }

    fun clearError() {
        _uiState.update { it.copy(hasError = false, errorMessage = null) }
    }

    fun refreshStationInfo() {
        loadCurrentStation()
    }
}
