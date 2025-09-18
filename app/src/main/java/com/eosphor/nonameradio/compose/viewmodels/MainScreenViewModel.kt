package com.eosphor.nonameradio.compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.eosphor.nonameradio.station.DataRadioStation

data class MainScreenUiState(
    val allStations: List<DataRadioStation> = emptyList(),
    val favoriteStations: List<DataRadioStation> = emptyList(),
    val historyStations: List<DataRadioStation> = emptyList(),
    val searchResults: List<DataRadioStation> = emptyList(),
    val currentStation: DataRadioStation? = null,
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val volume: Float = 0.7f,
    val error: String? = null
)

class MainScreenViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Симуляция загрузки данных
                delay(1000)
                
                // Пример данных станций
                val sampleStations = listOf(
                    createSampleStation("Radio Example 1", "Germany", "German", "pop,rock,music", 128, "MP3"),
                    createSampleStation("Radio Example 2", "France", "French", "jazz,classical", 192, "AAC"),
                    createSampleStation("BBC Radio 1", "United Kingdom", "English", "pop,rock,chart", 128, "AAC"),
                    createSampleStation("Radio Paradise", "USA", "English", "eclectic,rock,alternative", 320, "MP3"),
                    createSampleStation("Radio Swiss Jazz", "Switzerland", "German", "jazz,smooth", 128, "AAC"),
                    createSampleStation("FIP", "France", "French", "eclectic,world,jazz", 128, "MP3"),
                    createSampleStation("KEXP", "USA", "English", "alternative,indie,rock", 128, "MP3"),
                    createSampleStation("Radio Caroline", "United Kingdom", "English", "rock,classic", 128, "MP3")
                )
                
                _uiState.value = _uiState.value.copy(
                    allStations = sampleStations,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun createSampleStation(
        name: String,
        country: String,
        language: String,
        tags: String,
        bitrate: Int,
        codec: String
    ): DataRadioStation {
        return DataRadioStation().apply {
            Name = name
            Country = country
            Language = language
            TagsAll = tags
            Bitrate = bitrate
            Codec = codec
            StationUuid = java.util.UUID.randomUUID().toString()
            IconUrl = null
        }
    }
    
    fun playStation(station: DataRadioStation) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentStation = station,
                isPlaying = true
            )
            
            // Добавляем в историю
            val currentHistory = _uiState.value.historyStations.toMutableList()
            currentHistory.removeAll { it.StationUuid == station.StationUuid }
            currentHistory.add(0, station)
            if (currentHistory.size > 50) {
                currentHistory.removeAt(currentHistory.size - 1)
            }
            
            _uiState.value = _uiState.value.copy(
                historyStations = currentHistory
            )
        }
    }
    
    fun pausePlayback() {
        _uiState.value = _uiState.value.copy(isPlaying = false)
    }
    
    fun resumePlayback() {
        _uiState.value = _uiState.value.copy(isPlaying = true)
    }
    
    fun toggleFavorite(station: DataRadioStation) {
        viewModelScope.launch {
            val currentFavorites = _uiState.value.favoriteStations.toMutableList()
            val existingIndex = currentFavorites.indexOfFirst { it.StationUuid == station.StationUuid }
            
            if (existingIndex >= 0) {
                currentFavorites.removeAt(existingIndex)
            } else {
                currentFavorites.add(station)
            }
            
            _uiState.value = _uiState.value.copy(
                favoriteStations = currentFavorites
            )
        }
    }
    
    fun selectStation(station: DataRadioStation) {
        // Логика выбора станции (например, показать детали)
        playStation(station)
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        if (query.isNotEmpty()) {
            performSearch(query)
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        }
    }
    
    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            
            try {
                // Симуляция поиска
                delay(500)
                
                val results = _uiState.value.allStations.filter { station ->
                    station.Name?.contains(query, ignoreCase = true) == true ||
                    station.Country?.contains(query, ignoreCase = true) == true ||
                    station.TagsAll?.contains(query, ignoreCase = true) == true
                }
                
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isSearching = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = e.message
                )
            }
        }
    }
    
    fun setVolume(volume: Float) {
        _uiState.value = _uiState.value.copy(volume = volume)
    }
    
    fun expandPlayer() {
        // Логика расширения плеера
    }
    
    fun navigateToSection(section: String) {
        // Логика навигации по разделам
        when (section) {
            "all_stations" -> {
                // Обновить список всех станций
            }
            "favorites" -> {
                // Показать избранные
            }
            "history" -> {
                // Показать историю
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearCurrentStation() {
        _uiState.value = _uiState.value.copy(
            currentStation = null,
            isPlaying = false
        )
    }
    
    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            pausePlayback()
        } else {
            resumePlayback()
        }
    }
}