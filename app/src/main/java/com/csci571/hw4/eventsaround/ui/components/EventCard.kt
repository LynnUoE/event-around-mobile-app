package com.csci571.hw4.eventsaround.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.csci571.hw4.eventsaround.data.model.Event

/**
 * EventCard component - displays event information in a card format
 * Used in ResultsScreen and HomeScreen (favorites list)
 *
 * @param event Event data object
 * @param isFavorite Whether the event is marked as favorite
 * @param onEventClick Callback when card is clicked
 * @param onFavoriteClick Callback when favorite button is clicked
 * @param showCategoryIcon Whether to show category icon (default: false)
 */
@Composable
fun EventCard(
    event: Event,
    isFavorite: Boolean,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (Event) -> Unit,
    showCategoryIcon: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEventClick(event.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event category icon or image
            if (showCategoryIcon) {
                CategoryIcon(
                    category = event.category,
                    modifier = Modifier.size(60.dp)
                )
            } else {
                // Event image
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.name,
                    modifier = Modifier
                        .size(80.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Event details column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Event name
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Venue name
                Text(
                    text = event.venue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Date and time
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDateTime(event.date, event.time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Category badge
                if (event.category.isNotEmpty()) {
                    Surface(
                        color = getCategoryColor(event.category),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = event.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Favorite toggle button
            IconButton(
                onClick = { onFavoriteClick(event) }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * Category icon component for displaying event category
 */
@Composable
fun CategoryIcon(
    category: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = getCategoryColor(category),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getCategoryInitial(category),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Get color for category badge based on category name
 */
@Composable
fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "music" -> Color(0xFF1DB954)           // Spotify Green
        "sports" -> Color(0xFF0066CC)          // Blue
        "arts & theatre" -> Color(0xFF9C27B0) // Purple
        "film" -> Color(0xFFE91E63)            // Pink
        "miscellaneous" -> Color(0xFF607D8B)   // Blue Grey
        else -> Color(0xFF757575)              // Grey
    }
}

/**
 * Get first letter of category for icon
 */
fun getCategoryInitial(category: String): String {
    return when (category.lowercase()) {
        "music" -> "M"
        "sports" -> "S"
        "arts & theatre" -> "A"
        "film" -> "F"
        "miscellaneous" -> "M"
        else -> "E"
    }
}

/**
 * Format date and time for display
 * Example: "Dec 15, 2024, 7:30 PM"
 */
fun formatDateTime(date: String, time: String): String {
    if (date.isEmpty()) return "Date TBA"

    return try {
        // Parse date: "2024-12-15"
        val dateParts = date.split("-")
        if (dateParts.size != 3) return date

        val year = dateParts[0]
        val month = dateParts[1].toInt()
        val day = dateParts[2].toInt()

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

        // Format time if available
        val timeFormatted = if (time.isNotEmpty()) {
            formatTime(time)
        } else {
            ""
        }

        if (timeFormatted.isNotEmpty()) {
            "$monthName $day, $year, $timeFormatted"
        } else {
            "$monthName $day, $year"
        }
    } catch (e: Exception) {
        date
    }
}

/**
 * Format time from 24-hour to 12-hour format
 * Example: "19:30:00" -> "7:30 PM"
 */
fun formatTime(time: String): String {
    return try {
        val parts = time.split(":")
        if (parts.isEmpty()) return time

        val hour = parts[0].toInt()
        val minute = parts.getOrNull(1)?.toInt() ?: 0

        val isPM = hour >= 12
        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        val period = if (isPM) "PM" else "AM"
        "$hour12:${minute.toString().padStart(2, '0')} $period"
    } catch (e: Exception) {
        time
    }
}