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
import com.eosphor.nonameradio.compose.screens.MainScreen
import com.eosphor.nonameradio.compose.screens.StationListScreen
import com.eosphor.nonameradio.compose.theme.RadioDroidTheme
import com.eosphor.nonameradio.compose.viewmodels.MainScreenViewModel
import com.eosphor.nonameradio.compose.viewmodels.StationListViewModel

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
    }
}