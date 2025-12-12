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
    viewModel: SearchViewModel = viewModel()
) {
    // Collect states from ViewModel
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Get last search params from ViewModel
    val lastSearchParams = viewModel.getLastSearchParams()

    // Category tabs state
    val categories = listOf("All", "Music", "Sports", "Arts & Theatre", "Film", "Miscellaneous")
    var selectedCategory by remember { mutableIntStateOf(0) }

    // Distance state for display
    var distance by remember { mutableIntStateOf(lastSearchParams?.distance ?: 10) }
    var useCurrentLocation by remember { mutableStateOf(lastSearchParams?.autoDetect ?: true) }

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
                            text = if (useCurrentLocation) "Current Location" else "Other",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Distance controls with swap icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Swap",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = distance.toString(),
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.widthIn(min = 20.dp),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "mi",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }

                // Category tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedCategory,
                    containerColor = Color(0xFFE3F2FD),
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedCategory == index,
                            onClick = { selectedCategory = index },
                            text = {
                                Text(
                                    text = category,
                                    fontSize = 14.sp,
                                    color = if (selectedCategory == index) Color(0xFF1976D2) else Color.Gray,
                                    fontWeight = if (selectedCategory == index) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFBDBDBD), thickness = 1.dp)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF1976D2)
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
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }

                filteredResults.isEmpty() -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(filteredResults) { event ->
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
 * Event card component with large image on top
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C3E50)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Large event image at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = "Event image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Category badge on top left
                if (event.category.isNotEmpty()) {
                    Surface(
                        color = getCategoryColor(event.category),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = event.category,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Date and time badge on top right
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = formatDateTime(event.date, event.time),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black,
                        fontSize = 12.sp
                    )
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
                        text = event.venue.ifEmpty { "Venue TBA" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp
                    )
                }

                // Favorite star button
                IconButton(
                    onClick = { isFavorite = !isFavorite },
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
private fun formatDateTime(dateString: String, timeString: String?): String {
    return try {
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

/**
 * Get category color based on category name
 */
private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "music" -> Color(0xFF9C27B0)
        "sports" -> Color(0xFF4CAF50)
        "arts & theatre", "arts" -> Color(0xFFFF5722)
        "film" -> Color(0xFF2196F3)
        "miscellaneous" -> Color(0xFF607D8B)
        else -> Color(0xFF757575)
    }
}