package com.eosphor.nonameradio.compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eosphor.nonameradio.CountryCodeDictionary
import com.eosphor.nonameradio.CountryFlagsLoader
import com.eosphor.nonameradio.data.DataCategory
import com.eosphor.nonameradio.station.StationsFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CategoriesScreenUiState(
    val categories: List<DataCategory> = emptyList(),
    val filteredCategories: List<DataCategory> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val searchQuery: String = "",
    val showSingleUseTags: Boolean = false
)

class CategoriesScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CategoriesScreenUiState())
    val uiState: StateFlow<CategoriesScreenUiState> = _uiState.asStateFlow()

    private var searchStyle: StationsFilter.SearchStyle = StationsFilter.SearchStyle.ByName
    private var singleUseFilter: Boolean = false
    private var countryCodeDictionary: CountryCodeDictionary? = null
    private var countryFlagsLoader: CountryFlagsLoader? = null

    fun initialize(
        searchStyle: StationsFilter.SearchStyle,
        singleUseFilter: Boolean = false,
        countryCodeDictionary: CountryCodeDictionary? = null,
        countryFlagsLoader: CountryFlagsLoader? = null
    ) {
        this.searchStyle = searchStyle
        this.singleUseFilter = singleUseFilter
        this.countryCodeDictionary = countryCodeDictionary
        this.countryFlagsLoader = countryFlagsLoader
        
        loadCategories()
    }

    fun setSearchStyle(searchStyle: StationsFilter.SearchStyle) {
        this.searchStyle = searchStyle
        loadCategories()
    }

    fun setSingleUseFilter(enabled: Boolean) {
        singleUseFilter = enabled
        filterCategories()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterCategories()
    }

    fun toggleSingleUseTags() {
        _uiState.value = _uiState.value.copy(showSingleUseTags = !_uiState.value.showSingleUseTags)
        filterCategories()
    }

    fun refreshCategories() {
        _uiState.value = _uiState.value.copy(isRefreshing = true, hasError = false)
        
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Simulate network call - in real implementation, this would fetch from API
                    kotlinx.coroutines.delay(1000)
                }
                
                loadCategories()
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    hasError = true,
                    errorMessage = e.message ?: "Failed to refresh categories"
                )
            }
        }
    }

    private fun loadCategories() {
        _uiState.value = _uiState.value.copy(isLoading = true, hasError = false)
        
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // TODO: Load categories from API or local storage
                    // For now, simulate with empty list
                    val mockCategories = createMockCategories()
                    
                    // Process categories based on search style
                    val processedCategories = processCategories(mockCategories)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categories = processedCategories
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasError = true,
                    errorMessage = e.message ?: "Failed to load categories"
                )
            }
        }
        
        filterCategories()
    }

    private fun createMockCategories(): List<DataCategory> {
        return when (searchStyle) {
            StationsFilter.SearchStyle.ByTagExact -> {
                listOf(
                    DataCategory().apply { Name = "pop"; UsedCount = 1250 },
                    DataCategory().apply { Name = "rock"; UsedCount = 980 },
                    DataCategory().apply { Name = "jazz"; UsedCount = 450 },
                    DataCategory().apply { Name = "classical"; UsedCount = 320 },
                    DataCategory().apply { Name = "news"; UsedCount = 280 },
                    DataCategory().apply { Name = "talk"; UsedCount = 200 },
                    DataCategory().apply { Name = "country"; UsedCount = 180 },
                    DataCategory().apply { Name = "electronic"; UsedCount = 150 },
                    DataCategory().apply { Name = "blues"; UsedCount = 120 },
                    DataCategory().apply { Name = "folk"; UsedCount = 100 }
                )
            }
            StationsFilter.SearchStyle.ByLanguageExact -> {
                listOf(
                    DataCategory().apply { Name = "english"; UsedCount = 2500 },
                    DataCategory().apply { Name = "spanish"; UsedCount = 1200 },
                    DataCategory().apply { Name = "french"; UsedCount = 800 },
                    DataCategory().apply { Name = "german"; UsedCount = 750 },
                    DataCategory().apply { Name = "italian"; UsedCount = 600 },
                    DataCategory().apply { Name = "portuguese"; UsedCount = 500 },
                    DataCategory().apply { Name = "russian"; UsedCount = 400 },
                    DataCategory().apply { Name = "chinese"; UsedCount = 350 },
                    DataCategory().apply { Name = "japanese"; UsedCount = 200 },
                    DataCategory().apply { Name = "arabic"; UsedCount = 180 }
                )
            }
            StationsFilter.SearchStyle.ByCountryCodeExact -> {
                listOf(
                    DataCategory().apply { Name = "US"; UsedCount = 1200 },
                    DataCategory().apply { Name = "GB"; UsedCount = 800 },
                    DataCategory().apply { Name = "DE"; UsedCount = 750 },
                    DataCategory().apply { Name = "FR"; UsedCount = 600 },
                    DataCategory().apply { Name = "CA"; UsedCount = 500 },
                    DataCategory().apply { Name = "AU"; UsedCount = 450 },
                    DataCategory().apply { Name = "IT"; UsedCount = 400 },
                    DataCategory().apply { Name = "ES"; UsedCount = 350 },
                    DataCategory().apply { Name = "NL"; UsedCount = 300 },
                    DataCategory().apply { Name = "BR"; UsedCount = 280 }
                )
            }
            else -> {
                listOf(
                    DataCategory().apply { Name = "BBC Radio 1"; UsedCount = 1500 },
                    DataCategory().apply { Name = "Radio France"; UsedCount = 1200 },
                    DataCategory().apply { Name = "Deutschlandfunk"; UsedCount = 1000 },
                    DataCategory().apply { Name = "Radio Canada"; UsedCount = 800 },
                    DataCategory().apply { Name = "ABC Radio"; UsedCount = 700 },
                    DataCategory().apply { Name = "Radio Italia"; UsedCount = 600 },
                    DataCategory().apply { Name = "RNE Radio"; UsedCount = 500 },
                    DataCategory().apply { Name = "NOS Radio"; UsedCount = 400 },
                    DataCategory().apply { Name = "Radio Globo"; UsedCount = 350 },
                    DataCategory().apply { Name = "Radio Moscow"; UsedCount = 300 }
                )
            }
        }
    }

    private fun processCategories(categories: List<DataCategory>): List<DataCategory> {
        return categories.map { category ->
            if (searchStyle == StationsFilter.SearchStyle.ByCountryCodeExact) {
                // Set country name and flag for country codes
                category.Label = countryCodeDictionary?.getCountryByCode(category.Name)
                // Note: Icon will be set in the UI layer using CountryFlagsLoader
            }
            category
        }
    }

    private fun filterCategories() {
        val categories = _uiState.value.categories
        val searchQuery = _uiState.value.searchQuery.lowercase()
        val showSingleUseTags = _uiState.value.showSingleUseTags

        val filtered = categories.filter { category ->
            // Filter by search query
            val matchesSearch = searchQuery.isEmpty() || 
                category.Name.lowercase().contains(searchQuery) ||
                category.Label?.lowercase()?.contains(searchQuery) == true

            // Filter by single use tags setting
            val matchesSingleUseFilter = if (singleUseFilter) {
                showSingleUseTags || category.UsedCount > 1
            } else {
                true
            }

            matchesSearch && matchesSingleUseFilter
        }.sortedBy { it.getSortField() }

        _uiState.value = _uiState.value.copy(filteredCategories = filtered)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            hasError = false,
            errorMessage = ""
        )
    }

    fun getSearchStyleTitle(): String {
        return when (searchStyle) {
            StationsFilter.SearchStyle.ByTagExact -> "Теги"
            StationsFilter.SearchStyle.ByLanguageExact -> "Языки"
            StationsFilter.SearchStyle.ByCountryCodeExact -> "Страны"
            else -> "Категории"
        }
    }
}
