package com.csci571.hw4.eventsaround.util
object Constants {

    const val BASE_URL = "https://csci571-472400.wl.r.appspot.com/"

    // API endpoints
    const val SEARCH_EVENTS = "api/events/search"
    const val EVENT_DETAILS = "api/events/{id}"
    const val SUGGEST_EVENTS = "api/events/suggest"
    const val VENUE_DETAILS = "api/events/venue/{name}"

    // Preferences keys
    const val FAVORITES_KEY = "favorites"
    const val PREFERENCES_NAME = "event_finder_prefs"

    // Categories
    val CATEGORIES = listOf(
        "All",
        "Music",
        "Sports",
        "Arts & Theatre",
        "Film",
        "Miscellaneous"
    )

    // Segment IDs for categories
    val CATEGORY_SEGMENT_MAP = mapOf(
        "All" to "",
        "Music" to "KZFzniwnSyZfZ7v7nJ",
        "Sports" to "KZFzniwnSyZfZ7v7nE",
        "Arts & Theatre" to "KZFzniwnSyZfZ7v7na",
        "Film" to "KZFzniwnSyZfZ7v7nn",
        "Miscellaneous" to "KZFzniwnSyZfZ7v7n1"
    )
}