package com.csci571.hw4.eventsaround.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.csci571.hw4.eventsaround.data.model.Event
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension to create DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "event_favorites")

/**
 * Repository for managing favorite events
 * Uses DataStore for persistence
 */
class FavoritesRepository private constructor(private val context: Context) {

    private val gson = Gson()
    private val favoritesKey = stringPreferencesKey("favorite_events")
    private val TAG = "FavoritesRepository"

    companion object {
        @Volatile
        private var instance: FavoritesRepository? = null

        fun getInstance(context: Context): FavoritesRepository {
            return instance ?: synchronized(this) {
                instance ?: FavoritesRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    /**
     * Get all favorite events
     */
    suspend fun getAllFavorites(): List<Event> {
        return try {
            val preferences = context.dataStore.data.first()
            val favoritesJson = preferences[favoritesKey]

            if (favoritesJson.isNullOrBlank()) {
                Log.d(TAG, "No favorites found")
                return emptyList()
            }

            val type = object : TypeToken<List<Event>>() {}.type
            val favorites: List<Event> = gson.fromJson(favoritesJson, type) ?: emptyList()

            Log.d(TAG, "Loaded ${favorites.size} favorites")
            favorites
        } catch (e: Exception) {
            Log.e(TAG, "Error loading favorites", e)
            emptyList()
        }
    }

    /**
     * Add event to favorites
     */
    suspend fun addFavorite(event: Event) {
        try {
            context.dataStore.edit { preferences ->
                val currentFavorites = getAllFavorites().toMutableList()

                // Check if already exists
                if (currentFavorites.any { it.id == event.id }) {
                    Log.d(TAG, "Event ${event.id} already in favorites")
                    return@edit
                }

                // Add to list
                currentFavorites.add(event)

                // Save to DataStore
                val json = gson.toJson(currentFavorites)
                preferences[favoritesKey] = json

                Log.d(TAG, "Added event ${event.id} to favorites. Total: ${currentFavorites.size}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding favorite", e)
        }
    }

    /**
     * Remove event from favorites
     */
    suspend fun removeFavorite(eventId: String) {
        try {
            context.dataStore.edit { preferences ->
                val currentFavorites = getAllFavorites().toMutableList()

                // Remove the event
                val removed = currentFavorites.removeAll { it.id == eventId }

                if (removed) {
                    // Save updated list
                    val json = gson.toJson(currentFavorites)
                    preferences[favoritesKey] = json

                    Log.d(TAG, "Removed event $eventId from favorites. Remaining: ${currentFavorites.size}")
                } else {
                    Log.d(TAG, "Event $eventId not found in favorites")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing favorite", e)
        }
    }

    /**
     * Check if event is in favorites
     */
    suspend fun isFavorite(eventId: String): Boolean {
        return try {
            val favorites = getAllFavorites()
            val isFav = favorites.any { it.id == eventId }
            Log.d(TAG, "Event $eventId is favorite: $isFav")
            isFav
        } catch (e: Exception) {
            Log.e(TAG, "Error checking favorite", e)
            false
        }
    }

    /**
     * Toggle favorite status
     */
    suspend fun toggleFavorite(event: Event): Boolean {
        return try {
            if (isFavorite(event.id)) {
                removeFavorite(event.id)
                false
            } else {
                addFavorite(event)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling favorite", e)
            false
        }
    }

    /**
     * Clear all favorites
     */
    suspend fun clearAllFavorites() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(favoritesKey)
                Log.d(TAG, "Cleared all favorites")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing favorites", e)
        }
    }

    /**
     * Get favorites count
     */
    suspend fun getFavoritesCount(): Int {
        return try {
            getAllFavorites().size
        } catch (e: Exception) {
            0
        }
    }
}