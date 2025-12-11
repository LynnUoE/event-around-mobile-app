package com.csci571.hw4.eventsaround.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.csci571.hw4.eventsaround.ui.screens.*
import com.csci571.hw4.eventsaround.ui.viewmodel.SearchViewModel

/**
 * Main navigation component - NO BOTTOM NAVIGATION BAR
 * Start with Favorites screen
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Share the same ViewModel instance across screens
    val searchViewModel: SearchViewModel = viewModel()

    // No Scaffold with bottom bar - just pure navigation
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route  // Start with Favorites
    ) {
        // Favorites/Home Screen (START DESTINATION)
        composable(Screen.Home.route) {
            HomeScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.Details.createRoute(eventId))
                },
                onSearchClick = {
                    // Navigate to search screen when search icon is clicked
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        // Search Screen
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToResults = {
                    navController.navigate(Screen.Results.route)
                },
                onNavigateBack = {
                    // Go back to favorites screen
                    navController.popBackStack()
                },
                viewModel = searchViewModel
            )
        }

        // Search Results Screen
        composable(Screen.Results.route) {
            ResultsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEventClick = { eventId ->
                    navController.navigate(Screen.Details.createRoute(eventId))
                },
                viewModel = searchViewModel
            )
        }

        // Event Details Screen
        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            DetailsScreen(
                eventId = eventId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Screen routes
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Results : Screen("results")
    object Details : Screen("details/{eventId}") {
        fun createRoute(eventId: String) = "details/$eventId"
    }
}