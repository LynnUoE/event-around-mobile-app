package com.csci571.hw4.eventsaround.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import com.csci571.hw4.eventsaround.data.model.Event
import com.csci571.hw4.eventsaround.data.model.SearchParams

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    fun addFavorite(event: Event) {
        val favorites = getAllFavoritesMap().toMutableMap()
        favorites[event.id] = event
        saveFavorites(favorites)
    }

    fun removeFavorite(eventId: String) {
        val favorites = getAllFavoritesMap().toMutableMap()
        favorites.remove(eventId)
        saveFavorites(favorites)
    }

    fun isFavorite(eventId: String): Boolean {
        return getAllFavoritesMap().containsKey(eventId)
    }

    fun getAllFavorites(): List<Event> {
        return getAllFavoritesMap().values.toList()
    }

    fun clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply()
    }

    private fun getAllFavoritesMap(): Map<String, Event> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Event>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveFavorites(favorites: Map<String, Event>) {
        val json = gson.toJson(favorites)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }

    fun saveLastSearchParams(params: SearchParams) {
        val json = gson.toJson(params)
        prefs.edit().putString(KEY_LAST_SEARCH, json).apply()
    }

    fun getLastSearchParams(): SearchParams? {
        val json = prefs.getString(KEY_LAST_SEARCH, null) ?: return null
        return try {
            gson.fromJson(json, SearchParams::class.java)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "event_finder_prefs"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_LAST_SEARCH = "last_search"
    }
}
