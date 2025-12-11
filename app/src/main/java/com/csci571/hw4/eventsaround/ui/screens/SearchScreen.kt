package com.csci571.hw4.eventsaround.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Search Screen - Main interface for searching events
 * Includes keyword input, location selector, distance, and category tabs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToResults: () -> Unit
) {
    // State variables for form inputs
    var keyword by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("10") }
    var selectedCategory by remember { mutableStateOf(0) }
    var location by remember { mutableStateOf("Current Location") }
    var showKeywordError by remember { mutableStateOf(false) }
    var showDistanceError by remember { mutableStateOf(false) }

    val categories = listOf("All", "Music", "Sports", "Arts & Theatre", "Film", "Miscellaneous")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Search") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Keyword input field with validation
            OutlinedTextField(
                value = keyword,
                onValueChange = {
                    keyword = it
                    showKeywordError = false
                },
                label = { Text("Keyword *") },
                placeholder = { Text("Search events...") },
                modifier = Modifier.fillMaxWidth(),
                isError = showKeywordError,
                singleLine = true,
                trailingIcon = {
                    if (keyword.isNotEmpty()) {
                        IconButton(onClick = { keyword = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                supportingText = {
                    if (showKeywordError) {
                        Text(
                            text = "Keyword is required",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            )

            // Distance input with increment/decrement buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = distance,
                    onValueChange = {
                        // Only allow numeric input
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            distance = it
                            showDistanceError = false
                        }
                    },
                    label = { Text("Distance") },
                    modifier = Modifier.weight(1f),
                    isError = showDistanceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        if (showDistanceError) {
                            Text(
                                text = "Distance must be greater than 0",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Decrement button
                OutlinedButton(
                    onClick = {
                        val current = distance.toIntOrNull() ?: 10
                        if (current > 1) {
                            distance = (current - 1).toString()
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("-", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Increment button
                OutlinedButton(
                    onClick = {
                        val current = distance.toIntOrNull() ?: 10
                        distance = (current + 1).toString()
                    },
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text("mi", fontSize = 14.sp)
            }

            // Location selector
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    // TODO: Add dropdown icon and location picker
                }
            )

            // Category tabs
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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
                            // TODO: Save search parameters and perform API call
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