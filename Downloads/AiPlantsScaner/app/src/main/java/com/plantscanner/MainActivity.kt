package com.plantscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.plantscanner.data.database.PlantDatabase
import com.plantscanner.data.repository.PlantRepository
import com.plantscanner.ui.screens.HomeScreen
import com.plantscanner.ui.screens.ResultScreen
import com.plantscanner.ui.theme.PlantScannerTheme
import com.plantscanner.viewmodel.PlantScanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = PlantDatabase.getDatabase(applicationContext)
        val repository = PlantRepository(database.plantDao(), applicationContext)
        
        setContent {
            PlantScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlantScannerApp(repository)
                }
            }
        }
    }
}

@Composable
fun PlantScannerApp(repository: PlantRepository) {
    val navController = rememberNavController()
    val viewModel: PlantScanViewModel = viewModel(
        factory = PlantScanViewModelFactory(repository)
    )
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToResult = { analysis ->
                    navController.navigate("result/${analysis.id}")
                }
            )
        }
        
        composable(
            route = "result/{analysisId}",
            arguments = listOf(navArgument("analysisId") { type = NavType.LongType })
        ) { backStackEntry ->
            val analysisId = backStackEntry.arguments?.getLong("analysisId") ?: 0
            val analysis = viewModel.history.collectAsState().value
                .find { it.id == analysisId }
            
            if (analysis != null) {
                ResultScreen(
                    analysis = analysis,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
