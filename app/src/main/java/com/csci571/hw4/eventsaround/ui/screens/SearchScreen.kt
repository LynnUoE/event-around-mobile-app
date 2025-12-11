package com.csci571.hw4.eventsaround.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.csci571.hw4.eventsaround.data.model.SearchParams
import com.csci571.hw4.eventsaround.ui.viewmodel.SearchViewModel

/**
 * Search Screen - Event search form
 * Matches the design from screenshot 2
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToResults: () -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: SearchViewModel = viewModel()
) {
    // Form state variables
    var keyword by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("10") }
    var selectedCategory by remember { mutableIntStateOf(0) }

    // Error states
    var showKeywordError by remember { mutableStateOf(false) }
    var showDistanceError by remember { mutableStateOf(false) }

    // Category names and mapping
    val categories = listOf("All", "Music", "Sports", "Arts & Theatre", "Film", "Miscellaneous")
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
                title = {
                    // Search input field in the top bar
                    OutlinedTextField(
                        value = keyword,
                        onValueChange = {
                            keyword = it
                            showKeywordError = false
                        },
                        placeholder = {
                            Text(
                                "Search events...",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Validate and perform search
                        var hasError = false

                        if (keyword.isBlank()) {
                            showKeywordError = true
                            hasError = true
                        }

                        val distanceValue = distance.toIntOrNull()
                        if (distanceValue == null || distanceValue <= 0) {
                            showDistanceError = true
                            hasError = true
                        }

                        if (!hasError) {
                            val searchParams = SearchParams(
                                keyword = keyword.trim(),
                                distance = distanceValue!!,
                                category = categorySegmentIds[selectedCategory] ?: "",
                                autoDetect = true,
                                latitude = null,
                                longitude = null
                            )
                            viewModel.searchEvents(searchParams)
                            onNavigateToResults()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE3F2FD) // Light blue background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Error message for keyword
            if (showKeywordError) {
                Text(
                    text = "Keyword is required",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Location row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Location icon and text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Location",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }

                // Distance controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search, // Replace with swap icon
                        contentDescription = "Distance",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )

                    // Distance input
                    OutlinedTextField(
                        value = distance,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                distance = it
                                showDistanceError = false
                            }
                        },
                        modifier = Modifier.width(60.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        isError = showDistanceError
                    )

                    Text(
                        text = "mi",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }

            // Category tabs
            ScrollableTabRow(
                selectedTabIndex = selectedCategory,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp,
                containerColor = Color.White,
                contentColor = Color.Black
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedCategory == index,
                        onClick = { selectedCategory = index },
                        text = {
                            Text(
                                category,
                                fontSize = 14.sp,
                                color = if (selectedCategory == index)
                                    Color(0xFF1976D2)
                                else
                                    Color.Black
                            )
                        }
                    )
                }
            }

            Divider(color = Color.LightGray, thickness = 1.dp)

            // Content area - "No events found" message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    color = Color(0xFFE3F2FD), // Light blue background
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "No events found",
                        modifier = Modifier.padding(vertical = 40.dp),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}