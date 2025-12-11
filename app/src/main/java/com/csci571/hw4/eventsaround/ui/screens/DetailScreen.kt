package com.csci571.hw4.eventsaround.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Details Screen - Shows comprehensive information about a single event
 * Includes tabs for Details, Artist, and Venue information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    eventId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var isFavorite by remember { mutableStateOf(false) }
    val isLoading by remember { mutableStateOf(false) }

    // Use var instead of val for mutable state
    var eventDetails by remember { mutableStateOf<EventDetails?>(null) }

    LaunchedEffect(eventId) {
        // TODO: Fetch event details from API
        // eventDetails = repository.getEventDetails(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Share button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Check out this event: ${eventDetails?.name ?: "Unknown Event"}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share event"))
                        }
                    ) {
                        Icon(Icons.Default.Share, "Share")
                    }

                    // Favorite button
                    IconButton(
                        onClick = {
                            isFavorite = !isFavorite
                            // TODO: Toggle favorite in repository
                        }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Create local variable for smart cast
            val currentEventDetails = eventDetails

            if (currentEventDetails == null) {
                // Empty/Error state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Event not found")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ID: $eventId", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                // Content state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Event image
                    AsyncImage(
                        model = currentEventDetails.imageUrl,
                        contentDescription = currentEventDetails.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Tabs
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Details") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Artist") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Venue") }
                        )
                    }

                    // Tab content
                    when (selectedTab) {
                        0 -> DetailsTab(currentEventDetails)
                        1 -> ArtistTab(currentEventDetails)
                        2 -> VenueTab(currentEventDetails)
                    }
                }
            }
        }
    }
}

/**
 * Details tab content showing event information
 */
@Composable
fun DetailsTab(event: EventDetails) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Event name
        Text(
            text = event.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Date and time
        DetailRow("Date", event.date)
        DetailRow("Time", event.time)

        // Venue
        DetailRow("Venue", event.venueName)

        // Genre/Category
        if (event.genres.isNotEmpty()) {
            DetailRow("Genres", event.genres.joinToString(", "))
        }

        // Price range
        if (event.priceRange.isNotEmpty()) {
            DetailRow("Price Range", event.priceRange)
        }

        // Ticket status
        DetailRow("Ticket Status", event.ticketStatus)

        // Buy tickets button
        if (event.buyTicketUrl.isNotEmpty()) {
            val context = LocalContext.current
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.buyTicketUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("BUY TICKETS")
            }
        }

        // Seat map (if available)
        if (event.seatmapUrl.isNotEmpty()) {
            Text(
                text = "Seat Map",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            AsyncImage(
                model = event.seatmapUrl,
                contentDescription = "Seat map",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Artist tab content showing artist/team information
 */
@Composable
fun ArtistTab(event: EventDetails) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (event.artists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No artist information available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            event.artists.forEach { artist ->
                ArtistCard(artist)
            }
        }
    }
}

/**
 * Venue tab content showing venue information and map
 */
@Composable
fun VenueTab(event: EventDetails) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Venue name
        Text(
            text = event.venueName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Address
        if (event.venueAddress.isNotEmpty()) {
            DetailRow("Address", event.venueAddress)
        }

        // City, State
        DetailRow("Location", "${event.venueCity}, ${event.venueState}")

        // Phone number (if available)
        if (event.venuePhone.isNotEmpty()) {
            DetailRow("Phone", event.venuePhone)
        }

        // Open hours (if available)
        if (event.venueOpenHours.isNotEmpty()) {
            DetailRow("Hours", event.venueOpenHours)
        }

        // Google Maps button
        Button(
            onClick = {
                val mapUri = "geo:0,0?q=${event.venueAddress}, ${event.venueCity}, ${event.venueState}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUri))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("OPEN IN MAPS")
        }

        // TODO: Add Google Maps embed
    }
}

/**
 * Reusable detail row component
 */
@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Artist card component
 */
@Composable
fun ArtistCard(artist: ArtistInfo) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Artist image
            if (artist.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = artist.imageUrl,
                    contentDescription = artist.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Artist name
            Text(
                text = artist.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Followers count (Spotify)
            if (artist.followers > 0) {
                Text(
                    text = "${artist.followers} followers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Popularity
            if (artist.popularity > 0) {
                Text(
                    text = "Popularity: ${artist.popularity}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Spotify link
            if (artist.spotifyUrl.isNotEmpty()) {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(artist.spotifyUrl))
                        context.startActivity(intent)
                    }
                ) {
                    Text("View on Spotify")
                }
            }
        }
    }
}

/**
 * Data classes for event details
 * TODO: Move to data/model package
 */
data class EventDetails(
    val id: String,
    val name: String,
    val date: String,
    val time: String,
    val venueName: String,
    val venueAddress: String,
    val venueCity: String,
    val venueState: String,
    val venuePhone: String = "",
    val venueOpenHours: String = "",
    val genres: List<String> = emptyList(),
    val priceRange: String = "",
    val ticketStatus: String = "",
    val buyTicketUrl: String = "",
    val seatmapUrl: String = "",
    val imageUrl: String = "",
    val artists: List<ArtistInfo> = emptyList()
)

data class ArtistInfo(
    val name: String,
    val imageUrl: String = "",
    val followers: Int = 0,
    val popularity: Int = 0,
    val spotifyUrl: String = ""
)