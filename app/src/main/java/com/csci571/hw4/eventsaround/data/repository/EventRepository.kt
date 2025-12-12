package com.csci571.hw4.eventsaround.data.repository

import android.util.Log
import com.csci571.hw4.eventsaround.data.model.*
import com.csci571.hw4.eventsaround.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.csci571.hw4.eventsaround.data.remote.GooglePlacesService
import com.csci571.hw4.eventsaround.data.remote.IPInfoService

/**
 * EventRepository - Handles all event-related data operations
 * Singleton pattern to ensure single instance across app
 * Communicates with backend API for:
 * - Event search
 * - Event details
 * - Autocomplete suggestions
 * - Location services (geocoding, IP-based location)
 * - Spotify artist/album data
 * - Venue details
 */
class EventRepository private constructor() {

    private val TAG = "EventRepository"
    private val apiService = RetrofitClient.getApiService()

    companion object {
        @Volatile
        private var instance: EventRepository? = null

        /**
         * Get singleton instance of EventRepository
         */
        fun getInstance(): EventRepository {
            return instance ?: synchronized(this) {
                instance ?: EventRepository().also {
                    instance = it
                }
            }
        }
    }
    /**
     * Get location suggestions using Google Places API (direct call from app)
     * @param query User's text input (minimum 2 characters)
     * @return Result containing list of location suggestions
     */
    suspend fun getLocationSuggestions(query: String): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                if (query.length < 2) {
                    return@withContext Result.success(emptyList())
                }

                Log.d(TAG, "Getting location suggestions for: $query")

                // Use GooglePlacesService singleton
                val placesService = GooglePlacesService.getInstance()
                val suggestions = placesService.getLocationSuggestions(query)

                Log.d(TAG, "Retrieved ${suggestions.size} location suggestions")
                Result.success(suggestions)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting location suggestions", e)
                Result.failure(e)
            }
        }
    }


    /**
     * Search for events with given parameters
     */
    suspend fun searchEvents(params: SearchParams): Result<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Searching events: ${params.keyword}")

                val response = apiService.searchEvents(params.toQueryMap())

                if (response.isSuccessful) {
                    val events = response.body()?.embedded?.events ?: emptyList()
                    Log.d(TAG, "Found ${events.size} events")
                    Result.success(events)
                } else {
                    Result.failure(
                        Exception("API Error: ${response.code()} ${response.message()}")
                    )
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
                Log.d(TAG, "Fetching event details: $eventId")

                val response = apiService.getEventDetails(eventId)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else if (response.code() == 404) {
                    Result.failure(Exception("Event details not found"))
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
     * Returns list of suggestion strings
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
     * Get current location based on IP address (direct call from app)
     * Uses IPInfo service
     * @return Result containing Pair of (latitude, longitude)
     */
    suspend fun getCurrentLocation(): Result<Pair<Double, Double>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting current location from IP")

                // Use IPInfoService singleton
                val ipInfoService = IPInfoService.getInstance()
                val location = ipInfoService.getCurrentLocation()

                Log.d(TAG, "Current location: (${location.first}, ${location.second})")
                Result.success(location)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting current location", e)
                // Return default LA coordinates
                Result.success(Pair(SearchParams.DEFAULT_LAT, SearchParams.DEFAULT_LNG))
            }
        }
    }

    /**
     * Geocode a location string to coordinates (direct call from app)
     * Uses Google Geocoding API
     * @param address Location string to geocode (e.g., "Boston, MA, USA")
     * @return Result containing Pair of (latitude, longitude)
     */
    suspend fun geocodeLocation(address: String): Result<Pair<Double, Double>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Geocoding address: $address")

                // Use GooglePlacesService singleton
                val placesService = GooglePlacesService.getInstance()
                val location = placesService.geocodeAddress(address)

                if (location != null) {
                    Log.d(TAG, "Geocoded to: (${location.first}, ${location.second})")
                    Result.success(location)
                } else {
                    Log.w(TAG, "Failed to geocode address, using default coordinates")
                    Result.success(Pair(SearchParams.DEFAULT_LAT, SearchParams.DEFAULT_LNG))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error geocoding address", e)
                Result.success(Pair(SearchParams.DEFAULT_LAT, SearchParams.DEFAULT_LNG))
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
                    } ?: Result.success(emptyList())
                } else if (response.code() == 404) {
                    Result.success(emptyList())
                } else {
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
     * Get venue details
     */
    suspend fun getVenueDetails(venueName: String): Result<VenueDetails?> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching venue details: $venueName")

                val response = apiService.getVenueDetails(venueName)

                if (response.isSuccessful) {
                    Result.success(response.body())
                } else if (response.code() == 404) {
                    Result.success(null)
                } else {
                    Result.failure(
                        Exception("Venue API Error: ${response.code()}")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Venue details error", e)
                Result.failure(e)
            }
        }
    }
}