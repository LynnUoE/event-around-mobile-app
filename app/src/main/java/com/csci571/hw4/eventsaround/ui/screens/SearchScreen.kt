package com.csci571.hw4.eventsaround.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.csci571.hw4.eventsaround.data.model.SearchParams
import com.csci571.hw4.eventsaround.ui.components.SearchBar
import com.csci571.hw4.eventsaround.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxWidth

/**
 * Search Screen with proper layout matching HW4 requirements
 * Layout structure:
 * 1. TopBar: Back button + Search input + Search icon
 * 2. Location & Distance row
 * 3. Category tabs
 * 4. Results area
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
    var isLoadingSuggestions by remember { mutableStateOf(false) }

    // Error states
    var showKeywordError by remember { mutableStateOf(false) }
    var keywordError by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

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

    // Debounced autocomplete
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
            autoDetect = useCurrentLocation,
            location = if (useCurrentLocation) "" else manualLocation
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
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                showLabel = false // Don't show label in top bar
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
                        // Location selector
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            // Location dropdown button
                            TextButton(
                                onClick = { /* TODO: Show location picker */ },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = if (useCurrentLocation) "Current Location" else manualLocation.ifBlank { "Select Location" },
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change location",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Distance selector with arrows
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.widthIn(min = 24.dp)
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 3: Category tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedCategory,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 0.dp,
                        divider = {} // Remove default divider
                    ) {
                        categories.forEachIndexed { index, category ->
                            Tab(
                                selected = selectedCategory == index,
                                onClick = { selectedCategory = index },
                                text = {
                                    Text(
                                        text = category,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedCategory == index) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Results area - initially shows "No events found"
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(32.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Text(
                    text = "No events found",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}