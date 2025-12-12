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
import androidx.compose.ui.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home Screen - Favorites List
 * Displays favorite events with "Powered by Ticketmaster" link
 * Content aligned to top (not centered)
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
                    containerColor = Color(0xFFE3F2FD) // Light blue background
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
                color = Color.Gray
            )

            // "Favorites" header
            Text(
                text = "Favorites",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
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
                        color = Color(0xFFE3F2FD), // Light blue background
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "No favorites",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Favorites list - starts from top
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Take available space
                    contentPadding = PaddingValues(16.dp),
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

            // "Powered by Ticketmaster" link - always at the bottom of content
            Text(
                text = "Powered by Ticketmaster",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.ticketmaster.com")
                        )
                        context.startActivity(intent)
                    }
                    .padding(vertical = 16.dp),
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

/**
 * Favorite event card component
 * Displays event info with image, time ago, and arrow icon
 */
@Composable
fun FavoriteEventCard(
    event: Event,
    onClick: () -> Unit
) {
    // State for dynamic time updates
    var timeAgo by remember { mutableStateOf(calculateTimeAgo(event.favoritedAt ?: System.currentTimeMillis())) }

    // Update time every second
    LaunchedEffect(event.favoritedAt) {
        while (true) {
            kotlinx.coroutines.delay(1000) // Update every 1 second
            timeAgo = calculateTimeAgo(event.favoritedAt ?: System.currentTimeMillis())
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event image (left side)
            AsyncImage(
                model = event.images?.firstOrNull()?.url ?: "",
                contentDescription = event.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Event details (middle)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Event name
                Text(
                    text = event.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date and time
                Text(
                    text = formatEventDateTime(event),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Time ago and arrow (right side)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeAgo,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "View details",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Calculate time elapsed since event was favorited
 */
private fun calculateTimeAgo(favoritedTimestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - favoritedTimestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "$seconds second${if (seconds == 1L) "" else "s"} ago"
        minutes < 60 -> "$minutes minute${if (minutes == 1L) "" else "s"} ago"
        hours < 24 -> "$hours hour${if (hours == 1L) "" else "s"} ago"
        else -> "$days day${if (days == 1L) "" else "s"} ago"
    }
}

/**
 * Format event date and time for display
 */
private fun formatEventDateTime(event: Event): String {
    return try {
        val dateString = event.dates?.start?.localDate
        val timeString = event.dates?.start?.localTime

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
        "Date TBA"
    }
}