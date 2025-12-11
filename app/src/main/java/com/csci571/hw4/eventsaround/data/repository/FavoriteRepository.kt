package com.csci571.hw4.eventsaround.data.repository

import android.content.Context
import android.util.Log
import com.csci571.hw4.eventsaround.data.local.PreferencesManager
import com.csci571.hw4.eventsaround.data.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing favorite events
 * Uses Flow for reactive updates across the app
 * All operations are synchronous since SharedPreferences is fast
 */
class FavoritesRepository private constructor(context: Context) {

    private val prefsManager = PreferencesManager(context)
    private val TAG = "FavoritesRepository"

    // StateFlow for reactive favorites list updates
    private val _favoritesFlow = MutableStateFlow<List<Event>>(emptyList())
    val favoritesFlow: StateFlow<List<Event>> = _favoritesFlow.asStateFlow()

    init {
        // Load initial favorites
        loadFavorites()
    }

    /**
     * Add an event to favorites
     */
    fun addFavorite(event: Event) {
        try {
            prefsManager.addFavorite(event)
            loadFavorites()
            Log.d(TAG, "Added to favorites: ${event.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding favorite", e)
        }
    }

    /**
     * Remove an event from favorites
     */
    fun removeFavorite(eventId: String) {
        try {
            prefsManager.removeFavorite(eventId)
            loadFavorites()
            Log.d(TAG, "Removed from favorites: $eventId")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing favorite", e)
        }
    }

    /**
     * Check if an event is in favorites (synchronous)
     */
    fun isFavorite(eventId: String): Boolean {
        return prefsManager.isFavorite(eventId)
    }

    /**
     * Get all favorite events
     */
    fun getAllFavorites(): List<Event> {
        return prefsManager.getAllFavorites()
    }

    /**
     * Clear all favorites
     */
    fun clearAllFavorites() {
        try {
            prefsManager.clearFavorites()
            loadFavorites()
            Log.d(TAG, "Cleared all favorites")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing favorites", e)
        }
    }

    /**
     * Toggle favorite status of an event
     * @return true if added, false if removed
     */
    fun toggleFavorite(event: Event): Boolean {
        return if (isFavorite(event.id)) {
            removeFavorite(event.id)
            false
        } else {
            addFavorite(event)
            true
        }
    }

    /**
     * Load favorites from preferences and update flow
     */
    private fun loadFavorites() {
        _favoritesFlow.value = prefsManager.getAllFavorites()
    }

    companion object {
        @Volatile
        private var instance: FavoritesRepository? = null

        /**
         * Get singleton instance of FavoritesRepository
         */
        fun getInstance(context: Context): FavoritesRepository {
            return instance ?: synchronized(this) {
                instance ?: FavoritesRepository(context.applicationContext)
                    .also { instance = it }
            }
        }
    }
}