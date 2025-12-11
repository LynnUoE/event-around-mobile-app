package com.csci571.hw4.eventsaround.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.csci571.hw4.eventsaround.data.model.*
import com.csci571.hw4.eventsaround.data.repository.EventRepository
import com.csci571.hw4.eventsaround.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.csci571.hw4.eventsaround.data.repository.*
/**
 * ViewModel for Event Details Screen
 * Manages event details, artist info, venue info, and favorite status
 */
class EventDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val eventRepository = EventRepository.getInstance()
    private val favoritesRepository = FavoritesRepository.getInstance(application)

    // Event details state
    private val _eventDetails = MutableStateFlow<EventDetails?>(null)
    val eventDetails: StateFlow<EventDetails?> = _eventDetails.asStateFlow()

    // Spotify artist data
    private val _artistData = MutableStateFlow<SpotifyArtist?>(null)
    val artistData: StateFlow<SpotifyArtist?> = _artistData.asStateFlow()

    // Albums data
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    // Venue details
    private val _venueDetails = MutableStateFlow<VenueDetails?>(null)
    val venueDetails: StateFlow<VenueDetails?> = _venueDetails.asStateFlow()

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

                val result = eventRepository.getEventDetails(eventId)

                if (result.isSuccess) {
                    _eventDetails.value = result.getOrNull()
                    // Check favorite status
                    _isFavorite.value = favoritesRepository.isFavorite(eventId)
                } else {
                    _error.value = result.exceptionOrNull()?.message
                        ?: "Failed to load event details"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
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

                val result = eventRepository.searchSpotifyArtist(artistName)

                if (result.isSuccess) {
                    // result.getOrNull() already returns SpotifyArtist (not the wrapper)
                    val artist = result.getOrNull()
                    _artistData.value = artist

                    // Load albums if artist found
                    artist?.id?.let { artistId ->
                        loadArtistAlbums(artistId)
                    }
                }
            } catch (e: Exception) {
                // Silently fail for artist data
            } finally {
                _isLoadingArtist.value = false
            }
        }
    }

    /**
     * Load artist albums from Spotify
     */
    private fun loadArtistAlbums(artistId: String) {
        viewModelScope.launch {
            try {
                val result = eventRepository.getArtistAlbums(artistId)

                if (result.isSuccess) {
                    _albums.value = result.getOrNull() ?: emptyList()
                }
            } catch (e: Exception) {
                // Silently fail for albums
            }
        }
    }

    /**
     * Load venue details
     */
    fun loadVenueDetails(venueName: String) {
        viewModelScope.launch {
            try {
                _isLoadingVenue.value = true

                val result = eventRepository.getVenueDetails(venueName)

                if (result.isSuccess) {
                    _venueDetails.value = result.getOrNull()
                }
            } catch (e: Exception) {
                // Silently fail for venue details
            } finally {
                _isLoadingVenue.value = false
            }
        }
    }

    /**
     * Toggle favorite status
     * @param eventId Event ID to toggle
     * @param event Optional Event object (if available from search results)
     */
    fun toggleFavorite(eventId: String, event: Event? = null): Boolean {
        // If we have the full Event object, use it
        if (event != null) {
            val isNowFavorite = favoritesRepository.toggleFavorite(event)
            _isFavorite.value = isNowFavorite
            return isNowFavorite
        }

        // Otherwise, create a minimal Event from EventDetails
        val details = _eventDetails.value
        if (details != null) {
            val minimalEvent = createMinimalEvent(details, eventId)
            val isNowFavorite = favoritesRepository.toggleFavorite(minimalEvent)
            _isFavorite.value = isNowFavorite
            return isNowFavorite
        }

        return false
    }

    /**
     * Check and update favorite status for current event
     */
    fun checkFavoriteStatus(eventId: String) {
        _isFavorite.value = favoritesRepository.isFavorite(eventId)
    }

    /**
     * Create minimal Event object from EventDetails for favorites
     */
    private fun createMinimalEvent(details: EventDetails, eventId: String): Event {
        return Event(
            id = eventId,
            name = details.name,
            dates = EventDates(
                start = EventDate(
                    localDate = details.date,
                    localTime = details.time
                )
            ),
            embedded = EventEmbedded(
                venues = listOf(
                    Venue(
                        name = details.venue.name,
                        city = VenueCity(""),
                        state = VenueState(""),
                        location = null
                    )
                ),
                attractions = details.artists?.map { artistName ->
                    Attraction(name = artistName)
                }
            ),
            images = if (!details.seatMapUrl.isNullOrEmpty()) {
                listOf(EventImage(url = details.seatMapUrl))
            } else emptyList(),
            classifications = if (!details.genres.isNullOrEmpty()) {
                listOf(
                    Classification(
                        segment = Segment(details.genres.firstOrNull() ?: ""),
                        genre = Genre(details.genres.firstOrNull() ?: "")
                    )
                )
            } else null,
            priceRanges = null
        )
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
}