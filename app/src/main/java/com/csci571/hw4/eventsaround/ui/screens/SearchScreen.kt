package com.csci571.hw4.eventsaround.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.csci571.hw4.eventsaround.data.model.SearchParams
import com.csci571.hw4.eventsaround.ui.viewmodel.SearchViewModel

/**
 * Search Screen - Main form for searching events
 * User inputs: keyword, distance, location, category
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToResults: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    // Form state variables
    var keyword by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("10") }
    var location by remember { mutableStateOf("Current Location") }
    var selectedCategory by remember { mutableIntStateOf(0) }

    // Error states
    var showKeywordError by remember { mutableStateOf(false) }
    var showDistanceError by remember { mutableStateOf(false) }

    // Category names
    val categories = listOf("All", "Music", "Sports", "Arts & Theatre", "Film", "Miscellaneous")

    // Map categories to segment IDs
    val categorySegmentIds = mapOf(
        0 to SearchParams.CATEGORY_ALL,
        1 to SearchParams.CATEGORY_MUSIC,
        2 to SearchParams.CATEGORY_SPORTS,
        3 to SearchParams.CATEGORY_ARTS,
        4 to SearchParams.CATEGORY_FILM,
        5 to SearchParams.CATEGORY_MISCELLANEOUS
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Search") },
                actions = {
                    IconButton(onClick = { /* Search icon in top bar */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Keyword input field
            OutlinedTextField(
                value = keyword,
                onValueChange = {
                    keyword = it
                    showKeywordError = false
                },
                label = { Text("Keyword") },
                placeholder = { Text("Search events...") },
                modifier = Modifier.fillMaxWidth(),
                isError = showKeywordError,
                supportingText = {
                    if (showKeywordError) {
                        Text(
                            text = "Keyword is required",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true
            )

            // Distance input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = distance,
                    onValueChange = {
                        // Only allow numbers
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            distance = it
                            showDistanceError = false
                        }
                    },
                    label = { Text("Distance") },
                    modifier = Modifier.weight(1f),
                    isError = showDistanceError,
                    supportingText = {
                        if (showDistanceError) {
                            Text(
                                text = "Distance must be greater than 0",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true
                )

                Text("mi", modifier = Modifier.padding(top = 8.dp))
            }

            // Location dropdown (simplified - using default location)
            OutlinedTextField(
                value = location,
                onValueChange = { },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.LocationOn, "Location")
                }
            )

            // Category tabs
            ScrollableTabRow(
                selectedTabIndex = selectedCategory,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedCategory == index,
                        onClick = { selectedCategory = index },
                        text = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Submit/Search button
                Button(
                    onClick = {
                        // Validate inputs before proceeding
                        var hasError = false

                        // Check if keyword is empty
                        if (keyword.isBlank()) {
                            showKeywordError = true
                            hasError = true
                        }

                        // Check if distance is valid
                        val distanceValue = distance.toIntOrNull()
                        if (distanceValue == null || distanceValue <= 0) {
                            showDistanceError = true
                            hasError = true
                        }

                        // Navigate to results if validation passes
                        if (!hasError) {
                            // Create search params
                            val searchParams = SearchParams(
                                keyword = keyword.trim(),
                                distance = distanceValue!!,
                                category = categorySegmentIds[selectedCategory] ?: "",
                                location = location,
                                autoDetect = true,  // Using current location by default
                                latitude = null,  // Will use default coordinates in SearchParams
                                longitude = null
                            )

                            // Trigger search
                            viewModel.searchEvents(searchParams)

                            // Navigate to results
                            onNavigateToResults()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("SEARCH")
                }

                // Clear button
                OutlinedButton(
                    onClick = {
                        // Reset all form fields to default values
                        keyword = ""
                        distance = "10"
                        location = "Current Location"
                        selectedCategory = 0
                        showKeywordError = false
                        showDistanceError = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("CLEAR")
                }
            }
        }
    }
}