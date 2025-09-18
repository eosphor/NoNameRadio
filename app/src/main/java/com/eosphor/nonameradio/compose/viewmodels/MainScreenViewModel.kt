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
    
    private var historyManager: com.eosphor.nonameradio.HistoryManager? = null
    
    init {
        loadInitialData()
    }
    
    fun initializeHistoryManager(historyManager: com.eosphor.nonameradio.HistoryManager) {
        this.historyManager = historyManager
        android.util.Log.d("MainScreenViewModel", "HistoryManager initialized")
        // Загружаем историю из HistoryManager
        loadHistoryFromManager()
    }
    
    private fun loadHistoryFromManager() {
        historyManager?.let { manager ->
            val historyStations = manager.getList()
            _uiState.value = _uiState.value.copy(historyStations = historyStations)
        }
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Симуляция загрузки данных
                delay(1000)
                
                // Пример данных станций
                val sampleStations = listOf(
                    createSampleStation("BBC Radio 1", "United Kingdom", "English", "pop,rock,chart", 128, "AAC", "https://picsum.photos/120/120?random=1"),
                    createSampleStation("Radio France Inter", "France", "French", "eclectic,world,jazz", 128, "MP3", "https://picsum.photos/120/120?random=2"),
                    createSampleStation("Deutschlandfunk", "Germany", "German", "news,talk,culture", 128, "MP3", "https://picsum.photos/120/120?random=3"),
                    createSampleStation("Radio Canada", "Canada", "French", "news,talk,music", 128, "MP3", "https://picsum.photos/120/120?random=4"),
                    createSampleStation("ABC Classic FM", "Australia", "English", "classical,music,culture", 128, "MP3", "https://picsum.photos/120/120?random=5"),
                    createSampleStation("Radio Italia", "Italy", "Italian", "pop,music,italian", 128, "MP3", "https://picsum.photos/120/120?random=6"),
                    createSampleStation("RNE Radio Nacional", "Spain", "Spanish", "news,talk,culture", 128, "MP3", "https://picsum.photos/120/120?random=7"),
                    createSampleStation("NOS Radio 1", "Netherlands", "Dutch", "news,talk,music", 128, "MP3", "https://picsum.photos/120/120?random=8")
                )
                
                android.util.Log.d("MainScreenViewModel", "Loaded ${sampleStations.size} stations")
                sampleStations.forEach { station ->
                    android.util.Log.d("MainScreenViewModel", "Station: ${station.Name}, IconUrl: ${station.IconUrl}")
                }
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
        codec: String,
        iconUrl: String? = null
    ): DataRadioStation {
        return DataRadioStation().apply {
            Name = name
            Country = country
            Language = language
            TagsAll = tags
            Bitrate = bitrate
            Codec = codec
            StationUuid = java.util.UUID.randomUUID().toString()
            IconUrl = iconUrl
        }
    }
    
    fun playStation(station: DataRadioStation) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentStation = station,
                isPlaying = true
            )
            
            // Добавляем в историю через HistoryManager
            historyManager?.let { manager ->
                android.util.Log.d("MainScreenViewModel", "Adding station to history: ${station.Name}")
                manager.add(station)
                android.util.Log.d("MainScreenViewModel", "History now has ${manager.getList().size} stations")
            } ?: run {
                android.util.Log.w("MainScreenViewModel", "HistoryManager is null, cannot add station to history")
            }
            
            // Обновляем локальный список истории
            loadHistoryFromManager()
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
                // Показать историю - навигация будет обработана в MainActivity
            }
            "recordings" -> {
                // Показать записи - навигация будет обработана в MainActivity
            }
            "categories/tags" -> {
                // Показать теги - навигация будет обработана в MainActivity
            }
            "categories/languages" -> {
                // Показать языки - навигация будет обработана в MainActivity
            }
            "categories/countries" -> {
                // Показать страны - навигация будет обработана в MainActivity
            }
            "search" -> {
                // Показать поиск
            }
            "settings" -> {
                // Показать настройки
            }
            "about" -> {
                // Показать информацию о программе
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