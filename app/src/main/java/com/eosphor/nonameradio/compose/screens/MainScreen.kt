package com.eosphor.nonameradio.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.eosphor.nonameradio.compose.components.*
import com.eosphor.nonameradio.compose.theme.RadioDroidTheme

data class TabItem(
    val title: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedDrawerItem by remember { mutableStateOf("all_stations") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    
    // Пример данных для плеера
    val playerState by remember {
        mutableStateOf(
            PlayerState(
                stationName = "Radio Example",
                stationCountry = "Germany",
                stationFavicon = null,
                isPlaying = false,
                isFavorite = false,
                volume = 0.7f,
                bitrate = "128 kbps",
                codec = "MP3"
            )
        )
    }
    
    // Табы как в оригинальном приложении
    val tabs = listOf(
        TabItem(
            title = "Все станции",
            icon = Icons.Default.Home,
            content = { AllStationsContent() }
        ),
        TabItem(
            title = "Избранное",
            icon = Icons.Default.Favorite,
            content = { FavoritesContent() }
        ),
        TabItem(
            title = "История",
            icon = Icons.Default.Settings,
            content = { HistoryContent() }
        ),
        TabItem(
            title = "Поиск",
            icon = Icons.Default.Search,
            content = { SearchContent() }
        )
    )
    
    RadioDroidTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                NavigationDrawer(
                    navigationItems = getDefaultNavigationItems(
                        selectedItem = selectedDrawerItem,
                        onNavigate = { item ->
                            selectedDrawerItem = item
                            // Здесь можно добавить логику навигации
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
                                    // Открытие drawer
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
                                onClick = {
                                    // Поиск
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
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
                        PlayerBottomSheet(
                            playerState = playerState,
                            onPlayPauseClick = {
                                // Логика воспроизведения/паузы
                            },
                            onFavoriteClick = {
                                // Логика добавления в избранное
                            },
                            onVolumeChange = { volume ->
                                // Логика изменения громкости
                            },
                            onExpandClick = {
                                // Логика расширения плеера
                            }
                        )
                        
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

@Composable
private fun AllStationsContent() {
    // Пример списка станций
    val sampleStations = remember {
        listOf(
            RadioStation(
                name = "Radio Example 1",
                country = "Germany",
                language = "German",
                tags = "pop,rock,music",
                bitrate = 128,
                codec = "MP3",
                favicon = null,
                isFavorite = false,
                isPlaying = false
            ),
            RadioStation(
                name = "Radio Example 2",
                country = "France",
                language = "French",
                tags = "jazz,classical",
                bitrate = 192,
                codec = "AAC",
                favicon = null,
                isFavorite = true,
                isPlaying = false
            )
        )
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(sampleStations) { station ->
            RadioStationItem(
                station = station,
                onPlayClick = {
                    // Логика воспроизведения
                },
                onFavoriteClick = {
                    // Логика избранного
                },
                onItemClick = {
                    // Логика клика по элементу
                }
            )
        }
    }
}

@Composable
private fun FavoritesContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Избранные станции",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
private fun HistoryContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "История воспроизведения",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
private fun SearchContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Поиск станций",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}