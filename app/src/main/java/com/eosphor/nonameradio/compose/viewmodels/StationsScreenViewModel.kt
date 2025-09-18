package com.eosphor.nonameradio.compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eosphor.nonameradio.station.DataRadioStation
import com.eosphor.nonameradio.station.StationsFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StationsScreenUiState(
    val stations: List<DataRadioStation> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val searchEnabled: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val searchStyle: StationsFilter.SearchStyle = StationsFilter.SearchStyle.ByName
)

class StationsScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StationsScreenUiState())
    val uiState: StateFlow<StationsScreenUiState> = _uiState.asStateFlow()

    private var stationsFilter: StationsFilter? = null
    private var allStations: List<DataRadioStation> = emptyList()

    fun loadStations(url: String, context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, hasError = false)
            
            try {
                // TODO: Implement actual station loading logic
                // This should integrate with the existing StationsFilter
                val stations = loadStationsFromUrl(url)
                allStations = stations
                _uiState.value = _uiState.value.copy(
                    stations = stations,
                    isLoading = false,
                    hasError = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun refreshStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                // TODO: Implement refresh logic
                // This should reload stations from the current URL
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    hasError = true,
                    errorMessage = e.message ?: "Refresh failed"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        if (query.isNotEmpty()) {
            searchStations(query)
        } else {
            // Show all stations when search is cleared
            _uiState.value = _uiState.value.copy(stations = allStations)
        }
    }

    fun searchStations(query: String) {
        viewModelScope.launch {
            try {
                // TODO: Implement search logic using StationsFilter
                val filteredStations = allStations.filter { station ->
                    station.Name?.contains(query, ignoreCase = true) == true ||
                    station.Country?.contains(query, ignoreCase = true) == true ||
                    station.TagsAll?.contains(query, ignoreCase = true) == true
                }
                
                _uiState.value = _uiState.value.copy(stations = filteredStations)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    hasError = true,
                    errorMessage = e.message ?: "Search failed"
                )
            }
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            stations = allStations,
            isSearchActive = false
        )
    }

    fun setSearchActive(active: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchActive = active)
    }

    fun setSearchEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(searchEnabled = enabled)
    }

    fun setSearchStyle(style: StationsFilter.SearchStyle) {
        _uiState.value = _uiState.value.copy(searchStyle = style)
    }

    private suspend fun loadStationsFromUrl(url: String): List<DataRadioStation> {
        // TODO: Implement actual loading from URL
        // This should use the existing DataRadioStation.DecodeJson logic
        return emptyList()
    }
}
