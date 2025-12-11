package com.csci571.hw4.eventsaround.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.csci571.hw4.eventsaround.data.remote.RetrofitClient
import com.csci571.hw4.eventsaround.data.model.*

/**
 * Repository for event-related API operations
 * Singleton pattern to ensure single instance across app
 */
class EventRepository private constructor() {

    private val apiService = RetrofitClient.getApiService()
    private val TAG = "EventRepository"

    /**
     * Search for events based on search parameters
     */
    suspend fun searchEvents(params: SearchParams): Result<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!params.isValid()) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Invalid search parameters")
                    )
                }

                Log.d(TAG, "Searching with params: ${params.toQueryMap()}")

                val response = apiService.searchEvents(params.toQueryMap())

                if (response.isSuccessful) {
                    val events = response.body()?.embedded?.events ?: emptyList()
                    Log.d(TAG, "Search successful: Found ${events.size} events")
                    Result.success(events)
                } else {
                    val error = "API Error: ${response.code()} ${response.message()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Search error", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get detailed information for a specific event
     */
    suspend fun getEventDetails(eventId: String): Result<EventDetails> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching details for event: $eventId")

                val response = apiService.getEventDetails(eventId)

                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d(TAG, "Event details fetched successfully")
                        Result.success(it)
                    } ?: Result.failure(Exception("Event details not found"))
                } else {
                    Result.failure(
                        Exception("API Error: ${response.code()} ${response.message()}")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Event details error", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get autocomplete suggestions for keyword search
     */
    suspend fun getAutocompleteSuggestions(keyword: String): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                if (keyword.isBlank()) {
                    return@withContext Result.success(emptyList())
                }

                Log.d(TAG, "Fetching autocomplete for: $keyword")

                val response = apiService.getAutocompleteSuggestions(keyword)

                if (response.isSuccessful) {
                    // Extract attraction names from the response
                    val suggestions = response.body()?._embedded?.attractions
                        ?.map { it.name } ?: emptyList()
                    Log.d(TAG, "Autocomplete: Found ${suggestions.size} suggestions")
                    Result.success(suggestions)
                } else {
                    Result.failure(
                        Exception("API Error: ${response.code()}")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Autocomplete error", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Search for artist on Spotify
     * Backend returns the artist object directly, not a search response
     */
    suspend fun searchSpotifyArtist(artistName: String): Result<SpotifyArtist?> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Searching Spotify for artist: $artistName")

                val response = apiService.searchSpotifyArtist(artistName)

                if (response.isSuccessful) {
                    val artist = response.body()
                    if (artist != null) {
                        Log.d(TAG, "Artist found on Spotify: ${artist.name}")
                        Result.success(artist)
                    } else {
                        Log.d(TAG, "No artist found on Spotify")
                        Result.success(null)
                    }
                } else if (response.code() == 404) {
                    // Artist not found - this is not an error
                    Log.d(TAG, "Artist not found on Spotify (404)")
                    Result.success(null)
                } else {
                    Result.failure(
                        Exception("Spotify API Error: ${response.code()}")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Spotify artist search error", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get artist albums from Spotify
     */
    suspend fun getArtistAlbums(artistId: String): Result<List<Album>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching albums for artist: $artistId")

                val response = apiService.getArtistAlbums(artistId)

                if (response.isSuccessful) {
                    response.body()?.let { albumsResponse ->
                        val albums = albumsResponse.items
                        Log.d(TAG, "Found ${albums.size} albums")
                        Result.success(albums)
                    } ?: Result.failure(Exception("Albums not found"))
                } else {
                    Log.e(TAG, "Failed to fetch albums: ${response.code()}")
                    Result.failure(
                        Exception("Spotify API Error: ${response.code()}")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Albums fetch error", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get venue details (if your backend provides this)
     * This is optional - Google Maps doesn't need extra venue details
     */
    suspend fun getVenueDetails(venueName: String): Result<VenueDetails?> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching venue details: $venueName")

                val response = apiService.getVenueDetails(venueName)

                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d(TAG, "Venue details fetched")
                        Result.success(it)
                    } ?: Result.success(null)
                } else {
                    // Don't fail - venue details are optional
                    Log.w(TAG, "Venue details not available: ${response.code()}")
                    Result.success(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Venue details error", e)
                // Don't fail - just return null
                Result.success(null)
            }
        }
    }

    /**
     * Get current location from IP (fallback method)
     */
    suspend fun getCurrentLocationFromIP(): Result<IPLocation?> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting location from IP")

                // If your backend has an IPInfo endpoint, use it
                // Otherwise return null and use device location
                Result.success(null)
            } catch (e: Exception) {
                Log.e(TAG, "IP location error", e)
                Result.success(null)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: EventRepository? = null

        /**
         * Get singleton instance of EventRepository
         */
        fun getInstance(): EventRepository {
            return instance ?: synchronized(this) {
                instance ?: EventRepository().also { instance = it }
            }
        }
    }
}