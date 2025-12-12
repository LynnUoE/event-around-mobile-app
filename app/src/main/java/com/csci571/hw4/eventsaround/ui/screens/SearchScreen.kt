package com.csci571.hw4.eventsaround.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.csci571.hw4.eventsaround.data.model.SearchParams
import com.csci571.hw4.eventsaround.ui.components.SearchBar
import com.csci571.hw4.eventsaround.ui.components.LocationSelector
import com.csci571.hw4.eventsaround.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.delay

/**
 * Search Screen with proper layout matching HW4 requirements
 * Layout structure:
 * 1. TopBar: Back button + Search input + Search icon
 * 2. Location & Distance row
 * 3. Category tabs
 * 4. Results/Info area
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToResults: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    // Form state
    var keyword by remember { mutableStateOf("") }
    var distance by remember { mutableIntStateOf(10) }
    var selectedCategory by remember { mutableIntStateOf(0) }
    var useCurrentLocation by remember { mutableStateOf(true) }
    var manualLocation by remember { mutableStateOf("") }

    // Autocomplete state
    val suggestions by viewModel.suggestions.collectAsState()
    val locationSuggestions by viewModel.locationSuggestions.collectAsState()
    var isLoadingSuggestions by remember { mutableStateOf(false) }

    // Error states
    var showKeywordError by remember { mutableStateOf(false) }
    var keywordError by remember { mutableStateOf("") }

    // Category definitions
    val categories = listOf("All", "Music", "Sports", "Arts & Theatre", "Film", "Miscellaneous")
    val categorySegmentIds = mapOf(
        0 to SearchParams.CATEGORY_ALL,
        1 to SearchParams.CATEGORY_MUSIC,
        2 to SearchParams.CATEGORY_SPORTS,
        3 to SearchParams.CATEGORY_ARTS,
        4 to SearchParams.CATEGORY_FILM,
        5 to SearchParams.CATEGORY_MISCELLANEOUS
    )

    // Debounced autocomplete for keyword
    LaunchedEffect(keyword) {
        if (keyword.trim().length >= 2) {
            isLoadingSuggestions = true
            delay(300)
            viewModel.getAutocompleteSuggestions(keyword)
            delay(100)
            isLoadingSuggestions = false
        }
    }

    // Search function
    fun performSearch() {
        if (keyword.isBlank()) {
            showKeywordError = true
            keywordError = "Keyword is required"
            return
        }

        showKeywordError = false

        val params = SearchParams(
            keyword = keyword.trim(),
            distance = distance,
            category = categorySegmentIds[selectedCategory] ?: SearchParams.CATEGORY_ALL,
            location = if (useCurrentLocation) "" else manualLocation,
            autoDetect = useCurrentLocation
        )

        viewModel.searchEvents(params)
        onNavigateToResults()
    }

    Scaffold(
        topBar = {
            // Custom top bar with search field
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Row 1: Back button + Search input + Search icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Back button
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Search input field with autocomplete
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            SearchBar(
                                query = keyword,
                                onQueryChange = {
                                    keyword = it
                                    showKeywordError = false
                                },
                                onSearch = { performSearch() },
                                suggestions = suggestions,
                                onSuggestionSelected = {
                                    keyword = it
                                    showKeywordError = false
                                },
                                isLoadingSuggestions = isLoadingSuggestions,
                                placeholder = "Search events...",
                                isError = showKeywordError,
                                errorMessage = keywordError,
                                showLabel = false
                            )
                        }

                        // Search icon button
                        IconButton(
                            onClick = { performSearch() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 2: Location selector + Distance selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Location selector using the new component
                        LocationSelector(
                            useCurrentLocation = useCurrentLocation,
                            manualLocation = manualLocation,
                            onLocationTypeChange = { isCurrentLocation ->
                                useCurrentLocation = isCurrentLocation
                                if (isCurrentLocation) {
                                    // Trigger current location fetch
                                    viewModel.fetchCurrentLocation()
                                }
                            },
                            onManualLocationChange = { newLocation ->
                                manualLocation = newLocation
                            },
                            onLocationSuggestionSelected = { selectedLocation ->
                                manualLocation = selectedLocation
                                useCurrentLocation = false
                            },
                            locationSuggestions = locationSuggestions,
                            onLoadLocationSuggestions = { query ->
                                viewModel.loadLocationSuggestions(query)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Distance selector with swap icon and arrows
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Swap/toggle icon (for visual consistency)
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            // Decrease button
                            IconButton(
                                onClick = { if (distance > 1) distance-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Decrease distance",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Distance value
                            Text(
                                text = distance.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.widthIn(min = 30.dp)
                            )

                            // Increase button
                            IconButton(
                                onClick = { distance++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Increase distance",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Unit label
                            Text(
                                text = "mi",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category tabs
            ScrollableTabRow(
                selectedTabIndex = selectedCategory,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedCategory == index,
                        onClick = { selectedCategory = index },
                        text = {
                            Text(
                                text = category,
                                fontSize = 14.sp,
                                fontWeight = if (selectedCategory == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Info area (placeholder for "No events found" message)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No events found",
                        modifier = Modifier.padding(24.dp),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}