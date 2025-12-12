package com.csci571.hw4.eventsaround.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.csci571.hw4.eventsaround.data.local.PreferencesManager
import com.csci571.hw4.eventsaround.data.model.Event
import com.csci571.hw4.eventsaround.data.model.SearchParams
import com.csci571.hw4.eventsaround.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Search and Results screens
 * Manages search state, results, and loading states
 */
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "SearchViewModel"
    private val repository = EventRepository.getInstance()
    private val preferencesManager = PreferencesManager(application)

    // Search results state
    private val _searchResults = MutableStateFlow<List<Event>>(emptyList())
    val searchResults: StateFlow<List<Event>> = _searchResults.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Autocomplete suggestions
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    // Last search params for reference
    private var lastSearchParams: SearchParams? = null

    /**
     * Perform event search
     */
    fun searchEvents(params: SearchParams) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                lastSearchParams = params

                Log.d(TAG, "Starting search with params: $params")

                // Get coordinates
                val (lat, lng) = if (params.autoDetect) {
                    Log.d(TAG, "Using auto-detect location")
                    getCurrentLocation()
                } else if (params.location.isNotBlank()) {
                    Log.d(TAG, "Geocoding manual location: ${params.location}")
                    geocodeLocation(params.location)
                } else {
                    Log.d(TAG, "Using default LA coordinates")
                    Pair(SearchParams.DEFAULT_LAT, SearchParams.DEFAULT_LNG)
                }

                // Update params with coordinates
                val searchParamsWithCoords = params.copy(
                    latitude = lat,
                    longitude = lng
                )

                Log.d(TAG, "Searching events at: ($lat, $lng)")

                // Perform search
                val result = repository.searchEvents(searchParamsWithCoords)

                if (result.isSuccess) {
                    val events = result.getOrNull() ?: emptyList()
                    _searchResults.value = events
                    Log.d(TAG, "Search successful: ${events.size} events found")

                    // Save search params and history
                    preferencesManager.saveLastSearchParams(params)
                    preferencesManager.saveSearchHistory(params.keyword)
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to search events"
                    _searchResults.value = emptyList()
                    Log.e(TAG, "Search failed: ${_error.value}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                _searchResults.value = emptyList()
                Log.e(TAG, "Search error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get autocomplete suggestions
     * PUBLIC method - can be called from SearchScreen
     */
    fun getAutocompleteSuggestions(keyword: String) {
        if (keyword.length < 2) {
            _suggestions.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val result = repository.getAutocompleteSuggestions(keyword)

                if (result.isSuccess) {
                    _suggestions.value = result.getOrNull() ?: emptyList()
                } else {
                    _suggestions.value = emptyList()
                }
            } catch (e: Exception) {
                _suggestions.value = emptyList()
            }
        }
    }

    /**
     * Get current location using IP-based geolocation
     */
    private suspend fun getCurrentLocation(): Pair<Double, Double> {
        Log.d(TAG, "Getting current location via backend")

        val result = repository.getCurrentLocation()
        return result.getOrElse {
            Log.w(TAG, "Failed to get current location, using default")
            Pair(SearchParams.DEFAULT_LAT, SearchParams.DEFAULT_LNG)
        }
    }

    /**
     * Geocode a location string to coordinates
     */
    private suspend fun geocodeLocation(location: String): Pair<Double, Double> {
        Log.d(TAG, "Geocoding location: $location")

        val result = repository.geocodeLocation(location)
        return result.getOrElse {
            Log.w(TAG, "Failed to geocode location, using default")
            Pair(SearchParams.DEFAULT_LAT, SearchParams.DEFAULT_LNG)
        }
    }

    /**
     * Clear search results
     */
    fun clearResults() {
        _searchResults.value = emptyList()
        _error.value = null
        lastSearchParams = null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Get search history from preferences
     */
    fun getSearchHistory(): List<String> {
        return preferencesManager.getSearchHistory()
    }

    /**
     * Clear search history
     */
    fun clearSearchHistory() {
        preferencesManager.clearSearchHistory()
    }

    /**
     * Get last search parameters
     */
    fun getLastSearchParams(): SearchParams? = lastSearchParams
}
