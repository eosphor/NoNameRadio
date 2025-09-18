package com.eosphor.nonameradio.compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.eosphor.nonameradio.station.DataRadioStation

data class StationListUiState(
    val allStations: List<DataRadioStation> = emptyList(),
    val filteredStations: List<DataRadioStation> = emptyList(),
    val favoriteStationIds: Set<String> = emptySet(),
    val currentPlayingStationId: String? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isSearchMode: Boolean = false,
    val showFilters: Boolean = false,
    val showSortMenu: Boolean = false,
    val selectedCountry: String? = null,
    val selectedLanguage: String? = null,
    val selectedCodec: String? = null,
    val sortOption: String = "name",
    val totalStations: Int = 0,
    val hasActiveFilters: Boolean = false
)

class StationListViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(StationListUiState())
    val uiState: StateFlow<StationListUiState> = _uiState.asStateFlow()
    
    private val _allStations = MutableStateFlow<List<DataRadioStation>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCountry = MutableStateFlow<String?>(null)
    private val _selectedLanguage = MutableStateFlow<String?>(null)
    private val _selectedCodec = MutableStateFlow<String?>(null)
    private val _sortOption = MutableStateFlow("name")
    
    init {
        loadStations()
        setupFiltering()
    }
    
    private fun setupFiltering() {
        viewModelScope.launch {
            combine(
                _allStations,
                _searchQuery,
                _selectedCountry,
                _selectedLanguage,
                _selectedCodec,
                _sortOption
            ) { flows ->
                val stations = flows[0] as List<DataRadioStation>
                val query = flows[1] as String
                val country = flows[2] as String?
                val language = flows[3] as String?
                val codec = flows[4] as String?
                val sort = flows[5] as String
                filterAndSortStations(stations, query, country, language, codec, sort)
            }.collect { filteredStations ->
                _uiState.value = _uiState.value.copy(
                    filteredStations = filteredStations,
                    hasActiveFilters = hasActiveFilters()
                )
            }
        }
    }
    
    private fun filterAndSortStations(
        stations: List<DataRadioStation>,
        query: String,
        country: String?,
        language: String?,
        codec: String?,
        sortOption: String
    ): List<DataRadioStation> {
        var filtered = stations
        
        // Фильтрация по поисковому запросу
        if (query.isNotEmpty()) {
            filtered = filtered.filter { station ->
                station.Name?.contains(query, ignoreCase = true) == true ||
                station.Country?.contains(query, ignoreCase = true) == true ||
                station.TagsAll?.contains(query, ignoreCase = true) == true
            }
        }
        
        // Фильтрация по стране
        if (country != null) {
            filtered = filtered.filter { it.Country == country }
        }
        
        // Фильтрация по языку
        if (language != null) {
            filtered = filtered.filter { it.Language == language }
        }
        
        // Фильтрация по кодеку
        if (codec != null) {
            filtered = filtered.filter { it.Codec == codec }
        }
        
        // Сортировка
        return when (sortOption) {
            "name" -> filtered.sortedBy { it.Name?.lowercase() }
            "country" -> filtered.sortedBy { it.Country?.lowercase() }
            "bitrate" -> filtered.sortedByDescending { it.Bitrate }
            "codec" -> filtered.sortedBy { it.Codec?.lowercase() }
            "recent" -> filtered.reversed() // Предполагаем, что последние добавленные в конце
            else -> filtered
        }
    }
    
    private fun hasActiveFilters(): Boolean {
        return _searchQuery.value.isNotEmpty() ||
               _selectedCountry.value != null ||
               _selectedLanguage.value != null ||
               _selectedCodec.value != null
    }
    
    private fun loadStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Симуляция загрузки данных
                delay(1000)
                
                val stations = generateSampleStations()
                _allStations.value = stations
                
                _uiState.value = _uiState.value.copy(
                    allStations = stations,
                    totalStations = stations.size,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }
    
    private fun generateSampleStations(): List<DataRadioStation> {
        val countries = listOf("Germany", "France", "USA", "United Kingdom", "Switzerland", "Italy", "Spain")
        val languages = listOf("English", "German", "French", "Spanish", "Italian")
        val codecs = listOf("MP3", "AAC", "OGG")
        val genres = listOf("pop", "rock", "jazz", "classical", "electronic", "folk", "country")
        
        return (1..100).map { index ->
            DataRadioStation().apply {
                StationUuid = java.util.UUID.randomUUID().toString()
                Name = "Radio Station $index"
                Country = countries.random()
                Language = languages.random()
                Codec = codecs.random()
                Bitrate = listOf(64, 128, 192, 256, 320).random()
                TagsAll = genres.shuffled().take(2).joinToString(",")
                IconUrl = null
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun toggleSearchMode() {
        val newSearchMode = !_uiState.value.isSearchMode
        _uiState.value = _uiState.value.copy(isSearchMode = newSearchMode)
        
        if (!newSearchMode) {
            _searchQuery.value = ""
            _uiState.value = _uiState.value.copy(searchQuery = "")
        }
    }
    
    fun toggleFilterMenu() {
        _uiState.value = _uiState.value.copy(showFilters = !_uiState.value.showFilters)
    }
    
    fun toggleSortMenu() {
        _uiState.value = _uiState.value.copy(showSortMenu = !_uiState.value.showSortMenu)
    }
    
    fun setCountryFilter(country: String?) {
        _selectedCountry.value = country
        _uiState.value = _uiState.value.copy(selectedCountry = country)
    }
    
    fun setLanguageFilter(language: String?) {
        _selectedLanguage.value = language
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
    }
    
    fun setCodecFilter(codec: String?) {
        _selectedCodec.value = codec
        _uiState.value = _uiState.value.copy(selectedCodec = codec)
    }
    
    fun setSortOption(option: String) {
        _sortOption.value = option
        _uiState.value = _uiState.value.copy(sortOption = option)
    }
    
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCountry.value = null
        _selectedLanguage.value = null
        _selectedCodec.value = null
        
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedCountry = null,
            selectedLanguage = null,
            selectedCodec = null,
            showFilters = false
        )
    }
    
    fun playStation(station: DataRadioStation) {
        _uiState.value = _uiState.value.copy(
            currentPlayingStationId = station.StationUuid
        )
    }
    
    fun toggleFavorite(station: DataRadioStation) {
        val currentFavorites = _uiState.value.favoriteStationIds.toMutableSet()
        val stationId = station.StationUuid ?: return
        
        if (stationId in currentFavorites) {
            currentFavorites.remove(stationId)
        } else {
            currentFavorites.add(stationId)
        }
        
        _uiState.value = _uiState.value.copy(favoriteStationIds = currentFavorites)
    }
    
    fun retryLoading() {
        loadStations()
    }
    
    fun loadMoreStations() {
        if (_uiState.value.isLoadingMore) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            
            try {
                // Симуляция загрузки дополнительных данных
                delay(1000)
                
                val additionalStations = generateSampleStations().take(20)
                val currentStations = _allStations.value.toMutableList()
                currentStations.addAll(additionalStations)
                
                _allStations.value = currentStations
                _uiState.value = _uiState.value.copy(
                    allStations = currentStations,
                    totalStations = currentStations.size,
                    isLoadingMore = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Ошибка загрузки дополнительных данных"
                )
            }
        }
    }
}