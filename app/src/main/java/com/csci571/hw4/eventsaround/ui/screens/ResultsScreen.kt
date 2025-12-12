package com.csci571.hw4.eventsaround.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.csci571.hw4.eventsaround.data.model.Event
import com.csci571.hw4.eventsaround.ui.viewmodel.SearchViewModel
import com.csci571.hw4.eventsaround.ui.viewmodel.ResultsViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Results Screen - Displays search results with category tabs and filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onNavigateBack: () -> Unit,
    onEventClick: (String) -> Unit,
    searchViewModel: SearchViewModel = viewModel(),
    resultsViewModel: ResultsViewModel = viewModel()  // Add ResultsViewModel for favorite management
) {
    // Collect states from SearchViewModel
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    val error by searchViewModel.error.collectAsState()

    // Collect favorite states from ResultsViewModel
    val favoriteStates by resultsViewModel.favoriteStates.collectAsState()

    // Get last search params from ViewModel
    val lastSearchParams = searchViewModel.getLastSearchParams()

    // Category tabs state
    val categories = listOf("All", "Music", "Sports", "Arts & Theatre", "Film", "Miscellaneous")
    var selectedCategory by remember { mutableIntStateOf(0) }

    // Distance state for display
    var distance by remember { mutableIntStateOf(lastSearchParams?.distance ?: 10) }
    var useCurrentLocation by remember { mutableStateOf(lastSearchParams?.autoDetect ?: true) }

    // Initialize favorite states when search results change
    LaunchedEffect(searchResults) {
        resultsViewModel.initializeFavoriteStates(searchResults)
    }

    // Filter results based on selected category
    val filteredResults = remember(searchResults, selectedCategory) {
        if (selectedCategory == 0) {
            searchResults
        } else {
            val categoryName = categories[selectedCategory]
            searchResults.filter { event ->
                event.category.equals(categoryName, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                // Top bar with back button, search keyword, and search icon
                TopAppBar(
                    title = {
                        Text(
                            text = lastSearchParams?.keyword ?: "",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        IconButton(onClick = { /* Search action */ }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                )

                // Location and Distance row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE3F2FD))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Location selector
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (useCurrentLocation) "Current Location" else (lastSearchParams?.location ?: "Location"),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    // Distance display
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Distance",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$distance mi",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Category tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedCategory,
                    containerColor = Color(0xFFE3F2FD),
                    edgePadding = 0.dp
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
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { searchViewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }

                filteredResults.isEmpty() -> {
                    Text(
                        text = "No events found",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredResults) { event ->
                            EventResultCard(
                                event = event,
                                isFavorite = favoriteStates[event.id] ?: false,
                                onEventClick = { onEventClick(event.id) },
                                onFavoriteClick = {
                                    resultsViewModel.toggleFavorite(event)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Event card component for search results with favorite button
 */
@Composable
fun EventResultCard(
    event: Event,
    isFavorite: Boolean,
    onEventClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEventClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Event image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = event.images?.firstOrNull()?.url ?: "",
                    contentDescription = event.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Category badge - top left
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = event.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Date & Time badge - top right
                event.dates?.start?.let { start ->
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = formatDateTime(start.localDate, start.localTime),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Event details section at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = event.embedded?.venues?.firstOrNull()?.name ?: "Venue TBA",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp
                    )
                }

                // Favorite star button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/**
 * Format date and time to display format
 */
private fun formatDateTime(dateString: String?, timeString: String?): String {
    return try {
        if (dateString.isNullOrEmpty()) return "Date TBA"

        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputDateFormat.parse(dateString)
        val outputDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val formattedDate = date?.let { outputDateFormat.format(it) } ?: dateString

        if (!timeString.isNullOrEmpty()) {
            val inputTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
            val time = inputTimeFormat.parse(timeString)
            val outputTimeFormat = SimpleDateFormat("h:mm a", Locale.US)
            val formattedTime = time?.let { outputTimeFormat.format(it) } ?: timeString
            "$formattedDate, $formattedTime"
        } else {
            formattedDate
        }
    } catch (e: Exception) {
        "$dateString ${timeString ?: ""}"
    }
}