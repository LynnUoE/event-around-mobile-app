package com.csci571.hw4.eventsaround.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.csci571.hw4.eventsaround.data.model.EventDetails
import com.csci571.hw4.eventsaround.ui.viewmodel.EventDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Details Screen - Shows comprehensive information about a single event
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    viewModel: EventDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Collect states from ViewModel
    val eventDetails by viewModel.eventDetails.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isLoading by viewModel.isLoadingDetails.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load event details when screen opens
    LaunchedEffect(eventId) {
        viewModel.loadEventDetails(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = eventDetails?.name ?: "",
                        fontSize = 18.sp,
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
                    // Favorite button only
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE3F2FD)
                )
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
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }

                eventDetails != null -> {
                    val currentEventDetails = eventDetails!!

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Tabs with icons
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.White
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Details"
                                    )
                                },
                                text = { Text("Details") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Artist"
                                    )
                                },
                                text = { Text("Artist") }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Venue"
                                    )
                                },
                                text = { Text("Venue") }
                            )
                        }

                        // Tab content
                        when (selectedTab) {
                            0 -> DetailsTab(currentEventDetails)
                            1 -> ArtistTab(currentEventDetails, viewModel)
                            2 -> VenueTab(currentEventDetails)
                        }
                    }
                }

                else -> {
                    Text(
                        text = "No event data available",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * Details tab content - Card-based design like Figure 11
 */
@Composable
fun DetailsTab(event: EventDetails) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Event details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Event title with icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Event",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // External link icon
                        event.url?.let { url ->
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = "Open event link",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                            )
                        }

                        // Share icon
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    event.url?.let { url ->
                                        val shareIntent = Intent.createChooser(
                                            Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, url)
                                                type = "text/plain"
                                            },
                                            "Share event"
                                        )
                                        context.startActivity(shareIntent)
                                    }
                                }
                        )
                    }
                }

                HorizontalDivider(color = Color.LightGray)

                // Date
                DetailItem(
                    label = "Date",
                    value = formatEventDateTime(
                        event.dates?.start?.localDate,
                        event.dates?.start?.localTime
                    )
                )

                // Artists
                event._embedded?.attractions?.let { attractions ->
                    if (attractions.isNotEmpty()) {
                        DetailItem(
                            label = "Artists",
                            value = attractions.joinToString(", ") { it.name }
                        )
                    }
                }

                // Venue
                event._embedded?.venues?.firstOrNull()?.let { venue ->
                    DetailItem(label = "Venue", value = venue.name)
                }

                // Genres
                val genres = buildGenresList(event.classifications?.firstOrNull())
                if (genres.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Genres",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            genres.forEach { genre ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color.LightGray
                                    )
                                ) {
                                    Text(
                                        text = genre,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Ticket Status
                event.dates?.status?.code?.let { statusCode ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Ticket Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Surface(
                            color = getStatusColor(statusCode),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = formatStatus(statusCode),
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Seatmap card
        event.seatmap?.staticUrl?.let { seatmapUrl ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Seatmap",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    AsyncImage(
                        model = seatmapUrl,
                        contentDescription = "Seatmap",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

/**
 * Artist tab content
 */
@Composable
fun ArtistTab(event: EventDetails, viewModel: EventDetailsViewModel) {
    // TODO: Implement artist information
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Artist information will be displayed here")
    }
}

/**
 * Venue tab content
 */
@Composable
fun VenueTab(event: EventDetails) {
    // TODO: Implement venue information
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Venue information will be displayed here")
    }
}

/**
 * Detail item component
 */
@Composable
fun DetailItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

/**
 * Format event date and time
 */
private fun formatEventDateTime(dateString: String?, timeString: String?): String {
    if (dateString == null) return "TBA"

    return try {
        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputDateFormat.parse(dateString)

        val calendar = Calendar.getInstance()
        calendar.time = date ?: return dateString

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val eventYear = calendar.get(Calendar.YEAR)

        // Format: "Aug 8, 2026, 5:30 PM" or "Dec 3, 5:00 PM" (omit year if current)
        val dateFormat = if (eventYear == currentYear) {
            SimpleDateFormat("MMM d", Locale.US)
        } else {
            SimpleDateFormat("MMM d, yyyy", Locale.US)
        }

        val formattedDate = dateFormat.format(date)

        if (!timeString.isNullOrEmpty()) {
            try {
                val inputTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
                val time = inputTimeFormat.parse(timeString)
                val outputTimeFormat = SimpleDateFormat("h:mm a", Locale.US)
                val formattedTime = time?.let { outputTimeFormat.format(it) } ?: ""
                "$formattedDate, $formattedTime"
            } catch (e: Exception) {
                formattedDate
            }
        } else {
            formattedDate
        }
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Build genres list from classification - remove duplicates and undefined
 */
private fun buildGenresList(classification: com.csci571.hw4.eventsaround.data.model.EventClassification?): List<String> {
    if (classification == null) return emptyList()

    val genres = mutableListOf<String>()

    // Add each genre type if it exists and is not empty/undefined
    classification.segment?.name?.let {
        if (it.isNotBlank() && it.lowercase() != "undefined") {
            genres.add(it)
        }
    }
    classification.genre?.name?.let {
        if (it.isNotBlank() && it.lowercase() != "undefined") {
            genres.add(it)
        }
    }
    classification.subGenre?.name?.let {
        if (it.isNotBlank() && it.lowercase() != "undefined") {
            genres.add(it)
        }
    }
    classification.type?.name?.let {
        if (it.isNotBlank() && it.lowercase() != "undefined") {
            genres.add(it)
        }
    }
    classification.subType?.name?.let {
        if (it.isNotBlank() && it.lowercase() != "undefined") {
            genres.add(it)
        }
    }

    // Remove duplicates while preserving order
    return genres.distinct()
}

/**
 * Get status color based on status code
 */
private fun getStatusColor(statusCode: String): Color {
    return when (statusCode.lowercase()) {
        "onsale" -> Color(0xFF1976D2) // Primary
        "offsale" -> Color(0xFF757575) // Secondary
        "canceled", "cancelled" -> Color(0xFFD32F2F) // Error
        "postponed" -> Color(0xFFF57C00) // Warning
        else -> Color.Gray
    }
}

/**
 * Format status text
 */
private fun formatStatus(statusCode: String): String {
    return when (statusCode.lowercase()) {
        "onsale" -> "On Sale"
        "offsale" -> "Off Sale"
        "canceled" -> "Canceled"
        "postponed" -> "Postponed"
        else -> statusCode.uppercase()
    }
}