package com.csci571.hw4.eventsaround.ui.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csci571.hw4.eventsaround.data.model.Event
import com.csci571.hw4.eventsaround.ui.components.*
import kotlinx.coroutines.delay

/**
 * Test screen for Components
 * This screen demonstrates and tests all component functionality:
 * - EventCard with different states
 * - SearchBar with autocomplete
 * - DebouncedSearchBar
 * - All loading indicators
 * - Empty and error states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentsTestScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("EventCard", "SearchBar", "Progress Indicators")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Components Test") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab selector
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> EventCardTestSection()
                    1 -> SearchBarTestSection()
                    2 -> ProgressIndicatorTestSection()
                }
            }
        }
    }
}

/**
 * Test section for EventCard component
 */
@Composable
fun EventCardTestSection() {
    var isFavorite1 by remember { mutableStateOf(false) }
    var isFavorite2 by remember { mutableStateOf(true) }
    var isFavorite3 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "EventCard Component Tests",
            style = MaterialTheme.typography.headlineSmall
        )

        Divider()

        // Test 1: Music Event Card
        Text(
            text = "1. Music Event (Not Favorite)",
            style = MaterialTheme.typography.titleMedium
        )
        EventCard(
            event = createSampleEvent(
                id = "1",
                name = "Taylor Swift | The Eras Tour",
                venue = "SoFi Stadium",
                date = "2024-12-15",
                time = "19:30:00",
                category = "Music",
                imageUrl = "https://via.placeholder.com/300"
            ),
            isFavorite = isFavorite1,
            onEventClick = {
                println("Event clicked: $it")
            },
            onFavoriteClick = {
                isFavorite1 = !isFavorite1
                println("Favorite toggled for event 1")
            }
        )

        // Test 2: Sports Event Card (Favorite)
        Text(
            text = "2. Sports Event (Favorite)",
            style = MaterialTheme.typography.titleMedium
        )
        EventCard(
            event = createSampleEvent(
                id = "2",
                name = "Los Angeles Lakers vs Golden State Warriors",
                venue = "Crypto.com Arena",
                date = "2024-12-20",
                time = "20:00:00",
                category = "Sports",
                imageUrl = "https://via.placeholder.com/300"
            ),
            isFavorite = isFavorite2,
            onEventClick = {
                println("Event clicked: $it")
            },
            onFavoriteClick = {
                isFavorite2 = !isFavorite2
                println("Favorite toggled for event 2")
            }
        )

        // Test 3: Arts & Theatre with Category Icon
        Text(
            text = "3. Arts Event (With Category Icon)",
            style = MaterialTheme.typography.titleMedium
        )
        EventCard(
            event = createSampleEvent(
                id = "3",
                name = "Hamilton - An American Musical",
                venue = "Pantages Theatre",
                date = "2024-12-25",
                time = "14:00:00",
                category = "Arts & Theatre",
                imageUrl = "https://via.placeholder.com/300"
            ),
            isFavorite = isFavorite3,
            onEventClick = {
                println("Event clicked: $it")
            },
            onFavoriteClick = {
                isFavorite3 = !isFavorite3
                println("Favorite toggled for event 3")
            },
            showCategoryIcon = true
        )

        // Test 4: Long event name test
        Text(
            text = "4. Long Event Name (Overflow Test)",
            style = MaterialTheme.typography.titleMedium
        )
        EventCard(
            event = createSampleEvent(
                id = "4",
                name = "This is a very long event name that should be truncated with ellipsis when it exceeds two lines of text",
                venue = "Very Long Venue Name That Should Also Be Truncated",
                date = "2025-01-01",
                time = "18:00:00",
                category = "Film",
                imageUrl = "https://via.placeholder.com/300"
            ),
            isFavorite = false,
            onEventClick = { println("Event clicked: $it") },
            onFavoriteClick = { println("Favorite toggled for event 4") }
        )

        // Test 5: All categories color test
        Text(
            text = "5. Category Colors Test",
            style = MaterialTheme.typography.titleMedium
        )

        listOf("Music", "Sports", "Arts & Theatre", "Film", "Miscellaneous").forEach { category ->
            EventCard(
                event = createSampleEvent(
                    id = category,
                    name = "$category Event",
                    venue = "Test Venue",
                    date = "2024-12-31",
                    time = "20:00:00",
                    category = category,
                    imageUrl = "https://via.placeholder.com/300"
                ),
                isFavorite = false,
                onEventClick = { },
                onFavoriteClick = { },
                showCategoryIcon = true
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Test section for SearchBar component
 */
@Composable
fun SearchBarTestSection() {
    var query1 by remember { mutableStateOf("") }
    var query2 by remember { mutableStateOf("") }
    var query3 by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    // Mock suggestions
    val mockSuggestions = listOf(
        "Concert",
        "Comedy Show",
        "Classical Music",
        "Country Music",
        "Christmas Events"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "SearchBar Component Tests",
            style = MaterialTheme.typography.headlineSmall
        )

        Divider()

        // Test 1: Basic SearchBar with manual suggestions
        Text(
            text = "1. Basic SearchBar (Type 'c' to see suggestions)",
            style = MaterialTheme.typography.titleMedium
        )
        SearchBar(
            query = query1,
            onQueryChange = {
                query1 = it
                showError = false
            },
            onSearch = {
                if (query1.isBlank()) {
                    showError = true
                } else {
                    println("Search submitted: $query1")
                }
            },
            suggestions = if (query1.startsWith("c", ignoreCase = true)) {
                mockSuggestions.filter { it.startsWith("c", ignoreCase = true) }
            } else {
                emptyList()
            },
            onSuggestionSelected = { suggestion ->
                query1 = suggestion
                println("Suggestion selected: $suggestion")
            },
            placeholder = "Search events...",
            isError = showError,
            errorMessage = "Keyword is required"
        )

        // Test 2: DebouncedSearchBar
        Text(
            text = "2. DebouncedSearchBar (Auto-fetch after 500ms)",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Type to see delayed suggestions",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DebouncedSearchBar(
            query = query2,
            onQueryChange = { query2 = it },
            onSearch = {
                println("Debounced search submitted: $query2")
            },
            onGetSuggestions = { q ->
                // Simulate API call with delay
                delay(200)
                mockSuggestions.filter {
                    it.contains(q, ignoreCase = true)
                }
            },
            placeholder = "Search with autocomplete...",
            debounceMillis = 500L
        )

        // Test 3: SimpleSearchBar
        Text(
            text = "3. SimpleSearchBar (No Autocomplete)",
            style = MaterialTheme.typography.titleMedium
        )
        SimpleSearchBar(
            query = query3,
            onQueryChange = { query3 = it },
            onSearch = {
                println("Simple search submitted: $query3")
            },
            placeholder = "Simple search..."
        )

        // Test 4: Disabled SearchBar
        Text(
            text = "4. Disabled SearchBar",
            style = MaterialTheme.typography.titleMedium
        )
        SearchBar(
            query = "Disabled",
            onQueryChange = { },
            onSearch = { },
            placeholder = "Disabled search...",
            enabled = false
        )

        // Test 5: Error State
        Text(
            text = "5. SearchBar with Error",
            style = MaterialTheme.typography.titleMedium
        )
        SearchBar(
            query = "",
            onQueryChange = { },
            onSearch = { },
            placeholder = "Search...",
            isError = true,
            errorMessage = "This field is required"
        )

        // Test results display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Test Results:",
                    style = MaterialTheme.typography.titleSmall
                )
                Text("Query 1: $query1")
                Text("Query 2: $query2")
                Text("Query 3: $query3")
                Text("Show Error: $showError")
            }
        }
    }
}

/**
 * Test section for Progress Indicators
 */
@Composable
fun ProgressIndicatorTestSection() {
    var showFullScreen by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showShimmer by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.3f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Progress Indicator Tests",
            style = MaterialTheme.typography.headlineSmall
        )

        Divider()

        // Test 1: CenteredLoadingIndicator
        Text(
            text = "1. Centered Loading Indicator",
            style = MaterialTheme.typography.titleMedium
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            CenteredLoadingIndicator(message = "Loading events...")
        }

        // Test 2: InlineLoadingIndicator
        Text(
            text = "2. Inline Loading Indicator",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Loading")
            InlineLoadingIndicator()
        }

        // Test 3: Linear Progress Bars
        Text(
            text = "3. Linear Progress Bars",
            style = MaterialTheme.typography.titleMedium
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Determinate (${(progress * 100).toInt()}%)")
            LinearProgressBar(progress = progress)

            Text("Indeterminate")
            IndeterminateLinearProgressBar()

            Button(onClick = {
                progress = (progress + 0.1f).coerceAtMost(1f)
            }) {
                Text("Increase Progress")
            }
        }

        // Test 4: EmptyStateIndicator
        Text(
            text = "4. Empty State Indicator",
            style = MaterialTheme.typography.titleMedium
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            EmptyStateIndicator(
                message = "No events found",
                iconContent = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }

        // Test 5: ErrorStateIndicator
        Text(
            text = "5. Error State Indicator",
            style = MaterialTheme.typography.titleMedium
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            ErrorStateIndicator(
                message = "Failed to load events. Please try again.",
                onRetry = {
                    println("Retry clicked")
                }
            )
        }

        // Test 6: ShimmerLoadingItem
        Text(
            text = "6. Shimmer Loading Items",
            style = MaterialTheme.typography.titleMedium
        )
        Button(onClick = { showShimmer = !showShimmer }) {
            Text(if (showShimmer) "Hide Shimmer" else "Show Shimmer")
        }
        if (showShimmer) {
            repeat(3) {
                ShimmerLoadingItem()
            }
        }

        // Test 7: Full Screen Loading
        Text(
            text = "7. Full Screen Loading Overlay",
            style = MaterialTheme.typography.titleMedium
        )
        Button(onClick = { showFullScreen = true }) {
            Text("Show Full Screen Loading")
        }

        // Test 8: Loading Dialog
        Text(
            text = "8. Loading Dialog (Blocking)",
            style = MaterialTheme.typography.titleMedium
        )
        Button(onClick = { showDialog = true }) {
            Text("Show Loading Dialog")
        }
    }

    // Full screen loading overlay
    if (showFullScreen) {
        FullScreenLoadingIndicator(
            isLoading = true,
            message = "Searching events..."
        )

        // Auto-hide after 3 seconds
        LaunchedEffect(Unit) {
            delay(3000)
            showFullScreen = false
        }
    }

    // Loading dialog
    LoadingDialog(
        isLoading = showDialog,
        message = "Processing..."
    )

    // Auto-hide dialog after 2 seconds
    if (showDialog) {
        LaunchedEffect(Unit) {
            delay(2000)
            showDialog = false
        }
    }
}

/**
 * Helper function to create sample Event objects for testing
 */
private fun createSampleEvent(
    id: String,
    name: String,
    venue: String,
    date: String,
    time: String,
    category: String,
    imageUrl: String
): Event {
    return Event(
        id = id,
        name = name,
        dates = com.csci571.hw4.eventsaround.data.model.EventDates(
            start = com.csci571.hw4.eventsaround.data.model.EventDate(
                localDate = date,
                localTime = time
            )
        ),
        embedded = com.csci571.hw4.eventsaround.data.model.EventEmbedded(
            venues = listOf(
                com.csci571.hw4.eventsaround.data.model.Venue(
                    name = venue
                )
            ),
            attractions = null
        ),
        classifications = listOf(
            com.csci571.hw4.eventsaround.data.model.Classification(
                segment = com.csci571.hw4.eventsaround.data.model.Segment(name = category),
                genre = null
            )
        ),
        images = listOf(
            com.csci571.hw4.eventsaround.data.model.EventImage(
                url = imageUrl,
                width = 300,
                height = 300
            )
        ),
        priceRanges = listOf(
            com.csci571.hw4.eventsaround.data.model.PriceRange(
                min = 50.0,
                max = 250.0
            )
        )
    )
}