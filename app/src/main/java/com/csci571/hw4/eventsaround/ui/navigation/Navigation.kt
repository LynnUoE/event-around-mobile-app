package com.csci571.hw4.eventsaround.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.csci571.hw4.eventsaround.ui.screens.*
import com.csci571.hw4.eventsaround.ui.viewmodel.SearchViewModel

/**
 * Main navigation component with bottom navigation bar
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Share the same ViewModel instance across screens
    val searchViewModel: SearchViewModel = viewModel()

    // Define bottom navigation items
    val bottomNavItems = listOf(
        NavigationItem.Search,
        NavigationItem.Favorites
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination
                                popUpTo(Screen.Search.route) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Search.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Search Screen (Start destination)
            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateToResults = {
                        navController.navigate(Screen.Results.route)
                    },
                    viewModel = searchViewModel  // Pass shared ViewModel
                )
            }

            // Favorites/Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.Details.createRoute(eventId))
                    }
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
                    viewModel = searchViewModel  // Pass shared ViewModel
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
}

/**
 * Screen routes
 */
sealed class Screen(val route: String) {
    object Search : Screen("search")
    object Home : Screen("home")
    object Results : Screen("results")
    object Details : Screen("details/{eventId}") {
        fun createRoute(eventId: String) = "details/$eventId"
    }
}

/**
 * Bottom navigation items
 */
sealed class NavigationItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    object Search : NavigationItem(
        route = Screen.Search.route,
        icon = Icons.Default.Search,
        label = "Search"
    )

    object Favorites : NavigationItem(
        route = Screen.Home.route,
        icon = Icons.Default.Favorite,
        label = "Favorites"
    )
}