package com.eosphor.nonameradio.compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eosphor.nonameradio.station.DataRadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class OriginalRadioDroidUiState(
    val isLoading: Boolean = false,
    val currentStation: DataRadioStation? = null,
    val isPlaying: Boolean = false,
    val localStations: List<DataRadioStation> = emptyList(),
    val topClickStations: List<DataRadioStation> = emptyList(),
    val topVoteStations: List<DataRadioStation> = emptyList(),
    val changedLatelyStations: List<DataRadioStation> = emptyList(),
    val currentlyHeardStations: List<DataRadioStation> = emptyList(),
    val favoriteStations: List<DataRadioStation> = emptyList(),
    val historyStations: List<DataRadioStation> = emptyList(),
    val searchResults: List<DataRadioStation> = emptyList(),
    val tagCategories: List<String> = emptyList(),
    val countryCategories: List<String> = emptyList(),
    val languageCategories: List<String> = emptyList(),
    val error: String? = null
)

class OriginalRadioDroidViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OriginalRadioDroidUiState())
    val uiState: StateFlow<OriginalRadioDroidUiState> = _uiState.asStateFlow()

    private var historyManager: com.eosphor.nonameradio.HistoryManager? = null

    init {
        loadInitialData()
    }

    fun initializeHistoryManager(historyManager: com.eosphor.nonameradio.HistoryManager) {
        this.historyManager = historyManager
        android.util.Log.d("OriginalRadioDroidViewModel", "HistoryManager initialized")
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

                // Загружаем все категории станций точно как в оригинале
                val localStations = createOriginalStations("local", 20)
                val topClickStations = createOriginalStations("topclick", 20)
                val topVoteStations = createOriginalStations("topvote", 20)
                val changedLatelyStations = createOriginalStations("changed", 20)
                val currentlyHeardStations = createOriginalStations("currently", 20)
                val favoriteStations = createOriginalStations("favorite", 15)

                // Загружаем категории точно как в оригинале
                val tagCategories = listOf(
                    "pop", "rock", "jazz", "classical", "news", "talk", "electronic", "folk",
                    "country", "blues", "reggae", "hip hop", "metal", "alternative", "indie", "ambient"
                )
                val countryCategories = listOf(
                    "United States", "United Kingdom", "Germany", "France", "Italy", "Spain", 
                    "Russia", "Canada", "Australia", "Netherlands", "Sweden", "Norway", 
                    "Denmark", "Finland", "Poland", "Czech Republic"
                )
                val languageCategories = listOf(
                    "English", "Spanish", "French", "German", "Italian", "Russian", 
                    "Portuguese", "Dutch", "Swedish", "Norwegian", "Danish", "Finnish",
                    "Polish", "Czech", "Japanese", "Chinese"
                )

                _uiState.value = _uiState.value.copy(
                    localStations = localStations,
                    topClickStations = topClickStations,
                    topVoteStations = topVoteStations,
                    changedLatelyStations = changedLatelyStations,
                    currentlyHeardStations = currentlyHeardStations,
                    favoriteStations = favoriteStations,
                    tagCategories = tagCategories,
                    countryCategories = countryCategories,
                    languageCategories = languageCategories,
                    isLoading = false
                )

                android.util.Log.d("OriginalRadioDroidViewModel", "Loaded all data successfully")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun createOriginalStations(prefix: String, count: Int): List<DataRadioStation> {
        return (1..count).map { index ->
            DataRadioStation().apply {
                StationUuid = "$prefix-station-$index"
                Name = when (prefix) {
                    "local" -> "Local Radio $index"
                    "topclick" -> "Top Click Radio $index"
                    "topvote" -> "Top Vote Radio $index"
                    "changed" -> "Recently Changed Radio $index"
                    "currently" -> "Currently Heard Radio $index"
                    "favorite" -> "Favorite Radio $index"
                    else -> "Radio Station $index"
                }
                Country = listOf("US", "UK", "DE", "FR", "IT", "ES", "RU", "CA", "AU", "NL").random()
                Language = listOf("English", "Spanish", "French", "German", "Italian", "Russian").random()
                TagsAll = listOf("pop", "rock", "jazz", "classical", "news", "talk", "electronic", "folk").shuffled().take(3).joinToString(",")
                Bitrate = listOf(64, 128, 192, 320).random()
                Codec = listOf("MP3", "AAC", "OGG").random()
                IconUrl = "https://picsum.photos/70/70?random=${prefix.hashCode() + index}"
                Working = true
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
                android.util.Log.d("OriginalRadioDroidViewModel", "Adding station to history: ${station.Name}")
                manager.add(station)
                android.util.Log.d("OriginalRadioDroidViewModel", "History now has ${manager.getList().size} stations")
            } ?: run {
                android.util.Log.w("OriginalRadioDroidViewModel", "HistoryManager is null, cannot add station to history")
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
        android.util.Log.d("OriginalRadioDroidViewModel", "Toggle favorite for station: ${station.Name}")
    }

    fun searchStations(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                delay(500) // Симуляция поиска
                
                val searchResults = createOriginalStations("search", 15).map { station ->
                    station.apply {
                        Name = "$query Radio ${station.Name}"
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    searchResults = searchResults,
                    isLoading = false
                )
                
                android.util.Log.d("OriginalRadioDroidViewModel", "Search completed for query: $query")
                
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
            val newStations = createOriginalStations("local", 20)
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
            val newStations = createOriginalStations("topclick", 20)
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
            val newStations = createOriginalStations("topvote", 20)
            _uiState.value = _uiState.value.copy(
                topVoteStations = newStations,
                isLoading = false
            )
        }
    }

    fun refreshChangedLatelyStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000)
            val newStations = createOriginalStations("changed", 20)
            _uiState.value = _uiState.value.copy(
                changedLatelyStations = newStations,
                isLoading = false
            )
        }
    }

    fun refreshCurrentlyHeardStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000)
            val newStations = createOriginalStations("currently", 20)
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
            val newStations = createOriginalStations("favorite", 15)
            _uiState.value = _uiState.value.copy(
                favoriteStations = newStations,
                isLoading = false
            )
        }
    }
}
