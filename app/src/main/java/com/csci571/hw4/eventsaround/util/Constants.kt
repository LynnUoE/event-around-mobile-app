package com.csci571.hw4.eventsaround.util
object Constants {
    const val BASE_URL = "https://csci571-472400.wl.r.appspot.com/"
    const val DEBUG_MODE = true

    // API Endpoints
    const val ENDPOINT_SEARCH = "api/search"
    const val ENDPOINT_EVENT_DETAILS = "api/events"
    const val ENDPOINT_AUTOCOMPLETE = "api/autocomplete"

    // Network
    const val TIMEOUT_SECONDS = 30L
    const val RETRY_COUNT = 3

    // Cache
    const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB

    // UI
    const val SPLASH_DELAY_MS = 2000L
    const val DEBOUNCE_DELAY_MS = 300L
}