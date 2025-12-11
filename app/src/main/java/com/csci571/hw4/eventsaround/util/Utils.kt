package com.csci571.hw4.eventsaround.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility object for common helper functions
 */
object Utils {

    /**
     * Format date from "2024-12-15" to "Dec 15, 2024"
     * Include year only if not current year
     */
    fun formatEventDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = inputFormat.parse(dateString) ?: return dateString

            val calendar = Calendar.getInstance()
            calendar.time = date
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val eventYear = calendar.get(Calendar.YEAR)

            val outputFormat = if (eventYear == currentYear) {
                SimpleDateFormat("MMM d", Locale.US)
            } else {
                SimpleDateFormat("MMM d, yyyy", Locale.US)
            }

            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Format time from "19:30:00" to "7:30 PM"
     */
    fun formatEventTime(timeString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
            val time = inputFormat.parse(timeString) ?: return timeString

            val outputFormat = SimpleDateFormat("h:mm a", Locale.US)
            outputFormat.format(time)
        } catch (e: Exception) {
            timeString
        }
    }

    /**
     * Combine date and time formatting
     * Example: "Dec 15, 7:30 PM"
     */
    fun formatDateTime(dateString: String, timeString: String?): String {
        val formattedDate = formatEventDate(dateString)
        val formattedTime = timeString?.let { formatEventTime(it) }

        return if (formattedTime != null) {
            "$formattedDate, $formattedTime"
        } else {
            formattedDate
        }
    }

    /**
     * Get current date formatted as "14 November 2025"
     */
    fun getCurrentDateFormatted(): String {
        val format = SimpleDateFormat("d MMMM yyyy", Locale.US)
        return format.format(Date())
    }

    /**
     * Get Material Icon for event category
     */
    fun getCategoryIcon(category: String): ImageVector {
        return when (category.lowercase()) {
            "music" -> Icons.Default.MusicNote
            "sports" -> Icons.Default.SportsBasketball
            "arts & theatre", "arts", "theatre" -> Icons.Default.TheaterComedy
            "film" -> Icons.Default.Movie
            else -> Icons.Default.Category
        }
    }

    /**
     * Map category name to Ticketmaster segment ID
     */
    fun getCategorySegmentId(category: String): String {
        return when (category) {
            "Music" -> "KZFzniwnSyZfZ7v7nJ"
            "Sports" -> "KZFzniwnSyZfZ7v7nE"
            "Arts & Theatre" -> "KZFzniwnSyZfZ7v7na"
            "Film" -> "KZFzniwnSyZfZ7v7nn"
            "Miscellaneous" -> "KZFzniwnSyZfZ7v7n1"
            else -> "Default"
        }
    }

    /**
     * Open URL in external browser
     */
    fun openInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Share event via Android share sheet
     */
    fun shareEvent(context: Context, eventName: String, eventUrl: String) {
        try {
            val shareText = "Check out $eventName on Ticketmaster:\n$eventUrl"
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, "Share Event"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Format followers count as M (millions) or K (thousands)
     */
    fun formatFollowersCount(count: Int): String {
        return when {
            count >= 1_000_000 -> {
                val millions = count / 1_000_000.0
                String.format("%.1fM", millions)
            }
            count >= 1_000 -> {
                val thousands = count / 1_000.0
                String.format("%.1fK", thousands)
            }
            else -> count.toString()
        }
    }

    /**
     * Calculate time elapsed since favorite was added
     * Returns formatted string like "2 hours ago", "3 days ago"
     */
    fun getTimeElapsed(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
            else -> "Just now"
        }
    }

    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Check if string is a valid number
     */
    fun isValidNumber(str: String): Boolean {
        return str.toIntOrNull() != null
    }
}

/**
 * Extension function for Int - format as price
 */
fun Int.formatAsPrice(): String {
    return "$$this"
}

/**
 * Extension function for String - capitalize first letter of each word
 */
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}