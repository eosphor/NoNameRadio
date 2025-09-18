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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eosphor.nonameradio.R
import com.eosphor.nonameradio.compose.components.*
import com.eosphor.nonameradio.compose.viewmodels.LegacyMainScreenViewModel
import com.eosphor.nonameradio.station.DataRadioStation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegacyMainScreen(
    modifier: Modifier = Modifier,
    viewModel: LegacyMainScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
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

    // Табы как в оригинальном приложении
    val tabs = listOf(
        LegacyTabItem(
            title = "Локальные",
            icon = Icons.Default.Home,
            content = {
                LegacyStationsScreen(
                    stations = uiState.localStations,
                    isLoading = uiState.isLoading,
                    onStationClick = { station -> viewModel.playStation(station) },
                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onRefresh = { viewModel.refreshLocalStations() }
                )
            }
        ),
        LegacyTabItem(
            title = "Топ клики",
            icon = Icons.Default.TrendingUp,
            content = {
                LegacyStationsScreen(
                    stations = uiState.topClickStations,
                    isLoading = uiState.isLoading,
                    onStationClick = { station -> viewModel.playStation(station) },
                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onRefresh = { viewModel.refreshTopClickStations() }
                )
            }
        ),
        LegacyTabItem(
            title = "Топ голоса",
            icon = Icons.Default.ThumbUp,
            content = {
                LegacyStationsScreen(
                    stations = uiState.topVoteStations,
                    isLoading = uiState.isLoading,
                    onStationClick = { station -> viewModel.playStation(station) },
                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onRefresh = { viewModel.refreshTopVoteStations() }
                )
            }
        ),
        LegacyTabItem(
            title = "Недавние",
            icon = Icons.Default.Update,
            content = {
                LegacyStationsScreen(
                    stations = uiState.recentStations,
                    isLoading = uiState.isLoading,
                    onStationClick = { station -> viewModel.playStation(station) },
                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onRefresh = { viewModel.refreshRecentStations() }
                )
            }
        ),
        LegacyTabItem(
            title = "Сейчас слушают",
            icon = Icons.Default.PlayArrow,
            content = {
                LegacyStationsScreen(
                    stations = uiState.currentlyHeardStations,
                    isLoading = uiState.isLoading,
                    onStationClick = { station -> viewModel.playStation(station) },
                    onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onRefresh = { viewModel.refreshCurrentlyHeardStations() }
                )
            }
        ),
        LegacyTabItem(
            title = "Теги",
            icon = Icons.Default.Tag,
            content = {
                LegacyCategoriesScreen(
                    categories = uiState.tagCategories,
                    onCategoryClick = { tag -> onNavigateToCategories("tag/$tag") }
                )
            }
        ),
        LegacyTabItem(
            title = "Страны",
            icon = Icons.Default.Public,
            content = {
                LegacyCategoriesScreen(
                    categories = uiState.countryCategories,
                    onCategoryClick = { country -> onNavigateToCategories("country/$country") }
                )
            }
        ),
        LegacyTabItem(
            title = "Языки",
            icon = Icons.Default.Language,
            content = {
                LegacyCategoriesScreen(
                    categories = uiState.languageCategories,
                    onCategoryClick = { language -> onNavigateToCategories("language/$language") }
                )
            }
        ),
        LegacyTabItem(
            title = "Поиск",
            icon = Icons.Default.Search,
            content = {
                LegacySearchScreen(
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
            LegacyNavigationDrawer(
                selectedItem = selectedDrawerItem,
                onItemSelected = { item ->
                    selectedDrawerItem = item
                    when (item) {
                        "stations" -> onNavigateToStations()
                        "starred" -> onNavigateToStationList()
                        "history" -> onNavigateToHistory()
                        "recordings" -> onNavigateToRecordings()
                        "settings" -> { /* TODO: Navigate to settings */ }
                    }
                }
            )
        }
    ) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // Top App Bar (как в старом приложении)
            TopAppBar(
                title = { 
                    Text(
                        text = when (selectedDrawerItem) {
                            "stations" -> "Станции"
                            "starred" -> "Избранные"
                            "history" -> "История"
                            "recordings" -> "Записи"
                            "settings" -> "Настройки"
                            else -> "NoNameRadio"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { 
                            // Drawer will be opened by ModalNavigationDrawer automatically
                        }
                    ) {
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

            // Tab Layout (как в старом приложении)
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

                // Tab Content
                tabs[selectedTabIndex].content()
            } else {
                // Content for other drawer items
                when (selectedDrawerItem) {
                    "starred" -> {
                        LegacyStationsScreen(
                            stations = uiState.favoriteStations,
                            isLoading = uiState.isLoading,
                            onStationClick = { station -> viewModel.playStation(station) },
                            onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                            onRefresh = { viewModel.refreshFavorites() }
                        )
                    }
                    "history" -> {
                        LegacyHistoryScreen(
                            stations = uiState.historyStations,
                            onStationClick = { station -> viewModel.playStation(station) },
                            onStationFavoriteClick = { station -> viewModel.toggleFavorite(station) }
                        )
                    }
                    "recordings" -> {
                        LegacyRecordingsScreen(
                            recordings = uiState.recordings,
                            onRecordingClick = { recording -> /* TODO: Play recording */ }
                        )
                    }
                    "settings" -> {
                        LegacySettingsScreen()
                    }
                }
            }
        }

        // Bottom Player (как BottomSheetBehavior в старом приложении)
        LegacyBottomPlayer(
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

data class LegacyTabItem(
    val title: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)
