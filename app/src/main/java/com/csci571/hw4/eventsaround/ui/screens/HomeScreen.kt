package com.csci571.hw4.eventsaround.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onSearchClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // TODO: Replace with actual favorites from repository
    val favoriteEvents = remember { mutableStateListOf<FavoriteEvent>() }

    // Format current date as "11 November 2025"
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
                    modifier = Modifier.fillMaxWidth(),
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

            // Spacer to push "Powered by Ticketmaster" to bottom
            Spacer(modifier = Modifier.weight(1f))

            // "Powered by Ticketmaster" link at the bottom
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
 */
@Composable
fun FavoriteEventCard(
    event: FavoriteEvent,
    onClick: () -> Unit
) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.venue,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.dateTime,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Read-only star icon
            Icon(
                imageVector = Icons.Default.Star, // Filled star icon
                contentDescription = "Favorite",
                tint = Color(0xFFFFD700), // Gold color
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Data class for favorite event
 */
data class FavoriteEvent(
    val id: String,
    val name: String,
    val venue: String,
    val dateTime: String,
    val imageUrl: String = ""
)