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
    val filteredStations: List<DataRadioStation> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val searchEnabled: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val searchStyle: StationsFilter.SearchStyle = StationsFilter.SearchStyle.ByName,
    val totalStations: Int = 0,
    val filteredCount: Int = 0
)

class StationsScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StationsScreenUiState())
    val uiState: StateFlow<StationsScreenUiState> = _uiState.asStateFlow()

    private var allStations: List<DataRadioStation> = emptyList()

    init {
        loadStations()
    }

    fun loadStations() {
        _uiState.value = _uiState.value.copy(isLoading = true, hasError = false)
        
        viewModelScope.launch {
            try {
                // TODO: Load stations from repository
                // For now, simulate loading with empty list
                allStations = emptyList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    stations = allStations,
                    totalStations = allStations.size,
                    filteredStations = allStations
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasError = true,
                    errorMessage = e.message ?: "Failed to load stations"
                )
            }
        }
    }

    fun refreshStations() {
        _uiState.value = _uiState.value.copy(isRefreshing = true, hasError = false)
        
        viewModelScope.launch {
            try {
                // TODO: Refresh stations from repository
                // For now, simulate refresh
                allStations = emptyList()
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    stations = allStations,
                    totalStations = allStations.size,
                    filteredStations = allStations
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    hasError = true,
                    errorMessage = e.message ?: "Failed to refresh stations"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterStations(query)
    }

    fun updateSearchStyle(style: StationsFilter.SearchStyle) {
        _uiState.value = _uiState.value.copy(searchStyle = style)
        filterStations(_uiState.value.searchQuery)
    }

    fun toggleSearch() {
        _uiState.value = _uiState.value.copy(searchEnabled = !_uiState.value.searchEnabled)
        
        if (!_uiState.value.searchEnabled) {
            _uiState.value = _uiState.value.copy(
                searchQuery = "",
                filteredStations = allStations,
                filteredCount = allStations.size
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(hasError = false, errorMessage = "")
    }

    private fun filterStations(query: String) {
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                filteredStations = allStations,
                filteredCount = allStations.size
            )
            return
        }

        val filtered = allStations.filter { station ->
            when (_uiState.value.searchStyle) {
                StationsFilter.SearchStyle.ByName -> {
                    station.Name?.contains(query, ignoreCase = true) == true
                }
                StationsFilter.SearchStyle.ByLanguageExact -> {
                    station.Language?.equals(query, ignoreCase = true) == true
                }
                StationsFilter.SearchStyle.ByCountryCodeExact -> {
                    station.Country?.equals(query, ignoreCase = true) == true
                }
                StationsFilter.SearchStyle.ByTagExact -> {
                    station.TagsAll?.equals(query, ignoreCase = true) == true
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            filteredStations = filtered,
            filteredCount = filtered.size
        )
    }
}