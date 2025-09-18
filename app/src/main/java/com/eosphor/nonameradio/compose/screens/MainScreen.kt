package com.eosphor.nonameradio.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eosphor.nonameradio.compose.components.*
import com.eosphor.nonameradio.compose.theme.RadioDroidTheme
import com.eosphor.nonameradio.compose.viewmodels.MainScreenViewModel
import com.eosphor.nonameradio.station.DataRadioStation

data class TabItem(
    val title: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel(),
    onNavigateToStationList: () -> Unit = {},
    onNavigateToStations: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedDrawerItem by remember { mutableStateOf("all_stations") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    
    // Состояние UI из ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    // Табы как в оригинальном приложении
    val tabs = listOf(
        TabItem(
            title = "Все станции",
            icon = Icons.Default.Home,
            content = { 
                AllStationsContent(
                    stations = uiState.allStations,
                    isLoading = uiState.isLoading,
                    onPlayClick = { station -> viewModel.playStation(station) },
                    onFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onItemClick = { station -> viewModel.selectStation(station) }
                )
            }
        ),
        TabItem(
            title = "Избранное",
            icon = Icons.Default.Favorite,
            content = { 
                FavoritesContent(
                    stations = uiState.favoriteStations,
                    isLoading = uiState.isLoading,
                    onPlayClick = { station -> viewModel.playStation(station) },
                    onFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onItemClick = { station -> viewModel.selectStation(station) }
                )
            }
        ),
        TabItem(
            title = "История",
            icon = Icons.Default.Settings,
            content = { 
                HistoryContent(
                    stations = uiState.historyStations,
                    isLoading = uiState.isLoading,
                    onPlayClick = { station -> viewModel.playStation(station) },
                    onFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onItemClick = { station -> viewModel.selectStation(station) }
                )
            }
        ),
        TabItem(
            title = "Поиск",
            icon = Icons.Default.Search,
            content = { 
                SearchContent(
                    searchQuery = uiState.searchQuery,
                    searchResults = uiState.searchResults,
                    isSearching = uiState.isSearching,
                    onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
                    onPlayClick = { station -> viewModel.playStation(station) },
                    onFavoriteClick = { station -> viewModel.toggleFavorite(station) },
                    onItemClick = { station -> viewModel.selectStation(station) }
                )
            }
        )
    )

    RadioDroidTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                NavigationDrawer(
                    navigationItems = getDefaultNavigationItems(
                        selectedItem = selectedDrawerItem,
                        onNavigate = { item: String ->
                            selectedDrawerItem = item
                            viewModel.navigateToSection(item)
                            
                            // Handle navigation to history
                            if (item == "history") {
                                onNavigateToHistory()
                            }
                        }
                    ),
                    onItemClick = { item ->
                        item.onClick()
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                text = "RadioDroid",
                                style = MaterialTheme.typography.headlineSmall
                            ) 
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    // Открытие drawer через coroutine scope
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = onNavigateToStationList
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "Список станций"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                },
                bottomBar = {
                    Column {
                        // Плеер внизу экрана
                        uiState.currentStation?.let { station ->
                            PlayerBottomSheet(
                                playerState = PlayerState(
                                    stationName = station.Name ?: "Unknown",
                                    stationCountry = station.Country ?: "",
                                    stationFavicon = station.IconUrl,
                                    isPlaying = uiState.isPlaying,
                                    isFavorite = false, // TODO: Get from favorites
                                    volume = 1f,
                                    bitrate = station.Bitrate?.toString() ?: "",
                                    codec = station.Codec ?: ""
                                ),
                                onPlayPauseClick = { viewModel.togglePlayPause() },
                                onFavoriteClick = { viewModel.toggleFavorite(station) },
                                onVolumeChange = { /* TODO: Implement volume control */ },
                                onExpandClick = { /* TODO: Implement expand */ }
                            )
                        }
                        
                        // Нижняя навигация
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                        indicatorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onNavigateToStations,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = "Browse Stations"
                        )
                    }
                }
            ) { paddingValues ->
                // Контент выбранного таба
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    tabs[selectedTabIndex].content()
                }
            }
        }
    }
}

private fun getDefaultNavigationItems(
    selectedItem: String,
    onNavigate: (String) -> Unit
): List<NavigationItem> {
    return listOf(
        NavigationItem(
            title = "Все станции",
            icon = Icons.Default.Home,
            isSelected = selectedItem == "all_stations",
            onClick = { onNavigate("all_stations") }
        ),
        NavigationItem(
            title = "Избранное",
            icon = Icons.Default.Favorite,
            isSelected = selectedItem == "favorites",
            onClick = { onNavigate("favorites") }
        ),
        NavigationItem(
            title = "История",
            icon = Icons.Default.History,
            isSelected = selectedItem == "history",
            onClick = { onNavigate("history") }
        ),
        NavigationItem(
            title = "Поиск",
            icon = Icons.Default.Search,
            isSelected = selectedItem == "search",
            onClick = { onNavigate("search") }
        ),
        NavigationItem(
            title = "Настройки",
            icon = Icons.Default.Settings,
            isSelected = selectedItem == "settings",
            onClick = { onNavigate("settings") }
        ),
        NavigationItem(
            title = "О программе",
            icon = Icons.Default.Info,
            isSelected = selectedItem == "about",
            onClick = { onNavigate("about") }
        )
    )
}

@Composable
private fun AllStationsContent(
    stations: List<DataRadioStation>,
    isLoading: Boolean,
    onPlayClick: (DataRadioStation) -> Unit,
    onFavoriteClick: (DataRadioStation) -> Unit,
    onItemClick: (DataRadioStation) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(stations) { station ->
                RadioStationItem(
                    station = station,
                    onPlayClick = { onPlayClick(station) },
                    onFavoriteClick = { onFavoriteClick(station) },
                    onClick = { onItemClick(station) }
                )
            }
        }
    }
}

@Composable
private fun FavoritesContent(
    stations: List<DataRadioStation>,
    isLoading: Boolean,
    onPlayClick: (DataRadioStation) -> Unit,
    onFavoriteClick: (DataRadioStation) -> Unit,
    onItemClick: (DataRadioStation) -> Unit
) {
    if (stations.isEmpty() && !isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Нет избранных станций",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        AllStationsContent(stations, isLoading, onPlayClick, onFavoriteClick, onItemClick)
    }
}

@Composable
private fun HistoryContent(
    stations: List<DataRadioStation>,
    isLoading: Boolean,
    onPlayClick: (DataRadioStation) -> Unit,
    onFavoriteClick: (DataRadioStation) -> Unit,
    onItemClick: (DataRadioStation) -> Unit
) {
    if (stations.isEmpty() && !isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "История пуста",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        AllStationsContent(stations, isLoading, onPlayClick, onFavoriteClick, onItemClick)
    }
}

@Composable
private fun SearchContent(
    searchQuery: String,
    searchResults: List<DataRadioStation>,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onPlayClick: (DataRadioStation) -> Unit,
    onFavoriteClick: (DataRadioStation) -> Unit,
    onItemClick: (DataRadioStation) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Поле поиска
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Поиск станций") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )
        
        // Результаты поиска
        if (isSearching) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ничего не найдено",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else if (searchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(searchResults) { station ->
                    RadioStationItem(
                        station = station,
                        onPlayClick = { onPlayClick(station) },
                        onFavoriteClick = { onFavoriteClick(station) },
                        onClick = { onItemClick(station) }
                    )
                }
            }
        }
    }
}

