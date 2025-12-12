package com.csci571.hw4.eventsaround.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.csci571.hw4.eventsaround.data.model.Event
import com.csci571.hw4.eventsaround.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Results Screen
 * Manages favorite states for search results
 */
class ResultsViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ResultsViewModel"
    private val favoritesRepository = FavoritesRepository.getInstance(application)

    // Map of event ID to favorite status
    private val _favoriteStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favoriteStates: StateFlow<Map<String, Boolean>> = _favoriteStates.asStateFlow()

    /**
     * Initialize favorite states for a list of events
     * Check which events are already favorited
     */
    fun initializeFavoriteStates(events: List<Event>) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initializing favorite states for ${events.size} events")

                val states = mutableMapOf<String, Boolean>()
                events.forEach { event ->
                    states[event.id] = favoritesRepository.isFavorite(event.id)
                }

                _favoriteStates.value = states
                Log.d(TAG, "Favorite states initialized: ${states.size} events")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing favorite states", e)
            }
        }
    }

    /**
     * Toggle favorite status for an event
     */
    fun toggleFavorite(event: Event) {
        viewModelScope.launch {
            try {
                val currentState = _favoriteStates.value[event.id] ?: false
                Log.d(TAG, "Toggling favorite for ${event.name}, current: $currentState")

                if (currentState) {
                    // Remove from favorites
                    favoritesRepository.removeFavorite(event.id)
                    updateFavoriteState(event.id, false)
                    Log.d(TAG, "Removed ${event.name} from favorites")
                } else {
                    // Add to favorites
                    favoritesRepository.addFavorite(event)
                    updateFavoriteState(event.id, true)
                    Log.d(TAG, "Added ${event.name} to favorites")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite", e)
            }
        }
    }

    /**
     * Update favorite state for a specific event
     */
    private fun updateFavoriteState(eventId: String, isFavorite: Boolean) {
        val currentStates = _favoriteStates.value.toMutableMap()
        currentStates[eventId] = isFavorite
        _favoriteStates.value = currentStates
    }

    /**
     * Refresh favorite states from repository
     * Useful when returning from detail screen
     */
    fun refreshFavoriteStates(events: List<Event>) {
        initializeFavoriteStates(events)
    }
}