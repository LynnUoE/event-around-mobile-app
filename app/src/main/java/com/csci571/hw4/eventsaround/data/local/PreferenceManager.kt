package com.csci571.hw4.eventsaround.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.csci571.hw4.eventsaround.data.model.Event
import com.csci571.hw4.eventsaround.data.model.SearchParams

/**
 * PreferencesManager - Manages app preferences using SharedPreferences
 * Handles:
 * - Favorite events storage
 * - Last search parameters
 * - Search history
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    // ==================== FAVORITES ====================

    /**
     * Add event to favorites
     */
    fun addFavorite(event: Event) {
        val favorites = getAllFavoritesMap().toMutableMap()
        favorites[event.id] = event
        saveFavorites(favorites)
    }

    /**
     * Remove event from favorites
     */
    fun removeFavorite(eventId: String) {
        val favorites = getAllFavoritesMap().toMutableMap()
        favorites.remove(eventId)
        saveFavorites(favorites)
    }

    /**
     * Check if event is in favorites
     */
    fun isFavorite(eventId: String): Boolean {
        return getAllFavoritesMap().containsKey(eventId)
    }

    /**
     * Get all favorite events as a list
     */
    fun getAllFavorites(): List<Event> {
        return getAllFavoritesMap().values.toList()
    }

    /**
     * Clear all favorites
     */
    fun clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply()
    }

    /**
     * Get all favorites as a map (internal use)
     */
    private fun getAllFavoritesMap(): Map<String, Event> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Event>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Save favorites map (internal use)
     */
    private fun saveFavorites(favorites: Map<String, Event>) {
        val json = gson.toJson(favorites)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }

    // ==================== SEARCH PARAMS ====================

    /**
     * Save the last search parameters
     */
    fun saveLastSearchParams(params: SearchParams) {
        val json = gson.toJson(params)
        prefs.edit().putString(KEY_LAST_SEARCH, json).apply()
    }

    /**
     * Get the last search parameters
     */
    fun getLastSearchParams(): SearchParams? {
        val json = prefs.getString(KEY_LAST_SEARCH, null) ?: return null
        return try {
            gson.fromJson(json, SearchParams::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ==================== SEARCH HISTORY ====================

    /**
     * Save a keyword to search history
     * Keeps only the last MAX_SEARCH_HISTORY items
     */
    fun saveSearchHistory(keyword: String) {
        if (keyword.isBlank()) return

        val history = getSearchHistory().toMutableList()

        // Remove if already exists (to move it to the front)
        history.remove(keyword)

        // Add to front
        history.add(0, keyword)

        // Keep only MAX_SEARCH_HISTORY items
        val trimmedHistory = history.take(MAX_SEARCH_HISTORY)

        // Save to preferences
        val json = gson.toJson(trimmedHistory)
        prefs.edit().putString(KEY_SEARCH_HISTORY, json).apply()
    }

    /**
     * Get search history (most recent first)
     */
    fun getSearchHistory(): List<String> {
        val json = prefs.getString(KEY_SEARCH_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Clear search history
     */
    fun clearSearchHistory() {
        prefs.edit().remove(KEY_SEARCH_HISTORY).apply()
    }

    /**
     * Remove a specific keyword from search history
     */
    fun removeFromSearchHistory(keyword: String) {
        val history = getSearchHistory().toMutableList()
        history.remove(keyword)

        val json = gson.toJson(history)
        prefs.edit().putString(KEY_SEARCH_HISTORY, json).apply()
    }

    // ==================== CONSTANTS ====================

    companion object {
        private const val PREFS_NAME = "event_finder_prefs"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_LAST_SEARCH = "last_search"
        private const val KEY_SEARCH_HISTORY = "search_history"

        // Maximum number of search history items to keep
        private const val MAX_SEARCH_HISTORY = 10
    }
}