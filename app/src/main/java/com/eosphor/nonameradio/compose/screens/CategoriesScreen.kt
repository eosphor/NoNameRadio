package com.eosphor.nonameradio.compose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eosphor.nonameradio.CountryFlagsLoader
import com.eosphor.nonameradio.compose.viewmodels.CategoriesScreenViewModel
import com.eosphor.nonameradio.data.DataCategory
import com.eosphor.nonameradio.station.StationsFilter
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriesScreenViewModel = viewModel(),
    searchStyle: StationsFilter.SearchStyle = StationsFilter.SearchStyle.ByTagExact,
    onNavigateBack: () -> Unit = {},
    onCategoryClick: (DataCategory) -> Unit = {},
    onInitializeViewModel: (CategoriesScreenViewModel) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isInitialized by remember { mutableStateOf(false) }

    // Initialize view model when screen is first created
    LaunchedEffect(Unit) {
        if (!isInitialized) {
            val countryCodeDictionary = com.eosphor.nonameradio.CountryCodeDictionary.getInstance()
            val countryFlagsLoader = CountryFlagsLoader.getInstance()
            
            viewModel.initialize(
                searchStyle = searchStyle,
                singleUseFilter = false,
                countryCodeDictionary = countryCodeDictionary,
                countryFlagsLoader = countryFlagsLoader
            )
            onInitializeViewModel(viewModel)
            isInitialized = true
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = viewModel::refreshCategories
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.getSearchStyleTitle()) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Toggle single use tags filter
                    if (searchStyle == StationsFilter.SearchStyle.ByTagExact) {
                        IconButton(onClick = viewModel::toggleSingleUseTags) {
                            Icon(
                                imageVector = if (uiState.showSingleUseTags) Icons.Default.FilterList else Icons.Default.FilterListOff,
                                contentDescription = "Toggle single use tags"
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.hasError -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::clearError) {
                            Text("Повторить")
                        }
                    }
                }
                
                uiState.filteredCategories.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            when (searchStyle) {
                                StationsFilter.SearchStyle.ByTagExact -> Icons.Default.Label
                                StationsFilter.SearchStyle.ByLanguageExact -> Icons.Default.Language
                                StationsFilter.SearchStyle.ByCountryCodeExact -> Icons.Default.Public
                                else -> Icons.Default.Category
                            },
                            contentDescription = "No categories",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = when {
                                uiState.searchQuery.isNotEmpty() -> "Категории не найдены"
                                else -> "Категории отсутствуют"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.searchQuery.isNotEmpty()) {
                            Text(
                                text = "Попробуйте изменить поисковый запрос",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = uiState.filteredCategories,
                            key = { category -> category.Name }
                        ) { category ->
                            CategoryItem(
                                category = category,
                                searchStyle = searchStyle,
                                onClick = { onCategoryClick(category) }
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun CategoryItem(
    category: DataCategory,
    searchStyle: StationsFilter.SearchStyle,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val countryFlagsLoader = CountryFlagsLoader.getInstance()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on search style
            when (searchStyle) {
                StationsFilter.SearchStyle.ByCountryCodeExact -> {
                    // Show country flag
                    val flagDrawable = countryFlagsLoader.getFlag(context, category.Name)
                    if (flagDrawable != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(flagDrawable)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Country flag",
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = "Country",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                StationsFilter.SearchStyle.ByLanguageExact -> {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = "Language",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                StationsFilter.SearchStyle.ByTagExact -> {
                    Icon(
                        Icons.Default.Label,
                        contentDescription = "Tag",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                else -> {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = "Category",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.Label ?: category.Name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = "${category.UsedCount} станций",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
