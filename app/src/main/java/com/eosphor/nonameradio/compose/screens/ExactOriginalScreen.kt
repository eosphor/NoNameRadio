package com.eosphor.nonameradio.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import com.eosphor.nonameradio.R
import com.eosphor.nonameradio.compose.components.*
import com.eosphor.nonameradio.compose.viewmodels.ExactOriginalViewModel
import com.eosphor.nonameradio.station.DataRadioStation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExactOriginalScreen(
    modifier: Modifier = Modifier,
    viewModel: ExactOriginalViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToStationList: () -> Unit = {},
    onNavigateToStations: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToRecordings: () -> Unit = {},
    onNavigateToCategories: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Инициализация HistoryManager
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val app = context.applicationContext as com.eosphor.nonameradio.RadioDroidApp
        viewModel.initializeHistoryManager(app.getHistoryManager())
    }

    // Точная копия оригинального layout_main.xml
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // DrawerLayout (как в оригинале)
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                // NavigationView (точно как в оригинале)
                ExactOriginalNavigationDrawer(
                    selectedItem = uiState.selectedDrawerItem,
                    onItemSelected = { item ->
                        viewModel.navigateToSection(item)
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        ) {
            // CoordinatorLayout (как в оригинале)
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // AppBarLayout (как в оригинале)
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // CollapsingToolbarLayout (как в оригинале)
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Toolbar (точно как в оригинале с полным меню)
                            TopAppBar(
                                title = { 
                                    Text(
                                        text = when (uiState.selectedDrawerItem) {
                                            "stations" -> "Stations"
                                            "starred" -> "Starred"
                                            "history" -> "History"
                                            "alarm" -> "Alarm"
                                            "settings" -> "Settings"
                                            else -> "RadioDroid"
                                        }
                                    )
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { 
                                            scope.launch { drawerState.open() }
                                        }
                                    ) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                },
                                actions = {
                                    // Progress indicator (как в оригинале)
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    
                                    // Search (как в оригинале)
                                    IconButton(onClick = { viewModel.toggleSearch() }) {
                                        Icon(Icons.Default.Search, contentDescription = "Search")
                                    }
                                    
                                    // View toggle (Icons/List view)
                                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                                        Icon(
                                            if (uiState.isIconsView) Icons.Default.ViewList else Icons.Default.ViewModule,
                                            contentDescription = "Toggle View"
                                        )
                                    }
                                    
                                    // MPD support
                                    IconButton(onClick = { viewModel.showMpdSettings() }) {
                                        Icon(Icons.Default.SettingsInputComponent, contentDescription = "MPD")
                                    }
                                    
                                    // Sleep Timer
                                    IconButton(onClick = { viewModel.showSleepTimer() }) {
                                        Icon(Icons.Default.HourglassEmpty, contentDescription = "Sleep Timer")
                                    }
                                    
                                    // Media Route (Chromecast)
                                    IconButton(onClick = { viewModel.showMediaRoute() }) {
                                        Icon(Icons.Default.Cast, contentDescription = "Cast")
                                    }
                                    
                                    // Save favorites
                                    IconButton(onClick = { viewModel.saveFavorites() }) {
                                        Icon(Icons.Default.Save, contentDescription = "Save")
                                    }
                                    
                                    // Load favorites
                                    IconButton(onClick = { viewModel.loadFavorites() }) {
                                        Icon(Icons.Default.Upload, contentDescription = "Load")
                                    }
                                    
                                    // Add Alarm
                                    IconButton(onClick = { viewModel.addAlarm() }) {
                                        Icon(Icons.Default.AlarmAdd, contentDescription = "Add Alarm")
                                    }
                                    
                                    // Delete (context dependent)
                                    if (uiState.showDeleteButton) {
                                        IconButton(onClick = { viewModel.deleteSelected() }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                                        }
                                    }
                                }
                            )

                            // TabLayout (точно как в оригинале - только для stations)
                            if (uiState.selectedDrawerItem == "stations") {
                                ScrollableTabRow(
                                    selectedTabIndex = uiState.selectedTabIndex,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val tabs = listOf(
                                        "Local", "Top Click", "Top Vote", "Changed Lately", 
                                        "Playing", "Tags", "Countries", "Languages", "Search"
                                    )
                                    
                                    tabs.forEachIndexed { index, title ->
                                        Tab(
                                            selected = uiState.selectedTabIndex == index,
                                            onClick = { viewModel.selectTab(index) },
                                            text = { Text(title) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Container View (как в оригинале)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 56.dp) // actionBarSize для BottomSheet
                    ) {
                        when (uiState.selectedDrawerItem) {
                            "stations" -> {
                                // Tab Content (точно как в оригинале)
                                when (uiState.selectedTabIndex) {
                                    0 -> ExactOriginalStationsScreen(
                                        stations = uiState.localStations,
                                        isLoading = uiState.isLoading,
                                        onStationClick = { station -> viewModel.playStation(station) },
                                        onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                        onRefresh = { viewModel.refreshLocalStations() }
                                    )
                                    1 -> ExactOriginalStationsScreen(
                                        stations = uiState.topClickStations,
                                        isLoading = uiState.isLoading,
                                        onStationClick = { station -> viewModel.playStation(station) },
                                        onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                        onRefresh = { viewModel.refreshTopClickStations() }
                                    )
                                    2 -> ExactOriginalStationsScreen(
                                        stations = uiState.topVoteStations,
                                        isLoading = uiState.isLoading,
                                        onStationClick = { station -> viewModel.playStation(station) },
                                        onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                        onRefresh = { viewModel.refreshTopVoteStations() }
                                    )
                                    3 -> ExactOriginalStationsScreen(
                                        stations = uiState.changedLatelyStations,
                                        isLoading = uiState.isLoading,
                                        onStationClick = { station -> viewModel.playStation(station) },
                                        onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                        onRefresh = { viewModel.refreshChangedLatelyStations() }
                                    )
                                    4 -> ExactOriginalStationsScreen(
                                        stations = uiState.currentlyHeardStations,
                                        isLoading = uiState.isLoading,
                                        onStationClick = { station -> viewModel.playStation(station) },
                                        onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                        onRefresh = { viewModel.refreshCurrentlyHeardStations() }
                                    )
                                    5 -> ExactOriginalCategoriesScreen(
                                        categories = uiState.tagCategories,
                                        onCategoryClick = { tag -> onNavigateToCategories("tag/$tag") }
                                    )
                                    6 -> ExactOriginalCategoriesScreen(
                                        categories = uiState.countryCategories,
                                        onCategoryClick = { country -> onNavigateToCategories("country/$country") }
                                    )
                                    7 -> ExactOriginalCategoriesScreen(
                                        categories = uiState.languageCategories,
                                        onCategoryClick = { language -> onNavigateToCategories("language/$language") }
                                    )
                                    8 -> ExactOriginalSearchScreen(
                                        onSearch = { query -> viewModel.searchStations(query) },
                                        searchResults = uiState.searchResults,
                                        onStationClick = { station -> viewModel.playStation(station) },
                                        onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) }
                                    )
                                }
                            }
                            "starred" -> {
                                ExactOriginalStationsScreen(
                                    stations = uiState.favoriteStations,
                                    isLoading = uiState.isLoading,
                                    onStationClick = { station -> viewModel.playStation(station) },
                                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                    onRefresh = { viewModel.refreshFavorites() }
                                )
                            }
                            "history" -> {
                                ExactOriginalHistoryScreen(
                                    stations = uiState.historyStations,
                                    onStationClick = { station -> viewModel.playStation(station) },
                                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) }
                                )
                            }
                            "alarm" -> {
                                ExactOriginalAlarmScreen()
                            }
                            "settings" -> {
                                ExactOriginalSettingsScreen()
                            }
                        }
                    }
                }

                // BottomSheetBehavior (точно как в оригинале)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(56.dp) // actionBarSize
                        .zIndex(10f) // translationZ="10dp"
                ) {
                    ExactOriginalBottomPlayer(
                        currentStation = uiState.currentStation,
                        isPlaying = uiState.isPlaying,
                        onPlayPauseClick = {
                            if (uiState.isPlaying) {
                                viewModel.pauseStation()
                            } else {
                                uiState.currentStation?.let { station -> viewModel.playStation(station) }
                            }
                        },
                        onExpandClick = { /* TODO: Expand to full player */ }
                    )
                }
            }
        }

        // BottomNavigationView (точно как в оригинале)
        NavigationBar(
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBarItem(
                selected = uiState.selectedDrawerItem == "stations",
                onClick = { viewModel.navigateToSection("stations") },
                icon = { Icon(Icons.Default.List, contentDescription = null) },
                label = { Text("Stations") }
            )
            NavigationBarItem(
                selected = uiState.selectedDrawerItem == "starred",
                onClick = { viewModel.navigateToSection("starred") },
                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                label = { Text("Starred") }
            )
            NavigationBarItem(
                selected = uiState.selectedDrawerItem == "history",
                onClick = { viewModel.navigateToSection("history") },
                icon = { Icon(Icons.Default.History, contentDescription = null) },
                label = { Text("History") }
            )
            NavigationBarItem(
                selected = uiState.selectedDrawerItem == "alarm",
                onClick = { viewModel.navigateToSection("alarm") },
                icon = { Icon(Icons.Default.Alarm, contentDescription = null) },
                label = { Text("Alarm") }
            )
            NavigationBarItem(
                selected = uiState.selectedDrawerItem == "settings",
                onClick = { viewModel.navigateToSection("settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text("Settings") }
            )
        }
    }
}
