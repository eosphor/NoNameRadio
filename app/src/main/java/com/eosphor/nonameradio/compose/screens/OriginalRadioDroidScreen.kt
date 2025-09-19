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
import com.eosphor.nonameradio.R
import com.eosphor.nonameradio.compose.components.*
import com.eosphor.nonameradio.compose.viewmodels.OriginalRadioDroidViewModel
import com.eosphor.nonameradio.station.DataRadioStation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriginalRadioDroidScreen(
    modifier: Modifier = Modifier,
    viewModel: OriginalRadioDroidViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToStationList: () -> Unit = {},
    onNavigateToStations: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToRecordings: () -> Unit = {},
    onNavigateToCategories: (String) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedDrawerItem by remember { mutableStateOf("stations") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Состояние UI из ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Инициализация HistoryManager
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val app = context.applicationContext as com.eosphor.nonameradio.RadioDroidApp
        viewModel.initializeHistoryManager(app.getHistoryManager())
    }

    // Табы точно как в оригинальном RadioDroid (9 табов)
    val tabs = listOf(
        OriginalTabItem(
            title = "Local",
            icon = Icons.Default.Home,
            content = {
                OriginalStationsScreen(
                    stations = uiState.localStations,
                    isLoading = uiState.isLoading,
                    onStationClick = { station -> viewModel.playStation(station) },
                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onRefresh = { viewModel.refreshLocalStations() }
                )
            }
        ),
        OriginalTabItem(
            title = "Top Click",
            icon = Icons.Default.TrendingUp,
            content = {
                OriginalStationsScreen(
                    stations = uiState.topClickStations,
                    isLoading = uiState.isLoading,
                    onStationClick = { station -> viewModel.playStation(station) },
                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onRefresh = { viewModel.refreshTopClickStations() }
                )
            }
        ),
        OriginalTabItem(
            title = "Top Vote",
            icon = Icons.Default.ThumbUp,
            content = {
                OriginalStationsScreen(
                    stations = uiState.topVoteStations,
                    isLoading = uiState.isLoading,
                    onStationClick = { station -> viewModel.playStation(station) },
                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onRefresh = { viewModel.refreshTopVoteStations() }
                )
            }
        ),
        OriginalTabItem(
            title = "Changed Lately",
            icon = Icons.Default.Update,
            content = {
                OriginalStationsScreen(
                    stations = uiState.changedLatelyStations,
                    isLoading = uiState.isLoading,
                    onStationClick = { station -> viewModel.playStation(station) },
                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onRefresh = { viewModel.refreshChangedLatelyStations() }
                )
            }
        ),
        OriginalTabItem(
            title = "Currently Heard",
            icon = Icons.Default.PlayArrow,
            content = {
                OriginalStationsScreen(
                    stations = uiState.currentlyHeardStations,
                    isLoading = uiState.isLoading,
                    onStationClick = { station -> viewModel.playStation(station) },
                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onRefresh = { viewModel.refreshCurrentlyHeardStations() }
                )
            }
        ),
        OriginalTabItem(
            title = "Tags",
            icon = Icons.Default.Tag,
            content = {
                OriginalCategoriesScreen(
                    categories = uiState.tagCategories,
                    onCategoryClick = { tag -> onNavigateToCategories("tag/$tag") }
                )
            }
        ),
        OriginalTabItem(
            title = "Countries",
            icon = Icons.Default.Public,
            content = {
                OriginalCategoriesScreen(
                    categories = uiState.countryCategories,
                    onCategoryClick = { country -> onNavigateToCategories("country/$country") }
                )
            }
        ),
        OriginalTabItem(
            title = "Languages",
            icon = Icons.Default.Language,
            content = {
                OriginalCategoriesScreen(
                    categories = uiState.languageCategories,
                    onCategoryClick = { language -> onNavigateToCategories("language/$language") }
                )
            }
        ),
        OriginalTabItem(
            title = "Search",
            icon = Icons.Default.Search,
            content = {
                OriginalSearchScreen(
                    onSearch = { query -> viewModel.searchStations(query) },
                    searchResults = uiState.searchResults,
                    onStationClick = { station -> viewModel.playStation(station) },
                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) }
                )
            }
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            OriginalNavigationDrawer(
                selectedItem = selectedDrawerItem,
                onItemSelected = { item ->
                    selectedDrawerItem = item
                    when (item) {
                        "stations" -> onNavigateToStations()
                        "starred" -> onNavigateToStationList()
                        "history" -> onNavigateToHistory()
                        "alarm" -> { /* TODO: Navigate to alarm */ }
                        "settings" -> { /* TODO: Navigate to settings */ }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedDrawerItem) {
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
                        IconButton(onClick = { /* Drawer opens via ModalNavigationDrawer */ }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { /* TODO: Search */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { /* TODO: More options */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                    }
                )
            },
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Bottom player shown when there is a current station
                    OriginalBottomPlayer(
                        currentStation = uiState.currentStation,
                        isPlaying = uiState.isPlaying,
                        onPlayPauseClick = {
                            if (uiState.isPlaying) {
                                viewModel.pauseStation()
                            } else {
                                uiState.currentStation?.let { station -> viewModel.playStation(station) }
                            }
                        },
                        onExpandClick = { /* TODO: Expand to full player */ },
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    // Bottom Navigation (как в оригинале)
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedDrawerItem == "stations",
                            onClick = { selectedDrawerItem = "stations" },
                            icon = { Icon(Icons.Default.List, contentDescription = null) },
                            label = { Text("Stations") }
                        )
                        NavigationBarItem(
                            selected = selectedDrawerItem == "history",
                            onClick = { selectedDrawerItem = "history" },
                            icon = { Icon(Icons.Default.History, contentDescription = null) },
                            label = { Text("History") }
                        )
                        NavigationBarItem(
                            selected = selectedDrawerItem == "recordings",
                            onClick = { onNavigateToRecordings() },
                            icon = { Icon(Icons.Default.Mic, contentDescription = null) },
                            label = { Text("Recordings") }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { /* Open drawer categories or navigate to tags by default */ selectedDrawerItem = "stations" },
                            icon = { Icon(Icons.Default.Tag, contentDescription = null) },
                            label = { Text("Tags") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (selectedDrawerItem == "stations") {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                text = { Text(tab.title) }
                            )
                        }
                    }

                    tabs[selectedTabIndex].content()
                } else {
                    when (selectedDrawerItem) {
                        "starred" -> {
                            OriginalStationsScreen(
                                stations = uiState.favoriteStations,
                                isLoading = uiState.isLoading,
                                onStationClick = { station -> viewModel.playStation(station) },
                                onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                onRefresh = { viewModel.refreshFavorites() }
                            )
                        }
                        "history" -> {
                            OriginalHistoryScreen(
                                stations = uiState.historyStations,
                                onStationClick = { station -> viewModel.playStation(station) },
                                onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) }
                            )
                        }
                        "alarm" -> {
                            OriginalAlarmScreen()
                        }
                        "settings" -> {
                            OriginalSettingsScreen()
                        }
                        else -> {
                            // Default content
                            OriginalStationsScreen(
                                stations = uiState.localStations,
                                isLoading = uiState.isLoading,
                                onStationClick = { station -> viewModel.playStation(station) },
                                onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                                onRefresh = { viewModel.refreshLocalStations() }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class OriginalTabItem(
    val title: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)
