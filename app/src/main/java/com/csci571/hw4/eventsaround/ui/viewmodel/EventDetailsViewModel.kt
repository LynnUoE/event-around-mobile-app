package com.csci571.hw4.eventsaround.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.csci571.hw4.eventsaround.data.model.*
import com.csci571.hw4.eventsaround.data.repository.EventRepository
import com.csci571.hw4.eventsaround.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Event Details Screen
 * Manages event details, artist info, venue info, and favorite status
 */
class EventDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val eventRepository = EventRepository.getInstance()
    private val favoritesRepository = FavoritesRepository.getInstance(application)
    private val TAG = "EventDetailsViewModel"

    // Event details state
    private val _eventDetails = MutableStateFlow<EventDetails?>(null)
    val eventDetails: StateFlow<EventDetails?> = _eventDetails.asStateFlow()

    // Spotify artist data
    private val _artistData = MutableStateFlow<SpotifyArtist?>(null)
    val artistData: StateFlow<SpotifyArtist?> = _artistData.asStateFlow()

    // Albums data
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    // Favorite status
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Loading states
    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()

    private val _isLoadingArtist = MutableStateFlow(false)
    val isLoadingArtist: StateFlow<Boolean> = _isLoadingArtist.asStateFlow()

    private val _isLoadingVenue = MutableStateFlow(false)
    val isLoadingVenue: StateFlow<Boolean> = _isLoadingVenue.asStateFlow()

    // Error states
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load event details
     */
    fun loadEventDetails(eventId: String) {
        viewModelScope.launch {
            try {
                _isLoadingDetails.value = true
                _error.value = null

                Log.d(TAG, "Loading event details for: $eventId")

                val result = eventRepository.getEventDetails(eventId)

                if (result.isSuccess) {
                    val details = result.getOrNull()
                    _eventDetails.value = details

                    // Check favorite status
                    _isFavorite.value = favoritesRepository.isFavorite(eventId)

                    Log.d(TAG, "Event details loaded: ${details?.name}")
                } else {
                    _error.value = result.exceptionOrNull()?.message
                        ?: "Failed to load event details"
                    Log.e(TAG, "Failed to load event details: ${_error.value}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e(TAG, "Error loading event details", e)
            } finally {
                _isLoadingDetails.value = false
            }
        }
    }

    /**
     * Load Spotify artist data
     */
    fun loadArtistData(artistName: String) {
        viewModelScope.launch {
            try {
                _isLoadingArtist.value = true

                Log.d(TAG, "Loading artist data for: $artistName")

                // Search for artist
                val artistResult = eventRepository.searchSpotifyArtist(artistName)

                if (artistResult.isSuccess) {
                    val artist = artistResult.getOrNull()
                    _artistData.value = artist

                    // If artist found, load albums
                    if (artist != null) {
                        Log.d(TAG, "Artist found: ${artist.name}")
                        loadArtistAlbums(artist.id)
                    } else {
                        Log.d(TAG, "No artist found for: $artistName")
                        _albums.value = emptyList()
                    }
                } else {
                    Log.e(TAG, "Failed to load artist: ${artistResult.exceptionOrNull()?.message}")
                    _artistData.value = null
                    _albums.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading artist data", e)
                _artistData.value = null
                _albums.value = emptyList()
            } finally {
                _isLoadingArtist.value = false
            }
        }
    }

    /**
     * Load artist albums from Spotify
     */
    private suspend fun loadArtistAlbums(artistId: String) {
        try {
            Log.d(TAG, "Loading albums for artist: $artistId")

            val albumsResult = eventRepository.getArtistAlbums(artistId)

            if (albumsResult.isSuccess) {
                val albums = albumsResult.getOrNull() ?: emptyList()
                // Sort albums by release date (newest first)
                _albums.value = albums.sortedByDescending { it.release_date }
                Log.d(TAG, "Loaded ${albums.size} albums")
            } else {
                Log.e(TAG, "Failed to load albums: ${albumsResult.exceptionOrNull()?.message}")
                _albums.value = emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading albums", e)
            _albums.value = emptyList()
        }
    }

    /**
     * Toggle favorite status
     * Convert EventDetails to Event for storage
     */
    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val eventDetails = _eventDetails.value ?: return@launch

                Log.d(TAG, "Toggling favorite for event: ${eventDetails.id}")

                // Convert EventDetails to Event for favorites storage
                // ✅ 注意：EventDetails 使用 EventDetailsDate，Event 使用 EventDates
                val favoriteEvent = Event(
                    id = eventDetails.id,
                    name = eventDetails.name,

                    // ✅ 转换 EventDetailsDate 为 EventDates
                    dates = eventDetails.dates?.let { detailsDate ->
                        EventDates(
                            start = detailsDate.start?.let { startDate ->
                                EventDate(
                                    localDate = startDate.localDate,
                                    localTime = startDate.localTime
                                )
                            }
                        )
                    },

                    // 转换 embedded
                    embedded = eventDetails._embedded?.let { detailsEmbedded ->
                        EventEmbedded(
                            // Convert VenueDetails list to Venue list
                            venues = detailsEmbedded.venues?.map { venueDetails ->
                                Venue(
                                    name = venueDetails.name,
                                    city = venueDetails.city,  // ✅ VenueCity 类型相同
                                    state = venueDetails.state, // ✅ VenueState 类型相同
                                    location = venueDetails.location // ✅ VenueLocation 类型相同
                                )
                            },
                            attractions = detailsEmbedded.attractions // ✅ Attraction 类型相同
                        )
                    },

                    // 转换 classifications
                    classifications = eventDetails.classifications?.map { eventClassification ->
                        Classification(
                            segment = eventClassification.segment?.let { segment ->
                                Segment(name = segment.name ?: "")
                            },
                            genre = eventClassification.genre?.let { genre ->
                                Genre(name = genre.name ?: "")
                            }
                        )
                    },

                    // 这些字段类型相同，直接复制
                    images = eventDetails.images,
                    priceRanges = eventDetails.priceRanges
                )

                val newFavoriteStatus = if (_isFavorite.value) {
                    favoritesRepository.removeFavorite(eventDetails.id)
                    false
                } else {
                    favoritesRepository.addFavorite(favoriteEvent)
                    true
                }

                _isFavorite.value = newFavoriteStatus

                Log.d(TAG, "Favorite status updated: $newFavoriteStatus")
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite", e)
            }
        }
    }

    /**
     * Load venue details (optional - coordinates are in EventDetails)
     */
    fun loadVenueDetails(venueName: String) {
        viewModelScope.launch {
            try {
                _isLoadingVenue.value = true
                Log.d(TAG, "Venue details already in EventDetails")
            } finally {
                _isLoadingVenue.value = false
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}