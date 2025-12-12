package com.csci571.hw4.eventsaround.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.csci571.hw4.eventsaround.data.model.SearchParams
import com.csci571.hw4.eventsaround.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Search Screen - Event search form with autocomplete
 * Now supports dark mode
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
    var distance by remember { mutableStateOf(10) }
    var selectedCategory by remember { mutableIntStateOf(0) }
    var useCurrentLocation by remember { mutableStateOf(true) }
    var manualLocation by remember { mutableStateOf("") }

    // Autocomplete state
    val suggestions by viewModel.suggestions.collectAsState()
    var showSuggestions by remember { mutableStateOf(false) }

    // Error states
    var showKeywordError by remember { mutableStateOf(false) }
    var showLocationError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

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

    // Debounced autocomplete fetching
    LaunchedEffect(keyword) {
        if (keyword.trim().length > 0) {
            delay(300) // Debounce 300ms
            viewModel.getAutocompleteSuggestions(keyword)
            showSuggestions = true
        } else {
            showSuggestions = false
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                value = keyword,
                                onValueChange = {
                                    keyword = it
                                    showKeywordError = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search events...") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                trailingIcon = {
                                    if (keyword.isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                keyword = ""
                                                showSuggestions = false
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            )

                            // Autocomplete dropdown
                            if (showSuggestions && suggestions.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 56.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp)
                                    ) {
                                        // First item: user's input
                                        item {
                                            Text(
                                                text = keyword,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        showSuggestions = false
                                                    }
                                                    .padding(12.dp),
                                                fontSize = 14.sp
                                            )
                                            HorizontalDivider()
                                        }

                                        // API suggestions
                                        items(suggestions.filter {
                                            it.lowercase() != keyword.lowercase()
                                        }) { suggestion ->
                                            Text(
                                                text = suggestion,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        keyword = suggestion
                                                        showSuggestions = false
                                                    }
                                                    .padding(12.dp),
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
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
                            showKeywordError = keyword.isBlank()
                            showLocationError = !useCurrentLocation && manualLocation.isBlank()

                            if (!showKeywordError && !showLocationError) {
                                val searchParams = SearchParams(
                                    keyword = keyword.trim(),
                                    distance = distance,
                                    category = categorySegmentIds[selectedCategory] ?: "",
                                    autoDetect = useCurrentLocation,
                                    location = if (!useCurrentLocation) manualLocation else "",
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
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

            // Location and Distance row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Location selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = if (useCurrentLocation) "Current Location" else manualLocation.ifEmpty { "Enter location" },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Distance selector
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Decrease",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "$distance",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "mi",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Category tabs
            ScrollableTabRow(
                selectedTabIndex = selectedCategory,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedCategory == index,
                        onClick = { selectedCategory = index },
                        text = {
                            Text(
                                text = category,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            // "No events found" placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "No events found",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}