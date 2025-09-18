package com.eosphor.nonameradio.compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eosphor.nonameradio.station.DataRadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class LegacyMainScreenUiState(
    val isLoading: Boolean = false,
    val currentStation: DataRadioStation? = null,
    val isPlaying: Boolean = false,
    val localStations: List<DataRadioStation> = emptyList(),
    val topClickStations: List<DataRadioStation> = emptyList(),
    val topVoteStations: List<DataRadioStation> = emptyList(),
    val recentStations: List<DataRadioStation> = emptyList(),
    val currentlyHeardStations: List<DataRadioStation> = emptyList(),
    val favoriteStations: List<DataRadioStation> = emptyList(),
    val historyStations: List<DataRadioStation> = emptyList(),
    val searchResults: List<DataRadioStation> = emptyList(),
    val tagCategories: List<String> = emptyList(),
    val countryCategories: List<String> = emptyList(),
    val languageCategories: List<String> = emptyList(),
    val recordings: List<String> = emptyList(),
    val error: String? = null
)

class LegacyMainScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LegacyMainScreenUiState())
    val uiState: StateFlow<LegacyMainScreenUiState> = _uiState.asStateFlow()

    private var historyManager: com.eosphor.nonameradio.HistoryManager? = null

    init {
        loadInitialData()
    }

    fun initializeHistoryManager(historyManager: com.eosphor.nonameradio.HistoryManager) {
        this.historyManager = historyManager
        android.util.Log.d("LegacyMainScreenViewModel", "HistoryManager initialized")
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
                delay(1000) // Симуляция загрузки

                // Загружаем все категории станций
                val localStations = createMockStations("local", 15)
                val topClickStations = createMockStations("topclick", 15)
                val topVoteStations = createMockStations("topvote", 15)
                val recentStations = createMockStations("recent", 15)
                val currentlyHeardStations = createMockStations("currently", 15)
                val favoriteStations = createMockStations("favorite", 10)

                // Загружаем категории
                val tagCategories = listOf("pop", "rock", "jazz", "classical", "news", "talk", "electronic", "folk")
                val countryCategories = listOf("United States", "United Kingdom", "Germany", "France", "Italy", "Spain", "Russia", "Canada")
                val languageCategories = listOf("English", "Spanish", "French", "German", "Italian", "Russian", "Portuguese", "Dutch")

                // Загружаем записи
                val recordings = listOf("Recording_2024-01-15.mp3", "Recording_2024-01-14.mp3", "Recording_2024-01-13.mp3")

                _uiState.value = _uiState.value.copy(
                    localStations = localStations,
                    topClickStations = topClickStations,
                    topVoteStations = topVoteStations,
                    recentStations = recentStations,
                    currentlyHeardStations = currentlyHeardStations,
                    favoriteStations = favoriteStations,
                    tagCategories = tagCategories,
                    countryCategories = countryCategories,
                    languageCategories = languageCategories,
                    recordings = recordings,
                    isLoading = false
                )

                android.util.Log.d("LegacyMainScreenViewModel", "Loaded all data successfully")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun createMockStations(prefix: String, count: Int): List<DataRadioStation> {
        return (1..count).map { index ->
            DataRadioStation().apply {
                StationUuid = "$prefix-station-$index"
                Name = when (prefix) {
                    "local" -> "Local Station $index"
                    "topclick" -> "Top Click Station $index"
                    "topvote" -> "Top Vote Station $index"
                    "recent" -> "Recent Station $index"
                    "currently" -> "Currently Heard Station $index"
                    "favorite" -> "Favorite Station $index"
                    else -> "Station $index"
                }
                Country = listOf("US", "UK", "DE", "FR", "IT", "ES", "RU", "CA").random()
                Language = listOf("English", "Spanish", "French", "German", "Italian", "Russian").random()
                TagsAll = listOf("pop", "rock", "jazz", "classical", "news", "talk", "electronic").shuffled().take(3).joinToString(",")
                Bitrate = listOf(64, 128, 192, 320).random()
                Codec = listOf("MP3", "AAC", "OGG").random()
                IconUrl = "https://picsum.photos/120/120?random=${prefix.hashCode() + index}"
            }
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
                android.util.Log.d("LegacyMainScreenViewModel", "Adding station to history: ${station.Name}")
                manager.add(station)
                android.util.Log.d("LegacyMainScreenViewModel", "History now has ${manager.getList().size} stations")
            } ?: run {
                android.util.Log.w("LegacyMainScreenViewModel", "HistoryManager is null, cannot add station to history")
            }

            // Обновляем локальный список истории
            loadHistoryFromManager()
        }
    }

    fun pauseStation() {
        _uiState.value = _uiState.value.copy(isPlaying = false)
    }

    fun toggleFavorite(station: DataRadioStation) {
        // TODO: Implement favorite toggle logic
        android.util.Log.d("LegacyMainScreenViewModel", "Toggle favorite for station: ${station.Name}")
    }

    fun searchStations(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                delay(500) // Симуляция поиска
                
                val searchResults = createMockStations("search", 10).map { station ->
                    station.apply {
                        Name = "$query Station ${station.Name}"
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    searchResults = searchResults,
                    isLoading = false
                )
                
                android.util.Log.d("LegacyMainScreenViewModel", "Search completed for query: $query")
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun refreshLocalStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000)
            val newStations = createMockStations("local", 15)
            _uiState.value = _uiState.value.copy(
                localStations = newStations,
                isLoading = false
            )
        }
    }

    fun refreshTopClickStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000)
            val newStations = createMockStations("topclick", 15)
            _uiState.value = _uiState.value.copy(
                topClickStations = newStations,
                isLoading = false
            )
        }
    }

    fun refreshTopVoteStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000)
            val newStations = createMockStations("topvote", 15)
            _uiState.value = _uiState.value.copy(
                topVoteStations = newStations,
                isLoading = false
            )
        }
    }

    fun refreshRecentStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000)
            val newStations = createMockStations("recent", 15)
            _uiState.value = _uiState.value.copy(
                recentStations = newStations,
                isLoading = false
            )
        }
    }

    fun refreshCurrentlyHeardStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000)
            val newStations = createMockStations("currently", 15)
            _uiState.value = _uiState.value.copy(
                currentlyHeardStations = newStations,
                isLoading = false
            )
        }
    }

    fun refreshFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000)
            val newStations = createMockStations("favorite", 10)
            _uiState.value = _uiState.value.copy(
                favoriteStations = newStations,
                isLoading = false
            )
        }
    }
}
