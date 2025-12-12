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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.csci571.hw4.eventsaround.data.model.EventClassification
import com.csci571.hw4.eventsaround.data.model.EventDetails
import com.csci571.hw4.eventsaround.ui.viewmodel.EventDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Details Screen - Shows comprehensive information about a single event
 * Now supports dark mode
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
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color.Yellow else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    Text(
                        text = error ?: "Unknown error",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                eventDetails != null -> {
                    val currentEventDetails = eventDetails!!

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Tab Row
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Details tab content
 */
@Composable
fun DetailsTab(event: EventDetails) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Event details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Ticketmaster link
                        event.url?.let { url ->
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = "Open in Ticketmaster",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Share button
                        IconButton(
                            onClick = {
                                event.url?.let { url ->
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, url)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share event"))
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            genres.forEach { genre ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Text(
                                        text = genre,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Price Ranges
                event.priceRanges?.firstOrNull()?.let { priceRange ->
                    DetailItem(
                        label = "Price Ranges",
                        value = "$${priceRange.min} - $${priceRange.max}"
                    )
                }

                // Ticket Status
                event.dates?.status?.code?.let { statusCode ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Ticket Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            color = getStatusColor(statusCode),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = formatStatusCode(statusCode),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Buy Ticket At
                event.url?.let { url ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Buy Ticket At",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ticketmaster",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }

        // Seatmap card
        event.seatmap?.staticUrl?.let { seatmapUrl ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AsyncImage(
                        model = seatmapUrl,
                        contentDescription = "Seatmap",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(8.dp)),
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
            .background(MaterialTheme.colorScheme.background)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    // Artist info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
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

                            // Artist details
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Artist name with external link
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = artist.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // External link icon
                                    artist.external_urls.spotify?.let { spotifyUrl ->
                                        Icon(
                                            imageVector = Icons.Default.Launch,
                                            contentDescription = "Open in Spotify",
                                            tint = MaterialTheme.colorScheme.primary,
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = "Popularity: ${artist.popularity}%",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Genre
                                if (artist.genres.isNotEmpty()) {
                                    Text(
                                        text = artist.genres.firstOrNull() ?: "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Albums section
                    if (albums.isNotEmpty()) {
                        Text(
                            text = "Albums",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Albums grid - 2 columns
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
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // Fill remaining space if odd number
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.clickable {
            album.external_urls.spotify?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Album cover image
            album.images.firstOrNull()?.url?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = album.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Album info
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = album.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatReleaseDate(album.release_date),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${album.total_tracks} tracks",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Format followers number to K/M format
 */
private fun formatFollowersNumber(followers: Int): String {
    return when {
        followers >= 1_000_000 -> String.format("%.1fM", followers / 1_000_000.0)
        followers >= 1_000 -> String.format("%.1fK", followers / 1_000.0)
        else -> followers.toString()
    }
}

/**
 * Format release date
 */
private fun formatReleaseDate(releaseDate: String): String {
    return try {
        val parts = releaseDate.split("-")
        when (parts.size) {
            3 -> {
                val year = parts[0]
                val month = parts[1].toIntOrNull()
                val day = parts[2]
                val monthName = when (month) {
                    1 -> "Jan"
                    2 -> "Feb"
                    3 -> "Mar"
                    4 -> "Apr"
                    5 -> "May"
                    6 -> "Jun"
                    7 -> "Jul"
                    8 -> "Aug"
                    9 -> "Sep"
                    10 -> "Oct"
                    11 -> "Nov"
                    12 -> "Dec"
                    else -> ""
                }
                "$monthName $day, $year"
            }
            1 -> parts[0] // Just year
            else -> releaseDate
        }
    } catch (e: Exception) {
        releaseDate
    }
}

/**
 * Venue tab content
 */
@Composable
fun VenueTab(event: EventDetails) {
    val context = LocalContext.current
    val venue = event._embedded?.venues?.firstOrNull()

    if (venue != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Venue image or placeholder
            venue.images?.firstOrNull()?.url?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = venue.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } ?: run {
                // Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Venue",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = venue.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Venue details card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = venue.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        venue.url?.let { url ->
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = "Open venue page",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Address
                    val addressParts = mutableListOf<String>()
                    venue.address?.line1?.let { addressParts.add(it) }

                    val cityStateZip = buildString {
                        venue.city?.name?.let { append(it) }
                        venue.state?.stateCode?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                        venue.postalCode?.let {
                            if (isNotEmpty()) append(" ")
                            append(it)
                        }
                    }

                    if (cityStateZip.isNotEmpty()) {
                        addressParts.add(cityStateZip)
                    }

                    if (addressParts.isNotEmpty()) {
                        Text(
                            text = addressParts.joinToString(", "),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Build genres list from classification
 */
private fun buildGenresList(classification: EventClassification?): List<String> {
    val genres = mutableListOf<String>()
    classification?.let {
        it.segment?.name?.let { name -> genres.add(name) }
        it.genre?.name?.let { name -> genres.add(name) }
        it.subGenre?.name?.let { name -> genres.add(name) }
        it.type?.name?.let { name -> genres.add(name) }
        it.subType?.name?.let { name -> genres.add(name) }
    }
    return genres.filter { it.isNotBlank() && it.lowercase() != "undefined" }.distinct()
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
        calendar.time = date ?: return "TBA"

        val dayOfWeek = SimpleDateFormat("EEE", Locale.US).format(calendar.time)
        val month = SimpleDateFormat("MMM", Locale.US).format(calendar.time)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)

        val formattedDate = "$dayOfWeek, $month $day, $year"

        if (timeString != null) {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
            val time = timeFormat.parse(timeString)
            time?.let {
                val outputTimeFormat = SimpleDateFormat("h:mm a", Locale.US)
                "$formattedDate, ${outputTimeFormat.format(it)}"
            } ?: formattedDate
        } else {
            formattedDate
        }
    } catch (e: Exception) {
        "TBA"
    }
}

/**
 * Format status code
 */
private fun formatStatusCode(code: String): String {
    return when (code.lowercase()) {
        "onsale" -> "On Sale"
        "offsale" -> "Off Sale"
        "canceled" -> "Canceled"
        "cancelled" -> "Cancelled"
        "postponed" -> "Postponed"
        "rescheduled" -> "Rescheduled"
        else -> code.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

/**
 * Get status color
 */
private fun getStatusColor(code: String): Color {
    return when (code.lowercase()) {
        "onsale" -> Color(0xFF4CAF50) // Green
        "offsale" -> Color(0xFFF44336) // Red
        "canceled", "cancelled" -> Color(0xFF9E9E9E) // Gray
        "postponed" -> Color(0xFFFF9800) // Orange
        "rescheduled" -> Color(0xFF2196F3) // Blue
        else -> Color(0xFF607D8B) // Blue Gray
    }
}