package com.csci571.hw4.eventsaround.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.csci571.hw4.eventsaround.data.model.Event
import com.csci571.hw4.eventsaround.data.local.PreferencesManager

class FavoritesRepository(context: Context) {

    private val prefsManager = PreferencesManager(context)

    suspend fun addFavorite(event: Event): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                prefsManager.addFavorite(event)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun removeFavorite(eventId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                prefsManager.removeFavorite(eventId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun isFavorite(eventId: String): Boolean {
        return withContext(Dispatchers.IO) {
            prefsManager.isFavorite(eventId)
        }
    }

    suspend fun getAllFavorites(): List<Event> {
        return withContext(Dispatchers.IO) {
            prefsManager.getAllFavorites()
        }
    }

    suspend fun clearAllFavorites(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                prefsManager.clearFavorites()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: FavoritesRepository? = null

        fun getInstance(context: Context): FavoritesRepository {
            return instance ?: synchronized(this) {
                instance ?: FavoritesRepository(context.applicationContext)
                    .also { instance = it }
            }
        }
    }
}