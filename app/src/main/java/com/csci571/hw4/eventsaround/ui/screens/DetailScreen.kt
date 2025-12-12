package com.csci571.hw4.eventsaround.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.csci571.hw4.eventsaround.data.model.EventDetails
import com.csci571.hw4.eventsaround.ui.viewmodel.EventDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

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
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                // Swipe gesture to change tabs
                                detectHorizontalDragGestures { change, dragAmount ->
                                    change.consume()
                                    if (abs(dragAmount) > 50) {
                                        if (dragAmount < 0 && selectedTab < 2) {
                                            // Swipe left - next tab
                                            selectedTab++
                                        } else if (dragAmount > 0 && selectedTab > 0) {
                                            // Swipe right - previous tab
                                            selectedTab--
                                        }
                                    }
                                }
                            }
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
 * Artist tab content - Shows Spotify artist information
 */
@Composable
fun ArtistTab(event: EventDetails, viewModel: EventDetailsViewModel) {
    val context = LocalContext.current
    val artistData by viewModel.artistData.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val isLoadingArtist by viewModel.isLoadingArtist.collectAsState()

    // Check if this is a music event
    val isMusicEvent = event.classifications?.firstOrNull()?.segment?.name?.equals("Music", ignoreCase = true) == true

    // Load artist data when tab opens
    LaunchedEffect(event.id) {
        if (isMusicEvent) {
            event._embedded?.attractions?.firstOrNull()?.name?.let { artistName ->
                viewModel.loadArtistData(artistName)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            !isMusicEvent -> {
                // Not a music event
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No artist data",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }

            isLoadingArtist -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            artistData != null -> {
                val artist = artistData!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Artist card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Artist image
                            artist.images.firstOrNull()?.url?.let { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = artist.name,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Artist info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = artist.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // External link icon
                                    artist.external_urls.spotify?.let { spotifyUrl ->
                                        Icon(
                                            imageVector = Icons.Default.Launch,
                                            contentDescription = "Open in Spotify",
                                            tint = Color.Gray,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl))
                                                    context.startActivity(intent)
                                                }
                                        )
                                    }
                                }

                                // Followers and Popularity
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Followers: ${formatFollowersNumber(artist.followers.total)}",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )

                                    Text(
                                        text = "Popularity: ${artist.popularity}%",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }

                                // Genre
                                if (artist.genres.isNotEmpty()) {
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            Color.LightGray
                                        )
                                    ) {
                                        Text(
                                            text = artist.genres.first(),
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            ),
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Albums section
                    if (albums.isNotEmpty()) {
                        Text(
                            text = "Albums",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Grid of albums
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            albums.chunked(2).forEach { rowAlbums ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowAlbums.forEach { album ->
                                        AlbumCard(
                                            album = album,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                album.external_urls.spotify?.let { spotifyUrl ->
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl))
                                                    context.startActivity(intent)
                                                }
                                            }
                                        )
                                    }
                                    // Add empty space if odd number of albums
                                    if (rowAlbums.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No artist data available",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Album card component
 */
@Composable
fun AlbumCard(
    album: com.csci571.hw4.eventsaround.data.model.Album,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Album image
            album.images.firstOrNull()?.url?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = album.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Album name
            Text(
                text = album.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Release date and tracks
            Text(
                text = formatReleaseDate(album.release_date),
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(
                text = "${album.total_tracks} tracks",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * Format followers count with commas (e.g., 122,989,098)
 */
private fun formatFollowersNumber(count: Int): String {
    return String.format("%,d", count)
}

/**
 * Format followers count to K or M (for compact display if needed)
 */
private fun formatFollowers(count: Int): String {
    return when {
        count >= 1_000_000 -> {
            val millions = count / 1_000_000.0
            String.format("%.1fM", millions).replace(".0M", "M")
        }
        count >= 1_000 -> {
            val thousands = count / 1_000.0
            String.format("%.1fK", thousands).replace(".0K", "K")
        }
        else -> count.toString()
    }
}

/**
 * Format release date
 */
private fun formatReleaseDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        val year = parts[0]
        val month = if (parts.size > 1) parts[1] else "01"
        val day = if (parts.size > 2) parts[2] else "01"

        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputFormat.parse("$year-$month-$day")
        val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Venue tab content - Shows venue information from Ticketmaster with real images
 */
@Composable
fun VenueTab(event: EventDetails) {
    val context = LocalContext.current
    val venue = event._embedded?.venues?.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (venue != null) {
            // Venue card
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
                    // Venue cover image from Ticketmaster API
                    // Note: You need to update VenueDetails data class to include images and url fields
                    // For now, using a styled placeholder that looks like the SoFi Stadium logo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF00BCD4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Stadium/Venue icon
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Venue",
                                tint = Color.White,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = venue.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Venue name and external link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = venue.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        // External link icon - Opens venue's Ticketmaster page
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = "Open venue on Ticketmaster",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    // Use venue URL if available, otherwise search on Ticketmaster
                                    // NOTE: After updating VenueDetails to include url field, uncomment this:
                                    /*
                                    val venueUrl = venue.url ?: run {
                                        val venueName = Uri.encode(venue.name)
                                        "https://www.ticketmaster.com/search?q=$venueName"
                                    }
                                    */
                                    // For now, use search URL:
                                    val venueName = Uri.encode(venue.name)
                                    val ticketmasterUrl = "https://www.ticketmaster.com/search?q=$venueName"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ticketmasterUrl))
                                    context.startActivity(intent)
                                }
                        )
                    }

                    // Address
                    val address = buildString {
                        venue.address?.line1?.let { append(it) }
                        venue.city?.name?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                        venue.state?.stateCode?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                        if (isNotEmpty()) append(", US")
                    }

                    if (address.isNotEmpty()) {
                        Text(
                            text = address,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No venue information available",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
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