package com.csci571.hw4.eventsaround.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.csci571.hw4.eventsaround.data.model.Event
import com.csci571.hw4.eventsaround.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home Screen - Favorites List
 * Displays favorite events with "Powered by Ticketmaster" link
 * Content aligned to top (not centered)
 * Now supports dark mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    // Get favorites from ViewModel
    val favoriteEvents by viewModel.favorites.collectAsState()

    // Reload favorites when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    // Format current date as "11 December 2025"
    val currentDate = remember {
        SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(Date())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Event Search",
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
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
            // Current date display - at the top
            Text(
                text = currentDate,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // "Favorites" header
            Text(
                text = "Favorites",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Content area - aligned to top
            if (favoriteEvents.isEmpty()) {
                // Empty state - "No favorites" card at the top
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "No favorites",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // LazyColumn for favorite events
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteEvents) { event ->
                        FavoriteEventCard(
                            event = event,
                            onClick = { onEventClick(event.id) }
                        )
                    }
                }
            }

            // "Powered by Ticketmaster" link at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ticketmaster.com"))
                        context.startActivity(intent)
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Powered by Ticketmaster",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

/**
 * Favorite Event Card for Home Screen
 * Displays event thumbnail, name, date and time ago (updates in real-time)
 */
@Composable
private fun FavoriteEventCard(
    event: Event,
    onClick: () -> Unit
) {
    // State to trigger recomposition every second for time update
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Update current time every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L) // Update every 1 second
            currentTime = System.currentTimeMillis()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event thumbnail
            AsyncImage(
                model = event.images?.firstOrNull()?.url ?: "",
                contentDescription = event.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Event details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Event name
                Text(
                    text = event.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Event date and time
                Text(
                    text = formatEventDateTime(
                        event.dates?.start?.localDate,
                        event.dates?.start?.localTime
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Time ago and arrow (updates in real-time)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = getTimeAgo(event.favoritedAt ?: 0L, currentTime),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "View details",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Format event date and time for display
 */
private fun formatEventDateTime(dateString: String?, timeString: String?): String {
    return try {
        val formattedDate = if (!dateString.isNullOrEmpty()) {
            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val date = inputDateFormat.parse(dateString)
            date?.let { outputDateFormat.format(it) } ?: dateString
        } else {
            "Date TBA"
        }

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
        "Date TBA"
    }
}

/**
 * Calculate time ago from timestamp
 * Returns formatted string like "5 seconds ago", "2 minutes ago", etc.
 *
 * @param timestamp The timestamp when event was favorited
 * @param currentTime The current time (for real-time updates)
 */
private fun getTimeAgo(timestamp: Long, currentTime: Long = System.currentTimeMillis()): String {
    val diff = currentTime - timestamp

    return when {
        diff < 60_000 -> {
            val seconds = (diff / 1000).toInt()
            if (seconds <= 1) "1 second ago" else "$seconds seconds ago"
        }
        diff < 3_600_000 -> {
            val minutes = (diff / 60_000).toInt()
            if (minutes == 1) "1 minute ago" else "$minutes minutes ago"
        }
        diff < 86_400_000 -> {
            val hours = (diff / 3_600_000).toInt()
            if (hours == 1) "1 hour ago" else "$hours hours ago"
        }
        else -> {
            val days = (diff / 86_400_000).toInt()
            if (days == 1) "1 day ago" else "$days days ago"
        }
    }
}