package com.eosphor.nonameradio.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eosphor.nonameradio.compose.components.*
import com.eosphor.nonameradio.compose.theme.RadioDroidTheme
import com.eosphor.nonameradio.compose.viewmodels.StationListViewModel
import com.eosphor.nonameradio.station.DataRadioStation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListScreen(
    modifier: Modifier = Modifier,
    viewModel: StationListViewModel = viewModel(),
    onStationClick: (DataRadioStation) -> Unit = {},
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    
    RadioDroidTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "Список станций",
                            style = MaterialTheme.typography.headlineSmall
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    },
                    actions = {
                        // Кнопка сортировки
                        IconButton(
                            onClick = { viewModel.toggleSortMenu() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        
                        // Кнопка фильтра
                        IconButton(
                            onClick = { viewModel.toggleFilterMenu() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter"
                            )
                        }
                        
                        // Кнопка поиска
                        IconButton(
                            onClick = { viewModel.toggleSearchMode() }
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
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Поле поиска (показывается при активации)
                if (uiState.isSearchMode) {
                    SearchBar(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onCloseSearch = { viewModel.toggleSearchMode() }
                    )
                }
                
                // Фильтры (показываются при активации)
                if (uiState.showFilters) {
                    FilterChips(
                        selectedCountry = uiState.selectedCountry,
                        selectedLanguage = uiState.selectedLanguage,
                        selectedCodec = uiState.selectedCodec,
                        onCountryChange = { viewModel.setCountryFilter(it) },
                        onLanguageChange = { viewModel.setLanguageFilter(it) },
                        onCodecChange = { viewModel.setCodecFilter(it) },
                        onClearFilters = { viewModel.clearFilters() }
                    )
                }
                
                // Информация о количестве станций
                StationCountInfo(
                    totalCount = uiState.totalStations,
                    filteredCount = uiState.filteredStations.size,
                    isFiltered = uiState.hasActiveFilters
                )
                
                // Список станций
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    uiState.error != null -> {
                        val errorMessage = uiState.error
                        LaunchedEffect(errorMessage) {
                            // Показываем Snackbar с ошибкой
                        }
                        ErrorMessage(
                            error = errorMessage!!,
                            onRetry = { viewModel.retryLoading() }
                        )
                    }
                    
                    uiState.filteredStations.isEmpty() -> {
                        EmptyStateMessage(
                            hasActiveFilters = uiState.hasActiveFilters,
                            onClearFilters = { viewModel.clearFilters() }
                        )
                    }
                    
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                items = uiState.filteredStations,
                                key = { station -> station.StationUuid ?: station.hashCode() }
                            ) { station ->
                                RadioStationItem(
                                    station = RadioStation(
                                        name = station.Name ?: "Unknown",
                                        country = station.Country ?: "",
                                        language = station.Language ?: "",
                                        tags = station.TagsAll ?: "",
                                        bitrate = station.Bitrate,
                                        codec = station.Codec ?: "",
                                        favicon = station.IconUrl,
                                        isFavorite = station.StationUuid in uiState.favoriteStationIds,
                                        isPlaying = station.StationUuid == uiState.currentPlayingStationId
                                    ),
                                    onPlayClick = { 
                                        viewModel.playStation(station)
                                        onStationClick(station)
                                    },
                                    onFavoriteClick = { 
                                        viewModel.toggleFavorite(station) 
                                    },
                                    onItemClick = { 
                                        onStationClick(station) 
                                    }
                                )
                            }
                            
                            // Индикатор загрузки дополнительных данных
                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Меню сортировки
        if (uiState.showSortMenu) {
            SortMenu(
                currentSortOption = uiState.sortOption,
                onSortOptionSelected = { option ->
                    viewModel.setSortOption(option)
                    viewModel.toggleSortMenu()
                },
                onDismiss = { viewModel.toggleSortMenu() }
            )
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
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
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear"
                    )
                }
            } else {
                IconButton(onClick = onCloseSearch) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        singleLine = true
    )
}

@Composable
private fun FilterChips(
    selectedCountry: String?,
    selectedLanguage: String?,
    selectedCodec: String?,
    onCountryChange: (String?) -> Unit,
    onLanguageChange: (String?) -> Unit,
    onCodecChange: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Фильтры",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onClearFilters) {
                    Text("Очистить")
                }
            }
        }
        
        item {
            // Фильтр по стране
            FilterChipRow(
                label = "Страна",
                selectedValue = selectedCountry,
                options = listOf("Germany", "France", "USA", "United Kingdom", "Switzerland"),
                onValueChange = onCountryChange
            )
        }
        
        item {
            // Фильтр по языку
            FilterChipRow(
                label = "Язык",
                selectedValue = selectedLanguage,
                options = listOf("English", "German", "French", "Spanish", "Italian"),
                onValueChange = onLanguageChange
            )
        }
        
        item {
            // Фильтр по кодеку
            FilterChipRow(
                label = "Кодек",
                selectedValue = selectedCodec,
                options = listOf("MP3", "AAC", "OGG", "FLAC"),
                onValueChange = onCodecChange
            )
        }
    }
}

@Composable
private fun FilterChipRow(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onValueChange: (String?) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        LazyColumn {
            items(options) { option ->
                FilterChip(
                    selected = selectedValue == option,
                    onClick = {
                        onValueChange(if (selectedValue == option) null else option)
                    },
                    label = { Text(option) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StationCountInfo(
    totalCount: Int,
    filteredCount: Int,
    isFiltered: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFiltered) {
                    "Показано $filteredCount из $totalCount станций"
                } else {
                    "Всего станций: $totalCount"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ошибка загрузки",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (hasActiveFilters) Icons.Default.FilterList else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasActiveFilters) {
                    "Нет станций по заданным фильтрам"
                } else {
                    "Нет доступных станций"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (hasActiveFilters) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onClearFilters) {
                    Text("Очистить фильтры")
                }
            }
        }
    }
}

@Composable
private fun SortMenu(
    currentSortOption: String,
    onSortOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sortOptions = listOf(
        "name" to "По названию",
        "country" to "По стране",
        "bitrate" to "По битрейту",
        "codec" to "По кодеку",
        "recent" to "Недавние"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сортировка") },
        text = {
            LazyColumn {
                items(sortOptions) { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSortOption == key,
                            onClick = { onSortOptionSelected(key) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}