package com.eosphor.nonameradio.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.saveable.rememberSaveable
import com.eosphor.nonameradio.R
import com.eosphor.nonameradio.compose.components.RadioStationItem
import com.eosphor.nonameradio.compose.viewmodels.HistoryScreenViewModel
import com.eosphor.nonameradio.station.DataRadioStation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryScreenViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onStationClick: (DataRadioStation) -> Unit = {},
    onInitializeHistoryManager: (com.eosphor.nonameradio.HistoryManager) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isInitialized by rememberSaveable { mutableStateOf(false) }

    // Initialize history manager when screen is first created
    LaunchedEffect(Unit) {
        if (!isInitialized) {
            // Get history manager from RadioDroidApp
            val radioDroidApp = context.applicationContext as com.eosphor.nonameradio.RadioDroidApp
            val historyManager = radioDroidApp.getHistoryManager()
            viewModel.initializeHistoryManager(historyManager)
            onInitializeHistoryManager(historyManager)
            isInitialized = true
        }
    }

    // Show undo snackbar when station is deleted
    LaunchedEffect(uiState.showUndoSnackbar) {
        if (uiState.showUndoSnackbar) {
            // Snackbar will be handled by parent composable
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::clearError) {
                            Text("Retry")
                        }
                    }
                }
                
                uiState.stations.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "История пуста",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = uiState.stations,
                            key = { station -> station.StationUuid }
                        ) { station ->
                            RadioStationItem(
                                station = station,
                                onClick = { 
                                    viewModel.onStationClick(station)
                                    onStationClick(station)
                                },
                                onLongClick = { 
                                    viewModel.onStationSwipe(station)
                                }
                            )
                        }
                    }
                }
            }

            // Pull-to-refresh indicator
            if (uiState.isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
