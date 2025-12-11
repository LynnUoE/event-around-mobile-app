package com.csci571.hw4.eventsaround.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.csci571.hw4.eventsaround.data.model.EventDetails
import com.csci571.hw4.eventsaround.ui.viewmodel.EventDetailsViewModel
import androidx.compose.foundation.clickable

/**
 * Details Screen - Shows comprehensive information about a single event
 * Includes tabs for Details, Artist, and Venue information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    viewModel: EventDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

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
                title = { Text(eventDetails?.name ?: "Event Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Favorite button
                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        enabled = eventDetails != null
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val shareText = eventDetails?.let { event ->
                                "Check out this event: ${event.name}\n${event.url ?: ""}"
                            } ?: "Check out this event!"

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Event"))
                        },
                        enabled = eventDetails != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
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
                    // Show loading indicator
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
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.clearError()
                            viewModel.loadEventDetails(eventId)
                        }) {
                            Text("Retry")
                        }
                    }
                }

                eventDetails != null -> {
                    // Show event details
                    val currentEventDetails = eventDetails!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        // Event image
                        AsyncImage(
                            model = currentEventDetails.images?.firstOrNull()?.url ?: "",
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
                            1 -> ArtistTab(currentEventDetails, viewModel)
                            2 -> VenueTab(currentEventDetails)
                        }
                    }
                }

                else -> {
                    // Fallback - should not happen
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
        event.dates?.start?.let { start ->
            DetailRow("Date", start.localDate ?: "TBA")
            DetailRow("Time", start.localTime ?: "TBA")
        }

        // Venue
        event._embedded?.venues?.firstOrNull()?.let { venue ->
            DetailRow("Venue", venue.name)
        }

        // Category/Genre
        event.classifications?.firstOrNull()?.let { classification ->
            classification.segment?.name?.let { segment ->
                DetailRow("Category", segment)
            }
            classification.genre?.name?.let { genre ->
                DetailRow("Genre", genre)
            }
        }

        // Price range
        event.priceRanges?.firstOrNull()?.let { priceRange ->
            val priceText = when {
                priceRange.min != null && priceRange.max != null ->
                    "$${priceRange.min} - $${priceRange.max}"
                priceRange.min != null -> "From $${priceRange.min}"
                else -> "Check website"
            }
            DetailRow("Price Range", priceText)
        }

        // Ticket status
        event.dates?.status?.code?.let { status ->
            DetailRow("Status", status.uppercase())
        }

        // Seatmap
        event.seatmap?.staticUrl?.let { seatmapUrl ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Seatmap",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            AsyncImage(
                model = seatmapUrl,
                contentDescription = "Seatmap",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Buy tickets button
        event.url?.let { url ->
            Button(
                onClick = {
                    // Open URL in browser
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buy Tickets")
            }
        }
    }
}

/**
 * Artist tab content
 */
@Composable
fun ArtistTab(event: EventDetails, viewModel: EventDetailsViewModel) {
    val artistData by viewModel.artistData.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val isLoadingArtist by viewModel.isLoadingArtist.collectAsState()

    // Load artist data when tab opens
    LaunchedEffect(Unit) {
        event._embedded?.attractions?.firstOrNull()?.let { attraction ->
            viewModel.loadArtistData(attraction.name)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            isLoadingArtist -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            artistData != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Artist info
                    Text(
                        text = artistData!!.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    artistData!!.followers?.total?.let { followers ->
                        Text("Followers: ${formatFollowers(followers)}")
                    }

                    artistData!!.popularity?.let { popularity ->
                        Text("Popularity: $popularity%")
                    }

                    // Albums
                    if (albums.isNotEmpty()) {
                        Text(
                            text = "Albums",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        albums.forEach { album ->
                            AlbumCard(album)
                        }
                    }
                }
            }

            else -> {
                Text(
                    text = "No artist data available",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * Venue tab content
 */
@Composable
fun VenueTab(event: EventDetails) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        event._embedded?.venues?.firstOrNull()?.let { venue ->
            Text(
                text = venue.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Address
            venue.address?.line1?.let { address ->
                DetailRow("Address", address)
            }

            // City and State
            val location = buildString {
                venue.city?.name?.let { append(it) }
                venue.state?.stateCode?.let {
                    if (isNotEmpty()) append(", ")
                    append(it)
                }
            }
            if (location.isNotEmpty()) {
                DetailRow("Location", location)
            }

            // Coordinates
            venue.location?.let { loc ->
                if (loc.latitude.isNotEmpty() && loc.longitude.isNotEmpty()) {
                    DetailRow("Coordinates", "${loc.latitude}, ${loc.longitude}")
                }
            }
        } ?: run {
            Text(
                text = "No venue information available",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * Helper composable for detail rows
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
 * Album card composable - clickable to open in Spotify
 */
@Composable
fun AlbumCard(album: com.csci571.hw4.eventsaround.data.model.Album) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Open album in Spotify when clicked
                album.external_urls.spotify?.let { spotifyUrl ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("AlbumCard", "Failed to open Spotify URL", e)
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = album.images?.firstOrNull()?.url ?: "",
                contentDescription = album.name,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = album.release_date ?: "Unknown",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Format followers count (e.g., 1.2M, 345K)
 */
fun formatFollowers(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}