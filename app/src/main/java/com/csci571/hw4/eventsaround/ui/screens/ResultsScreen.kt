package com.csci571.hw4.eventsaround.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Results Screen - Displays search results in a scrollable list
 * Shows event cards with name, venue, date/time, image, and favorite button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onNavigateBack: () -> Unit,
    onEventClick: (String) -> Unit
) {
    // TODO: Get actual search results from ViewModel/Repository
    val searchResults = remember { mutableStateListOf<SearchResultEvent>() }
    val isLoading by remember { mutableStateOf(false) }

    // Temporary: Simulate loading results
    LaunchedEffect(Unit) {
        // TODO: Replace with actual API call
        // Example: viewModel.searchEvents(searchParams)
        searchResults.clear()
        // For testing, you can add sample data:
        // searchResults.addAll(getSampleEvents())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Results") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    // Show loading indicator while fetching results
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                searchResults.isEmpty() -> {
                    // Show "No events found" message when results are empty
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                            .fillMaxWidth(0.8f),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "No events found",
                            modifier = Modifier.padding(vertical = 32.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    // Display results list
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchResults) { event ->
                            SearchResultCard(
                                event = event,
                                onClick = { onEventClick(event.id) },
                                onFavoriteToggle = {
                                    // TODO: Toggle favorite status in repository
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
 * Card component for displaying a search result event
 * Shows event image, details, category badge, and favorite button
 */
@Composable
fun SearchResultCard(
    event: SearchResultEvent,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(event.isFavorite) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Event image
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.name,
                modifier = Modifier
                    .size(80.dp),
                contentScale = ContentScale.Crop
            )

            // Event details column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Event name
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Venue name
                Text(
                    text = event.venue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Date and time
                Text(
                    text = event.dateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Category badge (optional)
                if (event.category.isNotEmpty()) {
                    Surface(
                        color = getCategoryColor(event.category),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = event.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Favorite toggle button
            IconButton(
                onClick = {
                    isFavorite = !isFavorite
                    onFavoriteToggle()
                }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Get color for category badge based on category name
 */
@Composable
fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "music" -> Color(0xFF1DB954)        // Green
        "sports" -> Color(0xFF0066CC)       // Blue
        "arts & theatre" -> Color(0xFF9C27B0)  // Purple
        "film" -> Color(0xFFE91E63)         // Pink
        "miscellaneous" -> Color(0xFF607D8B)   // Blue Grey
        else -> Color(0xFF757575)           // Grey
    }
}

/**
 * Data class representing a search result event
 * TODO: Move to data/model package and use actual API response model
 */
data class SearchResultEvent(
    val id: String,
    val name: String,
    val venue: String,
    val dateTime: String,
    val imageUrl: String,
    val category: String,
    val isFavorite: Boolean = false
)

/**
 * Sample data for testing (remove when integrating with API)
 */
private fun getSampleEvents(): List<SearchResultEvent> {
    return listOf(
        SearchResultEvent(
            id = "1",
            name = "Sample Concert Event",
            venue = "Madison Square Garden",
            dateTime = "Dec 15, 2024, 7:30 PM",
            imageUrl = "https://via.placeholder.com/300",
            category = "Music",
            isFavorite = false
        ),
        SearchResultEvent(
            id = "2",
            name = "Basketball Game",
            venue = "Staples Center",
            dateTime = "Dec 20, 2024, 8:00 PM",
            imageUrl = "https://via.placeholder.com/300",
            category = "Sports",
            isFavorite = true
        )
    )
}