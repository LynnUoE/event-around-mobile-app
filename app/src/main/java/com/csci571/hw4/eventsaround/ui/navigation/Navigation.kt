package com.csci571.hw4.eventsaround.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.csci571.hw4.eventsaround.ui.screens.HomeScreen
import com.csci571.hw4.eventsaround.ui.screens.SearchScreen
import com.csci571.hw4.eventsaround.ui.screens.ResultsScreen
import com.csci571.hw4.eventsaround.ui.screens.DetailsScreen

/**
 * Sealed class representing all navigation destinations in the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Results : Screen("results")
    object Details : Screen("details/{eventId}") {
        fun createRoute(eventId: String) = "details/$eventId"
    }
}

/**
 * Bottom navigation items
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Search : BottomNavItem(
        route = Screen.Search.route,
        title = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )

    object Favorites : BottomNavItem(
        route = Screen.Home.route,
        title = "Favorites",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )
}

/**
 * Main navigation component with bottom navigation bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val bottomNavItems = listOf(
        BottomNavItem.Search,
        BottomNavItem.Favorites
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if bottom bar should be shown
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) {
                                        item.selectedIcon
                                    } else {
                                        item.unselectedIcon
                                    },
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
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
                    }
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
                    }
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