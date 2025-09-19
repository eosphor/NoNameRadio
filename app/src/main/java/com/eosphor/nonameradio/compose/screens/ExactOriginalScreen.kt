@file:OptIn(ExperimentalMaterial3Api::class)

package com.eosphor.nonameradio.compose.screens

import androidx.compose.foundation.background
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import java.util.Calendar
import com.eosphor.nonameradio.R
import com.eosphor.nonameradio.compose.components.*
import com.eosphor.nonameradio.compose.viewmodels.ExactOriginalUiState
import com.eosphor.nonameradio.compose.viewmodels.ExactOriginalViewModel
import com.eosphor.nonameradio.station.DataRadioStation
import com.eosphor.nonameradio.station.StationsFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExactOriginalScreen(
    modifier: Modifier = Modifier,
    viewModel: ExactOriginalViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToStationList: () -> Unit = {},
    onNavigateToStations: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToRecordings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val pendingAlarmStation by viewModel.pendingAlarmStation.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var sleepTimerDialogVisible by rememberSaveable { mutableStateOf(false) }
    var sleepTimerMinutes by remember { mutableStateOf(0) }

    // Инициализация HistoryManager
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val app = context.applicationContext as com.eosphor.nonameradio.RadioDroidApp
        viewModel.initializeHistoryManager(app.getHistoryManager())
        viewModel.initializeNetwork(app.getHttpClient(), context)
    }

    LaunchedEffect(sleepTimerDialogVisible) {
        if (sleepTimerDialogVisible) {
            sleepTimerMinutes = viewModel.getCurrentSleepTimerMinutes().coerceIn(0, 240)
        }
    }

    LaunchedEffect(uiState.sleepTimerMinutes) {
        if (!sleepTimerDialogVisible) {
            sleepTimerMinutes = uiState.sleepTimerMinutes
        }
    }

    LaunchedEffect(pendingAlarmStation) {
        val station = pendingAlarmStation ?: return@LaunchedEffect
        val calendar = Calendar.getInstance()
        val dialog = TimePickerDialog(
            context,
            { _, hour, minute -> viewModel.confirmAddAlarm(hour, minute) },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        dialog.setOnDismissListener { viewModel.dismissAddAlarmDialog() }
        dialog.setTitle(station.Name ?: context.getString(R.string.settings_alarm))
        dialog.show()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ExactOriginalNavigationDrawer(
                selectedItem = uiState.selectedDrawerItem,
                onItemSelected = { item ->
                    viewModel.navigateToSection(item)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                ExactOriginalAppBar(
                    uiState = uiState,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSearchClick = { viewModel.openSearch() },
                    onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
                    onSearchSubmit = { query -> viewModel.submitSearch(query) },
                    onSearchClose = { viewModel.closeSearch() },
                    onToggleToIcons = { viewModel.setIconsView(true) },
                    onToggleToList = { viewModel.setIconsView(false) },
                    onShowMpd = { viewModel.showMpdSettings() },
                    onShowSleepTimer = { sleepTimerDialogVisible = true },
                    onShowMediaRoute = { viewModel.showMediaRoute() },
                    onSaveFavorites = { viewModel.saveFavorites() },
                    onLoadFavorites = { viewModel.loadFavorites() },
                    onAddAlarm = { viewModel.requestAddAlarm() },
                    onDelete = { viewModel.deleteSelected() },
                    onTabSelected = { index -> viewModel.selectTab(index) }
                )
            },
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ExactOriginalBottomPlayer(
                        currentStation = uiState.currentStation,
                        isPlaying = uiState.isPlaying,
                        onPlayPauseClick = {
                            if (uiState.isPlaying) {
                                viewModel.pauseStation()
                            } else {
                                uiState.currentStation?.let { station ->
                                    viewModel.playStation(station)
                                }
                            }
                        },
                        onExpandClick = { /* TODO: Expand to full player */ }
                    )

                    if (!uiState.isSearchActive) {
                        NavigationBar(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            ExactOriginalBottomNavigationItems(
                                selectedItem = uiState.selectedDrawerItem,
                                onSelect = { route -> viewModel.navigateToSection(route) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.selectedDrawerItem) {
                    "stations" -> {
                        val favoriteIds = uiState.favoriteStationIds
                        when (uiState.selectedTabIndex) {
                            0 -> ExactOriginalStationsScreen(
                                stations = uiState.localStations,
                                isLoading = uiState.isLoading,
                                onStationClick = { station -> viewModel.playStation(station) },
                                onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                onRefresh = { viewModel.refreshLocalStations() },
                                favoriteStationIds = favoriteIds,
                                onFavoritesChanged = { viewModel.refreshFavoritesState() }
                            )
                            1 -> ExactOriginalStationsScreen(
                                stations = uiState.topClickStations,
                                isLoading = uiState.isLoading,
                                onStationClick = { station -> viewModel.playStation(station) },
                                onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                onRefresh = { viewModel.refreshTopClickStations() },
                                favoriteStationIds = favoriteIds,
                                onFavoritesChanged = { viewModel.refreshFavoritesState() }
                            )
                            2 -> ExactOriginalStationsScreen(
                                stations = uiState.topVoteStations,
                                isLoading = uiState.isLoading,
                                onStationClick = { station -> viewModel.playStation(station) },
                                onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                onRefresh = { viewModel.refreshTopVoteStations() },
                                favoriteStationIds = favoriteIds,
                                onFavoritesChanged = { viewModel.refreshFavoritesState() }
                            )
                            3 -> ExactOriginalStationsScreen(
                                stations = uiState.changedLatelyStations,
                                isLoading = uiState.isLoading,
                                onStationClick = { station -> viewModel.playStation(station) },
                                onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                onRefresh = { viewModel.refreshChangedLatelyStations() },
                                favoriteStationIds = favoriteIds,
                                onFavoritesChanged = { viewModel.refreshFavoritesState() }
                            )
                            4 -> ExactOriginalStationsScreen(
                                stations = uiState.currentlyHeardStations,
                                isLoading = uiState.isLoading,
                                onStationClick = { station -> viewModel.playStation(station) },
                                onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                onRefresh = { viewModel.refreshCurrentlyHeardStations() },
                                favoriteStationIds = favoriteIds,
                                onFavoritesChanged = { viewModel.refreshFavoritesState() }
                            )
                            5 -> ExactOriginalCategoriesScreen(
                                categories = uiState.tagCategories,
                                onCategoryClick = { tag -> viewModel.applyCategoryFilter(StationsFilter.SearchStyle.ByTagExact, tag) }
                            )
                            6 -> ExactOriginalCategoriesScreen(
                                categories = uiState.countryCategories,
                                onCategoryClick = { country -> viewModel.applyCategoryFilter(StationsFilter.SearchStyle.ByCountryCodeExact, country) }
                            )
                            7 -> ExactOriginalCategoriesScreen(
                                categories = uiState.languageCategories,
                                onCategoryClick = { language -> viewModel.applyCategoryFilter(StationsFilter.SearchStyle.ByLanguageExact, language) }
                            )
                            8 -> ExactOriginalSearchScreen(
                                query = uiState.searchQuery,
                                onQueryChange = viewModel::updateSearchQuery,
                                onSearch = { query -> viewModel.searchStations(query, uiState.currentSearchStyle) },
                                searchResults = uiState.searchResults,
                                onStationClick = { station -> viewModel.playStation(station) },
                                onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                favoriteStationIds = favoriteIds,
                                onFavoritesChanged = { viewModel.refreshFavoritesState() }
                            )
                        }
                    }
                    "starred" -> {
                        ExactOriginalStationsScreen(
                            stations = uiState.favoriteStations,
                            isLoading = uiState.isLoading,
                            onStationClick = { station -> viewModel.playStation(station) },
                            onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                            onRefresh = { viewModel.refreshFavorites() },
                            favoriteStationIds = uiState.favoriteStationIds,
                            onFavoritesChanged = { viewModel.refreshFavoritesState() }
                        )
                    }
                    "history" -> {
                        ExactOriginalHistoryScreen(
                            stations = uiState.historyStations,
                            onStationClick = { station -> viewModel.playStation(station) },
                            onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                            favoriteStationIds = uiState.favoriteStationIds
                        )
                    }
                    "alarm" -> ExactOriginalAlarmScreen()
                    "settings" -> ExactOriginalSettingsScreen()
                }
            }
        }

        if (sleepTimerDialogVisible) {
            SleepTimerDialog(
                initialMinutes = sleepTimerMinutes,
                onDismiss = { sleepTimerDialogVisible = false },
                onConfirm = { minutes ->
                    viewModel.setSleepTimer(minutes)
                    sleepTimerDialogVisible = false
                },
                onClear = {
                    viewModel.clearSleepTimer()
                    sleepTimerDialogVisible = false
                }
            )
        }
    }
}

private val StationTabTitles = listOf(
    R.string.action_local,
    R.string.action_top_click,
    R.string.action_top_vote,
    R.string.action_changed_lately,
    R.string.action_currently_playing,
    R.string.action_tags,
    R.string.action_countries,
    R.string.action_languages,
    R.string.action_search
)

@Composable
private fun ExactOriginalAppBar(
    uiState: ExactOriginalUiState,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onSearchClose: () -> Unit,
    onToggleToIcons: () -> Unit,
    onToggleToList: () -> Unit,
    onShowMpd: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onShowMediaRoute: () -> Unit,
    onSaveFavorites: () -> Unit,
    onLoadFavorites: () -> Unit,
    onAddAlarm: () -> Unit,
    onDelete: () -> Unit,
    onTabSelected: (Int) -> Unit
) {
    val title = when (uiState.selectedDrawerItem) {
        "stations" -> stringResource(R.string.nav_item_stations)
        "starred" -> stringResource(R.string.nav_item_starred)
        "history" -> stringResource(R.string.nav_item_history)
        "alarm" -> stringResource(R.string.nav_item_alarm)
        "settings" -> stringResource(R.string.nav_item_settings)
        else -> stringResource(R.string.app_name)
    }

    val showSearch = uiState.selectedDrawerItem == "stations"
    val showSleepTimer = uiState.selectedDrawerItem == "stations" ||
        uiState.selectedDrawerItem == "starred" ||
        uiState.selectedDrawerItem == "history"
    val showSave = uiState.selectedDrawerItem == "starred" || uiState.selectedDrawerItem == "history"
    val showLoad = uiState.selectedDrawerItem == "starred"
    val showIconsToggle = uiState.selectedDrawerItem == "starred" && !uiState.isIconsView
    val showListToggle = uiState.selectedDrawerItem == "starred" && uiState.isIconsView
    val showDelete = when (uiState.selectedDrawerItem) {
        "starred" -> uiState.favoriteStations.isNotEmpty()
        "history" -> uiState.historyStations.isNotEmpty()
        else -> false
    }
    val showAddAlarm = uiState.selectedDrawerItem == "alarm"
    val showMpd = uiState.mpdEnabled
    val showCast = uiState.mediaRouteActive
    val saveLabelRes = if (uiState.selectedDrawerItem == "history") {
        R.string.nav_item_save_history_playlist
    } else {
        R.string.nav_item_save_playlist
    }
    val deleteLabelRes = when (uiState.selectedDrawerItem) {
        "starred" -> R.string.action_delete_favorites
        "history" -> R.string.action_delete_history
        else -> R.string.action_delete
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onMenuClick, content = {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = null)
                })
            },
            title = {
                if (uiState.isSearchActive) {
                    ExactOriginalSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onSearch = onSearchSubmit,
                        onClose = onSearchClose
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(34.dp)
                                    .padding(end = 12.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            actions = {
                if (uiState.isSearchActive) {
                    IconButton(onClick = onSearchClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(android.R.string.cancel)
                        )
                    }
                } else {
                    if (showSearch) {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search_24dp),
                                contentDescription = stringResource(R.string.action_search)
                            )
                        }
                    }
                    if (showIconsToggle) {
                        IconButton(onClick = onToggleToIcons) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_view_icons_24dp),
                                contentDescription = stringResource(R.string.action_toggle_view)
                            )
                        }
                    }
                    if (showListToggle) {
                        IconButton(onClick = onToggleToList) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_view_list_24dp),
                                contentDescription = stringResource(R.string.action_toggle_view)
                            )
                        }
                    }
                    if (showMpd) {
                        IconButton(onClick = onShowMpd) {
                            Icon(
                                painter = painterResource(id = R.drawable.mpd),
                                contentDescription = stringResource(R.string.settings_mpd)
                            )
                        }
                    }
                    if (showCast) {
                        IconButton(onClick = onShowMediaRoute) {
                            Icon(
                                painter = painterResource(id = androidx.mediarouter.R.drawable.mr_button_dark),
                                contentDescription = stringResource(R.string.media_route_menu_title)
                            )
                        }
                    }
                    if (showSleepTimer) {
                        IconButton(onClick = onShowSleepTimer) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_hourglass_empty_black_24dp),
                                contentDescription = stringResource(R.string.nav_item_add_sleep)
                            )
                        }
                    }
                    if (showSave) {
                        IconButton(onClick = onSaveFavorites) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_save_black_24dp),
                                contentDescription = stringResource(saveLabelRes)
                            )
                        }
                    }
                    if (showLoad) {
                        IconButton(onClick = onLoadFavorites) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_load_playlist_24dp),
                                contentDescription = stringResource(R.string.nav_item_load_playlist)
                            )
                        }
                    }
                    if (showAddAlarm) {
                        IconButton(onClick = onAddAlarm) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add_alarm_black_24dp),
                                contentDescription = stringResource(R.string.nav_item_alarm)
                            )
                        }
                    }
                    if (showDelete) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete_white_24dp),
                                contentDescription = stringResource(deleteLabelRes)
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        if (uiState.selectedDrawerItem == "stations" && !uiState.isSearchActive) {
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (uiState.selectedTabIndex in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTabIndex]),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                divider = {}
            ) {
                StationTabTitles.forEachIndexed { index, titleRes ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        onClick = { onTabSelected(index) },
                        text = {
                            Text(
                                text = stringResource(id = titleRes),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExactOriginalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClose: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onPrimary),
        placeholder = {
            Text(
                text = stringResource(R.string.action_search),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(android.R.string.cancel),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(android.R.string.cancel),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onPrimary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
        )
    )
}

@Composable
private fun RowScope.ExactOriginalBottomNavigationItems(
    selectedItem: String,
    onSelect: (String) -> Unit
) {
    NavigationBarItem(
        selected = selectedItem == "stations",
        onClick = { onSelect("stations") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_list_24dp),
                contentDescription = stringResource(R.string.nav_item_stations)
            )
        },
        label = { Text(text = stringResource(R.string.nav_item_stations)) },
        alwaysShowLabel = true
    )
    NavigationBarItem(
        selected = selectedItem == "starred",
        onClick = { onSelect("starred") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_star_black_24dp),
                contentDescription = stringResource(R.string.nav_item_starred)
            )
        },
        label = { Text(text = stringResource(R.string.nav_item_starred)) },
        alwaysShowLabel = true
    )
    NavigationBarItem(
        selected = selectedItem == "history",
        onClick = { onSelect("history") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_restore_black_24dp),
                contentDescription = stringResource(R.string.nav_item_history)
            )
        },
        label = { Text(text = stringResource(R.string.nav_item_history)) },
        alwaysShowLabel = true
    )
    NavigationBarItem(
        selected = selectedItem == "alarm",
        onClick = { onSelect("alarm") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_access_alarms_black_24dp),
                contentDescription = stringResource(R.string.nav_item_alarm)
            )
        },
        label = { Text(text = stringResource(R.string.nav_item_alarm)) },
        alwaysShowLabel = true
    )
    NavigationBarItem(
        selected = selectedItem == "settings",
        onClick = { onSelect("settings") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_tune_24dp),
                contentDescription = stringResource(R.string.nav_item_settings)
            )
        },
        label = { Text(text = stringResource(R.string.nav_item_settings)) },
        alwaysShowLabel = true
    )
}

@Composable
private fun SleepTimerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    onClear: () -> Unit
) {
    var sliderValue by remember(initialMinutes) { mutableStateOf(initialMinutes.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.sleep_timer_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..240f,
                    steps = 239
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.sleep_timer, sliderValue.toInt(), 0),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(sliderValue.toInt())
            }) {
                Text(text = stringResource(id = R.string.sleep_timer_apply))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    onClear()
                    onDismiss()
                }) {
                    Text(text = stringResource(id = R.string.sleep_timer_clear))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            }
        }
    )
}
