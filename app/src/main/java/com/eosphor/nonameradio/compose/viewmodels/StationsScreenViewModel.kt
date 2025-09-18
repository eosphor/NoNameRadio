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
                // For now, simulate loading with mock data
                allStations = createMockStations()
                android.util.Log.d("StationsScreenViewModel", "Loaded ${allStations.size} stations")
                allStations.forEach { station ->
                    android.util.Log.d("StationsScreenViewModel", "Station: ${station.Name}, IconUrl: ${station.IconUrl}")
                }
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
                // For now, simulate refresh with mock data
                allStations = createMockStations()
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

    private fun createMockStations(): List<DataRadioStation> {
        return listOf(
            DataRadioStation().apply {
                StationUuid = "station-1"
                Name = "BBC Radio 1"
                Country = "GB"
                Language = "English"
                Bitrate = 128
                Codec = "MP3"
                IconUrl = "https://picsum.photos/120/120?random=1"
                TagsAll = "pop, music, entertainment"
                StreamUrl = "http://stream.live.vc.bbcmedia.co.uk/bbc_radio_one"
            },
            DataRadioStation().apply {
                StationUuid = "station-2"
                Name = "Radio France Inter"
                Country = "FR"
                Language = "French"
                Bitrate = 128
                Codec = "MP3"
                IconUrl = "https://picsum.photos/120/120?random=2"
                TagsAll = "talk, news, culture"
                StreamUrl = "http://direct.franceinter.fr/live/franceinter-midfi.mp3"
            },
            DataRadioStation().apply {
                StationUuid = "station-3"
                Name = "Deutschlandfunk"
                Country = "DE"
                Language = "German"
                Bitrate = 128
                Codec = "MP3"
                IconUrl = "https://picsum.photos/120/120?random=3"
                TagsAll = "news, talk, culture"
                StreamUrl = "https://st01.sslstream.dlf.de/dlf/01/128/mp3/stream.mp3"
            },
            DataRadioStation().apply {
                StationUuid = "station-4"
                Name = "Radio Canada"
                Country = "CA"
                Language = "French"
                Bitrate = 128
                Codec = "MP3"
                IconUrl = "https://picsum.photos/120/120?random=4"
                TagsAll = "news, talk, music"
                StreamUrl = "https://rcavlive.akamaized.net/hls/live/704025/xcanrcav/master.m3u8"
            },
            DataRadioStation().apply {
                StationUuid = "station-5"
                Name = "ABC Classic FM"
                Country = "AU"
                Language = "English"
                Bitrate = 128
                Codec = "MP3"
                IconUrl = "https://picsum.photos/120/120?random=5"
                TagsAll = "classical, music, culture"
                StreamUrl = "https://live-radio01.mediahubaustralia.com/2FCW/mp3/"
            },
            DataRadioStation().apply {
                StationUuid = "station-6"
                Name = "Radio Italia"
                Country = "IT"
                Language = "Italian"
                Bitrate = 128
                Codec = "MP3"
                IconUrl = "https://picsum.photos/120/120?random=6"
                TagsAll = "pop, music, italian"
                StreamUrl = "https://streaming.radioitalia.it/radioitalia/radioitalia/playlist.m3u8"
            },
            DataRadioStation().apply {
                StationUuid = "station-7"
                Name = "RNE Radio Nacional"
                Country = "ES"
                Language = "Spanish"
                Bitrate = 128
                Codec = "MP3"
                IconUrl = "https://picsum.photos/120/120?random=7"
                TagsAll = "news, talk, culture"
                StreamUrl = "https://crtve-rne1.cast.addradio.de/crtve/rne1/mp3/high"
            },
            DataRadioStation().apply {
                StationUuid = "station-8"
                Name = "NOS Radio 1"
                Country = "NL"
                Language = "Dutch"
                Bitrate = 128
                Codec = "MP3"
                IconUrl = "https://picsum.photos/120/120?random=8"
                TagsAll = "news, talk, music"
                StreamUrl = "https://icecast.omroep.nl/radio1-bb-mp3"
            },
            DataRadioStation().apply {
                StationUuid = "station-9"
                Name = "Radio Globo"
                Country = "BR"
                Language = "Portuguese"
                Bitrate = 128
                Codec = "MP3"
                IconUrl = "https://picsum.photos/120/120?random=9"
                TagsAll = "pop, music, brazilian"
                StreamUrl = "https://radiostream.globo.com/globo_sp.aac"
            },
            DataRadioStation().apply {
                StationUuid = "station-10"
                Name = "Radio Moscow"
                Country = "RU"
                Language = "Russian"
                Bitrate = 128
                Codec = "MP3"
                IconUrl = "https://picsum.photos/120/120?random=10"
                TagsAll = "news, talk, russian"
                StreamUrl = "https://icecast.vgtrk.cdnvideo.ru/rmfmmp3"
            }
        )
    }
}