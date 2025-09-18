package com.eosphor.nonameradio.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eosphor.nonameradio.compose.screens.CategoriesScreen
import com.eosphor.nonameradio.compose.screens.HistoryScreen
import com.eosphor.nonameradio.compose.screens.MainScreen
import com.eosphor.nonameradio.compose.screens.PlayerFullScreen
import com.eosphor.nonameradio.compose.screens.RecordingsScreen
import com.eosphor.nonameradio.compose.screens.StationListScreen
import com.eosphor.nonameradio.compose.screens.StationsScreen
import com.eosphor.nonameradio.compose.theme.RadioDroidTheme
import com.eosphor.nonameradio.compose.viewmodels.CategoriesScreenViewModel
import com.eosphor.nonameradio.compose.viewmodels.HistoryScreenViewModel
import com.eosphor.nonameradio.compose.viewmodels.MainScreenViewModel
import com.eosphor.nonameradio.compose.viewmodels.PlayerFullViewModel
import com.eosphor.nonameradio.compose.viewmodels.RecordingsScreenViewModel
import com.eosphor.nonameradio.compose.viewmodels.StationListViewModel
import com.eosphor.nonameradio.compose.viewmodels.StationsScreenViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RadioDroidTheme {
                RadioDroidApp()
            }
        }
    }
}

@Composable
fun RadioDroidApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            val viewModel: MainScreenViewModel = viewModel()
            MainScreen(
                viewModel = viewModel,
                onNavigateToStationList = {
                    navController.navigate("station_list")
                },
                onNavigateToStations = {
                    navController.navigate("stations")
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                },
                onNavigateToRecordings = {
                    navController.navigate("recordings")
                },
                onNavigateToCategories = { categoryRoute ->
                    navController.navigate(categoryRoute)
                }
            )
        }
        
        composable("station_list") {
            val viewModel: StationListViewModel = viewModel()
            StationListScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("stations") {
            val viewModel: StationsScreenViewModel = viewModel()
            StationsScreen(
                viewModel = viewModel,
                onStationClick = { station ->
                    navController.navigate("player_full")
                },
                onStationLongClick = { station ->
                    // TODO: Handle station long click - show context menu
                },
                onRetryClick = {
                    // TODO: Handle retry
                },
                onRefresh = {
                    viewModel.refreshStations()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("history") {
            val viewModel: HistoryScreenViewModel = viewModel()
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStationClick = { station ->
                    navController.navigate("player_full")
                }
            )
        }
        
        composable("categories/tags") {
            val viewModel: CategoriesScreenViewModel = viewModel()
            CategoriesScreen(
                viewModel = viewModel,
                searchStyle = com.eosphor.nonameradio.station.StationsFilter.SearchStyle.ByTagExact,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCategoryClick = { category ->
                    // TODO: Navigate to stations filtered by tag
                    navController.navigate("stations")
                }
            )
        }
        
        composable("categories/languages") {
            val viewModel: CategoriesScreenViewModel = viewModel()
            CategoriesScreen(
                viewModel = viewModel,
                searchStyle = com.eosphor.nonameradio.station.StationsFilter.SearchStyle.ByLanguageExact,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCategoryClick = { category ->
                    // TODO: Navigate to stations filtered by language
                    navController.navigate("stations")
                }
            )
        }
        
        composable("categories/countries") {
            val viewModel: CategoriesScreenViewModel = viewModel()
            CategoriesScreen(
                viewModel = viewModel,
                searchStyle = com.eosphor.nonameradio.station.StationsFilter.SearchStyle.ByCountryCodeExact,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCategoryClick = { category ->
                    // TODO: Navigate to stations filtered by country
                    navController.navigate("stations")
                }
            )
        }
        
        composable("recordings") {
            val viewModel: RecordingsScreenViewModel = viewModel()
            RecordingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRecordingClick = { recording ->
                    // TODO: Handle recording click - open audio file
                }
            )
        }
        
        composable("player_full") {
            val viewModel: PlayerFullViewModel = viewModel()
            PlayerFullScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStationClick = { station ->
                    // TODO: Handle station click
                }
            )
        }
    }
}