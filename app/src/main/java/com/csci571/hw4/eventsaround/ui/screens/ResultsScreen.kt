package com.csci571.hw4.eventsaround.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.csci571.hw4.eventsaround.data.model.Event
import com.csci571.hw4.eventsaround.ui.viewmodel.SearchViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Results Screen - Displays search results in a scrollable list
 * Shows event cards with name, venue, date/time, image, and favorite button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onNavigateBack: () -> Unit,
    onEventClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    // Collect states from ViewModel
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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

                error != null -> {
                    // Show error message
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${error}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }

                searchResults.isEmpty() -> {
                    // Show "No events found" message when results list is empty
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "No events found",
                            modifier = Modifier.padding(32.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    // Display search results in a scrollable list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchResults) { event ->
                            EventResultCard(
                                event = event,
                                onClick = { onEventClick(event.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Event card component for search results
 * Uses Event model's helper properties for easy access
 */
@Composable
fun EventResultCard(
    event: Event,
    onClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Event image - using helper property
            AsyncImage(
                model = event.imageUrl,
                contentDescription = "Event image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Event details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Event name - direct property
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Venue name - using helper property
                Text(
                    text = event.venue.ifEmpty { "Venue TBA" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date and time - using helper properties
                Text(
                    text = formatDateTime(event.date, event.time),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category badge - using helper property
                if (event.category.isNotEmpty()) {
                    Surface(
                        color = getCategoryColor(event.category),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = event.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Favorite button
            IconButton(
                onClick = {
                    isFavorite = !isFavorite
                    // TODO: Add to favorites using repository
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
        "arts & theatre", "arts" -> Color(0xFF9C27B0)  // Purple
        "film" -> Color(0xFFE91E63)         // Pink
        "miscellaneous" -> Color(0xFF607D8B)   // Blue Grey
        else -> Color(0xFF757575)           // Grey
    }
}

/**
 * Format date and time for display
 * Converts to "MMM dd, yyyy, h:mm a" format
 */
fun formatDateTime(date: String, time: String): String {
    return try {
        if (date.isEmpty()) return "Date TBA"

        val dateStr = if (time.isNotEmpty()) {
            // Combine date and time
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.US)
            val parsedDate = inputFormat.parse("$date $time")
            outputFormat.format(parsedDate ?: Date())
        } else {
            // Date only
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate ?: Date())
        }
        dateStr
    } catch (e: Exception) {
        "Date TBA"
    }
}