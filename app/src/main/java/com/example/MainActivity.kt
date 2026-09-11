package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.MainViewModel
import com.example.ui.screens.CalculatorDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ReferenceGuideScreen
import com.example.ui.theme.EngineeringToolkitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val themeMode by mainViewModel.themeMode.collectAsState()

            EngineeringToolkitTheme(themeMode = themeMode) {
                EngineeringAppNavHost(viewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun EngineeringAppNavHost(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier.fillMaxSize()
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToCalculator = { calcId ->
                    navController.navigate("calculator/$calcId")
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                },
                onNavigateToReference = {
                    navController.navigate("reference")
                }
            )
        }

        composable(
            route = "calculator/{calculatorId}",
            arguments = listOf(navArgument("calculatorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val calcId = backStackEntry.arguments?.getString("calculatorId") ?: "ohms_law"
            CalculatorDetailScreen(
                calculatorId = calcId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("history") {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCalculator = { calcId ->
                    navController.navigate("calculator/$calcId")
                }
            )
        }

        composable("reference") {
            ReferenceGuideScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
