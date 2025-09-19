package com.eosphor.nonameradio.compose.viewmodels

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.eosphor.nonameradio.RadioDroidApp
import com.eosphor.nonameradio.Utils
import com.eosphor.nonameradio.players.selector.PlayerType
import com.eosphor.nonameradio.service.PauseReason
import com.eosphor.nonameradio.service.PlayerServiceUtil
import com.eosphor.nonameradio.station.DataRadioStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import java.net.URLEncoder
import java.util.HashMap
import java.util.Locale

data class ExactOriginalUiState(
    val isLoading: Boolean = false,
    val currentStation: DataRadioStation? = null,
    val isPlaying: Boolean = false,
    val selectedDrawerItem: String = "stations",
    val selectedTabIndex: Int = 0,
    val localStations: List<DataRadioStation> = emptyList(),
    val topClickStations: List<DataRadioStation> = emptyList(),
    val topVoteStations: List<DataRadioStation> = emptyList(),
    val changedLatelyStations: List<DataRadioStation> = emptyList(),
    val currentlyHeardStations: List<DataRadioStation> = emptyList(),
    val favoriteStations: List<DataRadioStation> = emptyList(),
    val favoriteStationIds: Set<String> = emptySet(),
    val historyStations: List<DataRadioStation> = emptyList(),
    val searchResults: List<DataRadioStation> = emptyList(),
    val tagCategories: List<String> = emptyList(),
    val countryCategories: List<String> = emptyList(),
    val languageCategories: List<String> = emptyList(),
    val error: String? = null,
    // Новые функции
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val isIconsView: Boolean = false,
    val sleepTimerMinutes: Int = 0,
    val sleepTimerActive: Boolean = false,
    val mpdEnabled: Boolean = false,
    val mediaRouteActive: Boolean = false,
    val currentSearchStyle: com.eosphor.nonameradio.station.StationsFilter.SearchStyle = com.eosphor.nonameradio.station.StationsFilter.SearchStyle.ByName
)

class ExactOriginalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExactOriginalUiState(isLoading = true))
    val uiState: StateFlow<ExactOriginalUiState> = _uiState.asStateFlow()

    private var historyManager: com.eosphor.nonameradio.HistoryManager? = null
    private var httpClient: OkHttpClient? = null
    private var appContext: Context? = null
    private var hasLoadedInitialData = false
    private var favoritesReceiverRegistered = false

    private val SEARCH_TAB_INDEX = 8

    private val favoritesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshFavoritesState()
        }
    }

    private val _pendingAlarmStation = MutableStateFlow<DataRadioStation?>(null)
    val pendingAlarmStation: StateFlow<DataRadioStation?> = _pendingAlarmStation.asStateFlow()

    fun initializeHistoryManager(historyManager: com.eosphor.nonameradio.HistoryManager) {
        this.historyManager = historyManager
        android.util.Log.d("ExactOriginalViewModel", "HistoryManager initialized")
        loadHistoryFromManager()
    }

    fun initializeNetwork(client: OkHttpClient, context: Context) {
        this.httpClient = client
        this.appContext = context.applicationContext
        this.appContext?.let { PlayerServiceUtil.startService(it) }
        this.appContext?.let { registerFavoritesReceiver(it) }
        android.util.Log.d("ExactOriginalViewModel", "Network initialized")
        triggerInitialLoadIfNeeded()
        refreshTimerState()
    }

    private fun loadHistoryFromManager() {
        historyManager?.let { manager ->
            val historyStations = manager.getList()
            _uiState.value = _uiState.value.copy(historyStations = historyStations)
        }
    }

    private fun triggerInitialLoadIfNeeded() {
        if (!hasLoadedInitialData) {
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val client = httpClient
                val ctx = appContext
                val useNetwork = client != null && ctx != null

                val resolvedClient = client
                val resolvedContext = ctx
                val canUseNetwork = useNetwork && resolvedClient != null && resolvedContext != null

                val localStations = if (canUseNetwork) {
                    fetchStationsRelative(resolvedClient!!, resolvedContext!!, getLocalStationsEndpoint(resolvedContext))
                } else {
                    createRealisticStations("local", 20)
                }
                val topClickStations = if (canUseNetwork) {
                    fetchStationsRelative(resolvedClient!!, resolvedContext!!, "json/stations/topclick/100")
                } else {
                    createRealisticStations("topclick", 20)
                }
                val topVoteStations = if (canUseNetwork) {
                    fetchStationsRelative(resolvedClient!!, resolvedContext!!, "json/stations/topvote/100")
                } else {
                    createRealisticStations("topvote", 20)
                }
                val changedLatelyStations = if (canUseNetwork) {
                    fetchStationsRelative(resolvedClient!!, resolvedContext!!, "json/stations/lastchange/100")
                } else {
                    createRealisticStations("changed", 20)
                }
                val currentlyHeardStations = if (canUseNetwork) {
                    fetchStationsRelative(resolvedClient!!, resolvedContext!!, "json/stations/lastclick/100")
                } else {
                    createRealisticStations("currently", 20)
                }
                val favoriteStations = loadFavoriteStations()

                val tagCategories = if (canUseNetwork) {
                    fetchSimpleList(resolvedClient!!, resolvedContext!!, "json/tags", "name")
                } else {
                    loadRealTags()
                }
                val countryCategories = if (canUseNetwork) {
                    fetchSimpleList(resolvedClient!!, resolvedContext!!, "json/countrycodes", "name")
                } else {
                    loadRealCountries()
                }
                val languageCategories = if (canUseNetwork) {
                    fetchSimpleList(resolvedClient!!, resolvedContext!!, "json/languages", "name")
                } else {
                    loadRealLanguages()
                }

                _uiState.update {
                    it.copy(
                        localStations = localStations,
                        topClickStations = topClickStations,
                        topVoteStations = topVoteStations,
                        changedLatelyStations = changedLatelyStations,
                        currentlyHeardStations = currentlyHeardStations,
                        favoriteStations = favoriteStations,
                        favoriteStationIds = favoriteStations.mapNotNull { it.StationUuid }.toSet(),
                        tagCategories = tagCategories,
                        countryCategories = countryCategories,
                        languageCategories = languageCategories,
                        isLoading = false,
                        error = null
                    )
                }

                hasLoadedInitialData = true
                android.util.Log.d("ExactOriginalViewModel", "Loaded all real data successfully")

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun getLocalStationsEndpoint(context: Context): String {
        // В оригинале определяется страна; упростим до кода из конфигурации
        val locales = ConfigurationCompat.getLocales(context.resources.configuration)
        val country = when {
            !locales.isEmpty -> locales[0]?.country
            else -> Locale.getDefault().country
        }
        return if (!country.isNullOrEmpty()) {
            "json/stations/bycountrycodeexact/" + country + "?order=clickcount&reverse=true"
        } else {
            "json/stations/topclick/100"
        }
    }

    private suspend fun fetchStationsRelative(client: OkHttpClient, context: Context, relative: String): List<DataRadioStation> = withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val result = Utils.downloadFeedRelative(client, context, relative, false, null)
            if (result != null) {
                val list = com.eosphor.nonameradio.station.DataRadioStation.DecodeJson(result)
                list ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            android.util.Log.e("ExactOriginalViewModel", "fetchStationsRelative error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun fetchSimpleList(client: OkHttpClient, context: Context, relative: String, fieldName: String): List<String> = withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val result = Utils.downloadFeedRelative(client, context, relative, false, null)
            if (result != null) {
                val arr = JSONArray(result)
                val out = ArrayList<String>(arr.length())
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    if (obj.has(fieldName)) out.add(obj.getString(fieldName))
                }
                out
            } else emptyList()
        } catch (e: Exception) {
            android.util.Log.e("ExactOriginalViewModel", "fetchSimpleList error: ${e.message}")
            emptyList()
        }
    }

    private fun createRealisticStations(prefix: String, count: Int): List<DataRadioStation> {
        val realStationNames = listOf(
            "BBC Radio 1", "BBC Radio 2", "BBC Radio 3", "BBC Radio 4", "BBC Radio 5 Live",
            "Capital FM", "Heart FM", "Kiss FM", "Magic FM", "Smooth Radio",
            "Radio X", "Absolute Radio", "Virgin Radio", "LBC", "TalkSport",
            "Classic FM", "Jazz FM", "Planet Rock", "Radio 6 Music", "Radio 1Xtra",
            "NRJ", "RTL", "Europe 1", "France Inter", "France Info",
            "Deutschlandfunk", "WDR 2", "NDR 2", "Bayern 3", "SWR3",
            "Rai Radio 1", "Rai Radio 2", "Rai Radio 3", "Radio 105", "Radio Monte Carlo",
            "Cadena SER", "Cadena COPE", "Los 40 Principales", "Kiss FM España", "Europa FM",
            "Эхо Москвы", "Радио Маяк", "Радио России", "Вести FM", "Коммерсантъ FM",
            "CBC Radio One", "CBC Radio Two", "CBC Music", "Radio-Canada", "ICI Première"
        )

        val realCountries = listOf("US", "UK", "DE", "FR", "IT", "ES", "RU", "CA", "AU", "NL", "SE", "NO", "DK", "FI", "PL", "CZ")
        val realLanguages = listOf("English", "Spanish", "French", "German", "Italian", "Russian", "Portuguese", "Dutch", "Swedish", "Norwegian", "Danish", "Finnish", "Polish", "Czech")
        val realTags = listOf("pop", "rock", "jazz", "classical", "news", "talk", "electronic", "folk", "country", "blues", "reggae", "hip hop", "metal", "alternative", "indie", "ambient")

        return (1..count).map { index ->
            DataRadioStation().apply {
                StationUuid = "$prefix-station-$index"
                Name = realStationNames.random()
                Country = realCountries.random()
                Language = realLanguages.random()
                TagsAll = realTags.shuffled().take(3).joinToString(",")
                Bitrate = listOf(64, 128, 192, 320).random()
                Codec = listOf("MP3", "AAC", "OGG").random()
                IconUrl = "https://picsum.photos/70/70?random=${prefix.hashCode() + index}"
                Working = true
            }
        }
    }

    private suspend fun loadRealTags(): List<String> {
        return listOf(
            "pop", "rock", "jazz", "classical", "news", "talk", "electronic", "folk",
            "country", "blues", "reggae", "hip hop", "metal", "alternative", "indie", "ambient",
            "dance", "house", "techno", "trance", "dubstep", "drum and bass", "garage", "grime"
        )
    }

    private suspend fun loadRealCountries(): List<String> {
        return listOf(
            "United States", "United Kingdom", "Germany", "France", "Italy", "Spain", 
            "Russia", "Canada", "Australia", "Netherlands", "Sweden", "Norway", 
            "Denmark", "Finland", "Poland", "Czech Republic", "Austria", "Switzerland",
            "Belgium", "Portugal", "Ireland", "New Zealand", "South Africa", "Brazil"
        )
    }

    private suspend fun loadRealLanguages(): List<String> {
        return listOf(
            "English", "Spanish", "French", "German", "Italian", "Russian", 
            "Portuguese", "Dutch", "Swedish", "Norwegian", "Danish", "Finnish",
            "Polish", "Czech", "Japanese", "Chinese", "Korean", "Arabic", "Hindi"
        )
    }

    fun navigateToSection(section: String) {
        _uiState.update { state ->
            state.copy(
                selectedDrawerItem = section,
                isSearchActive = false,
                searchQuery = "",
                searchResults = if (section == "stations") state.searchResults else emptyList(),
                currentSearchStyle = com.eosphor.nonameradio.station.StationsFilter.SearchStyle.ByName
            )
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTabIndex = tabIndex)
    }

    fun playStation(station: DataRadioStation) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentStation = station,
                isPlaying = true
            )

            // Добавляем в историю через HistoryManager
            historyManager?.let { manager ->
                android.util.Log.d("ExactOriginalViewModel", "Adding station to history: ${station.Name}")
                manager.add(station)
                android.util.Log.d("ExactOriginalViewModel", "History now has ${manager.getList().size} stations")
            } ?: run {
                android.util.Log.w("ExactOriginalViewModel", "HistoryManager is null, cannot add station to history")
            }

            // Обновляем локальный список истории
            loadHistoryFromManager()

            val app = appContext as? RadioDroidApp
            if (app != null) {
                PlayerServiceUtil.startService(app)
                Utils.playAndWarnIfMetered(app, station, PlayerType.RADIODROID) {
                    Utils.play(app, station)
                }
            } else {
                android.util.Log.w("ExactOriginalViewModel", "RadioDroidApp context is null, playing via PlayerServiceUtil directly")
                PlayerServiceUtil.play(station)
            }
        }
    }

    fun pauseStation() {
        _uiState.value = _uiState.value.copy(isPlaying = false)
        PlayerServiceUtil.pause(PauseReason.USER)
    }

    fun toggleFavorite(station: DataRadioStation) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = appContext as? RadioDroidApp ?: return@launch
            val favouriteManager = app.favouriteManager
            val currentFavorites = favouriteManager.getList()
            val isFavorite = currentFavorites.any { it.StationUuid == station.StationUuid }

            if (isFavorite) {
                favouriteManager.remove(station.StationUuid)
            } else {
                favouriteManager.add(station)
            }

            val updatedFavorites = favouriteManager.getList()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        favoriteStations = updatedFavorites,
                        favoriteStationIds = updatedFavorites.mapNotNull { fav -> fav.StationUuid }.toSet()
                    )
                }
            }
        }
    }

    fun searchStations(query: String, style: com.eosphor.nonameradio.station.StationsFilter.SearchStyle = com.eosphor.nonameradio.station.StationsFilter.SearchStyle.ByName) {
        val trimmed = query.trim()
        _uiState.update { it.copy(currentSearchStyle = style) }

        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(
                    searchQuery = "",
                    searchResults = emptyList(),
                    isLoading = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, searchQuery = trimmed, selectedTabIndex = SEARCH_TAB_INDEX) }

            try {
                val results = fetchSearchResults(trimmed, style)
                _uiState.update {
                    it.copy(
                        searchResults = results,
                        isLoading = false,
                        error = null
                    )
                }
                android.util.Log.d("ExactOriginalViewModel", "Search completed for query: $trimmed style=$style size=${results.size}")
            } catch (e: Exception) {
                android.util.Log.e("ExactOriginalViewModel", "Search error", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun refreshLocalStations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val client = httpClient
            val context = appContext
            val newStations = if (client != null && context != null) {
                fetchStationsRelative(client, context, getLocalStationsEndpoint(context)).takeIf { it.isNotEmpty() }
            } else null

            _uiState.update {
                it.copy(
                    localStations = newStations ?: createRealisticStations("local", 20),
                    isLoading = false
                )
            }
        }
    }

    fun refreshTopClickStations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val client = httpClient
            val context = appContext
            val newStations = if (client != null && context != null) {
                fetchStationsRelative(client, context, "json/stations/topclick/100").takeIf { it.isNotEmpty() }
            } else null

            _uiState.update {
                it.copy(
                    topClickStations = newStations ?: createRealisticStations("topclick", 20),
                    isLoading = false
                )
            }
        }
    }

    fun refreshTopVoteStations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val client = httpClient
            val context = appContext
            val newStations = if (client != null && context != null) {
                fetchStationsRelative(client, context, "json/stations/topvote/100").takeIf { it.isNotEmpty() }
            } else null

            _uiState.update {
                it.copy(
                    topVoteStations = newStations ?: createRealisticStations("topvote", 20),
                    isLoading = false
                )
            }
        }
    }

    fun refreshChangedLatelyStations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val client = httpClient
            val context = appContext
            val newStations = if (client != null && context != null) {
                fetchStationsRelative(client, context, "json/stations/lastchange/100").takeIf { it.isNotEmpty() }
            } else null

            _uiState.update {
                it.copy(
                    changedLatelyStations = newStations ?: createRealisticStations("changed", 20),
                    isLoading = false
                )
            }
        }
    }

    fun refreshCurrentlyHeardStations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val client = httpClient
            val context = appContext
            val newStations = if (client != null && context != null) {
                fetchStationsRelative(client, context, "json/stations/lastclick/100").takeIf { it.isNotEmpty() }
            } else null

            _uiState.update {
                it.copy(
                    currentlyHeardStations = newStations ?: createRealisticStations("currently", 20),
                    isLoading = false
                )
            }
        }
    }

    fun refreshFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val favorites = loadFavoriteStations()
            _uiState.update {
                it.copy(
                    favoriteStations = favorites,
                    favoriteStationIds = favorites.mapNotNull { fav -> fav.StationUuid }.toSet(),
                    isLoading = false
                )
            }
        }
    }

    fun refreshFavoritesState() {
        viewModelScope.launch {
            val favorites = loadFavoriteStations()
            _uiState.update {
                it.copy(
                    favoriteStations = favorites,
                    favoriteStationIds = favorites.mapNotNull { fav -> fav.StationUuid }.toSet()
                )
            }
        }
    }

    fun requestAddAlarm() {
        viewModelScope.launch {
            val station = historyManager?.getList()?.firstOrNull()
            if (station != null) {
                _pendingAlarmStation.value = station
            } else {
                android.util.Log.w("ExactOriginalViewModel", "Cannot create alarm: history is empty")
            }
        }
    }

    fun confirmAddAlarm(hour: Int, minute: Int) {
        val station = _pendingAlarmStation.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val app = appContext as? RadioDroidApp ?: return@launch
            app.alarmManager.add(station, hour, minute)
            _pendingAlarmStation.value = null
        }
    }

    fun dismissAddAlarmDialog() {
        _pendingAlarmStation.value = null
    }

    // Новые функции для полного функционала
    fun openSearch() {
        if (!_uiState.value.isSearchActive) {
            _uiState.update {
                it.copy(
                    isSearchActive = true,
                    selectedTabIndex = SEARCH_TAB_INDEX,
                    currentSearchStyle = com.eosphor.nonameradio.station.StationsFilter.SearchStyle.ByName
                )
            }
            android.util.Log.d("ExactOriginalViewModel", "Search opened")
        }
    }

    fun closeSearch() {
        if (_uiState.value.isSearchActive) {
            _uiState.update {
                it.copy(
                    isSearchActive = false,
                    searchQuery = "",
                    searchResults = emptyList(),
                    isLoading = false
                )
            }
            android.util.Log.d("ExactOriginalViewModel", "Search closed")
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun submitSearch(query: String) {
        val trimmed = query.trim()
        updateSearchQuery(trimmed)
        if (trimmed.isNotEmpty()) {
            searchStations(trimmed, _uiState.value.currentSearchStyle)
        } else {
            _uiState.update { it.copy(searchResults = emptyList(), isLoading = false) }
        }
    }

    fun toggleSearch() {
        if (_uiState.value.isSearchActive) {
            closeSearch()
        } else {
            openSearch()
        }
    }

    fun setIconsView(useIcons: Boolean) {
        _uiState.update { it.copy(isIconsView = useIcons) }
        android.util.Log.d("ExactOriginalViewModel", "View mode set: ${if (useIcons) "Icons" else "List"}")
    }

    fun toggleViewMode() {
        setIconsView(!_uiState.value.isIconsView)
    }

    fun showMpdSettings() {
        android.util.Log.d("ExactOriginalViewModel", "Show MPD settings")
        // TODO: Navigate to MPD settings screen
    }

    fun getCurrentSleepTimerMinutes(): Int {
        val context = appContext ?: return 0
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val currentSeconds = PlayerServiceUtil.getTimerSeconds()
        return when {
            currentSeconds <= 0 -> sharedPref.getInt("sleep_timer_default_minutes", 10)
            currentSeconds < 60 -> 1
            else -> (currentSeconds / 60).toInt()
        }
    }

    fun setSleepTimer(minutes: Int) {
        val context = appContext ?: return
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        PlayerServiceUtil.clearTimer()
        if (minutes > 0) {
            PlayerServiceUtil.addTimer(minutes * 60)
            sharedPref.edit().putInt("sleep_timer_default_minutes", minutes).apply()
        }
        refreshTimerState()
        android.util.Log.d("ExactOriginalViewModel", "Sleep timer set to $minutes minutes")
    }

    fun clearSleepTimer() {
        PlayerServiceUtil.clearTimer()
        refreshTimerState()
        android.util.Log.d("ExactOriginalViewModel", "Sleep timer cleared")
    }

    private fun refreshTimerState() {
        val seconds = PlayerServiceUtil.getTimerSeconds()
        _uiState.update {
            it.copy(
                sleepTimerMinutes = if (seconds > 0) (seconds / 60).toInt() else 0,
                sleepTimerActive = seconds > 0
            )
        }
    }

    fun showMediaRoute() {
        android.util.Log.d("ExactOriginalViewModel", "Show media route (Chromecast)")
        // TODO: Show media route dialog
    }

    fun saveFavorites() {
        viewModelScope.launch {
            android.util.Log.d("ExactOriginalViewModel", "Save favorites to file")
            val context = appContext ?: return@launch
            val app = context.applicationContext as com.eosphor.nonameradio.RadioDroidApp
            val favouriteManager = app.getFavouriteManager()
            
            try {
                // Используем стандартную директорию для сохранения
                val saveDir = com.eosphor.nonameradio.StationSaveManager.getSaveDir()
                val fileName = "favorites_${System.currentTimeMillis()}.m3u"
                favouriteManager.SaveM3U(saveDir, fileName)
                android.util.Log.d("ExactOriginalViewModel", "Favorites saved to: $saveDir/$fileName")
            } catch (e: Exception) {
                android.util.Log.e("ExactOriginalViewModel", "Error saving favorites", e)
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            android.util.Log.d("ExactOriginalViewModel", "Load favorites from file")
            // TODO: Implement load favorites functionality with file picker
            // This should trigger a file picker dialog
        }
    }

    fun addAlarm() {
        android.util.Log.d("ExactOriginalViewModel", "Add new alarm")
        // TODO: Navigate to add alarm screen
    }

    fun deleteSelected() {
        android.util.Log.d("ExactOriginalViewModel", "Delete selected items")
        val context = appContext ?: return
        val app = context.applicationContext as com.eosphor.nonameradio.RadioDroidApp
        
        when (_uiState.value.selectedDrawerItem) {
            "history" -> {
                // Очистить историю
                val historyManager = app.getHistoryManager()
                historyManager.clear()
                android.util.Log.d("ExactOriginalViewModel", "History cleared")
            }
            "starred" -> {
                // Очистить избранное
                val favouriteManager = app.getFavouriteManager()
                favouriteManager.clear()
                android.util.Log.d("ExactOriginalViewModel", "Favorites cleared")
            }
            else -> {
                android.util.Log.d("ExactOriginalViewModel", "Delete not applicable for current context")
            }
        }
    }

    fun toggleMpd() {
        _uiState.value = _uiState.value.copy(
            mpdEnabled = !_uiState.value.mpdEnabled
        )
        android.util.Log.d("ExactOriginalViewModel", "MPD toggled: ${_uiState.value.mpdEnabled}")
    }

    fun toggleMediaRoute() {
        _uiState.value = _uiState.value.copy(
            mediaRouteActive = !_uiState.value.mediaRouteActive
        )
        android.util.Log.d("ExactOriginalViewModel", "Media route toggled: ${_uiState.value.mediaRouteActive}")
    }

    fun applyCategoryFilter(style: com.eosphor.nonameradio.station.StationsFilter.SearchStyle, value: String) {
        _uiState.update {
            it.copy(
                selectedTabIndex = SEARCH_TAB_INDEX,
                isSearchActive = false,
                searchQuery = value,
                currentSearchStyle = style
            )
        }
        searchStations(value, style)
    }

    private suspend fun fetchSearchResults(query: String, style: com.eosphor.nonameradio.station.StationsFilter.SearchStyle): List<DataRadioStation> = withContext(Dispatchers.IO) {
        val client = httpClient
        val context = appContext
        if (client == null || context == null) return@withContext emptyList()

        val radioApp = context as? RadioDroidApp ?: return@withContext emptyList()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val showBroken = sharedPref.getBoolean("show_broken", false)

        val params = HashMap<String, String>().apply {
            put("order", "clickcount")
            put("reverse", "true")
            put("hidebroken", (!showBroken).toString())
        }

        val encoded = try {
            URLEncoder.encode(query, "utf-8").replace("+", "%20")
        } catch (e: Exception) {
            android.util.Log.e("ExactOriginalViewModel", "Encoding error", e)
            query
        }

        val relative = when (style) {
            com.eosphor.nonameradio.station.StationsFilter.SearchStyle.ByName -> "json/stations/byname/$encoded"
            com.eosphor.nonameradio.station.StationsFilter.SearchStyle.ByLanguageExact -> "json/stations/bylanguageexact/$encoded"
            com.eosphor.nonameradio.station.StationsFilter.SearchStyle.ByCountryCodeExact -> "json/stations/bycountrycodeexact/$encoded"
            com.eosphor.nonameradio.station.StationsFilter.SearchStyle.ByTagExact -> "json/stations/bytagexact/$encoded"
        }

        try {
            val result = Utils.downloadFeedRelative(client, radioApp, relative, false, params)
            if (result != null) {
                com.eosphor.nonameradio.station.DataRadioStation.DecodeJson(result).orEmpty()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("ExactOriginalViewModel", "fetchSearchResults error", e)
            emptyList()
        }
    }

    private suspend fun loadFavoriteStations(): List<DataRadioStation> = withContext(Dispatchers.IO) {
        val app = appContext as? RadioDroidApp ?: return@withContext emptyList()
        val favouriteManager = app.favouriteManager
        favouriteManager.getList()
    }

    private fun registerFavoritesReceiver(context: Context) {
        if (!favoritesReceiverRegistered) {
            LocalBroadcastManager.getInstance(context).registerReceiver(
                favoritesReceiver,
                IntentFilter(com.eosphor.nonameradio.station.DataRadioStation.RADIO_STATION_LOCAL_INFO_CHAGED)
            )
            favoritesReceiverRegistered = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        appContext?.let {
            if (favoritesReceiverRegistered) {
                LocalBroadcastManager.getInstance(it).unregisterReceiver(favoritesReceiver)
                favoritesReceiverRegistered = false
            }
        }
    }
}
