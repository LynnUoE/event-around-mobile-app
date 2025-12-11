package com.csci571.hw4.eventsaround.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.csci571.hw4.eventsaround.data.remote.RetrofitClient
import com.csci571.hw4.eventsaround.data.model.SearchParams
import com.csci571.hw4.eventsaround.data.model.EventDetails
import com.csci571.hw4.eventsaround.data.model.Event
import android.util.Log

class EventRepository {

    private val apiService = RetrofitClient.getApiService()
    private val TAG = "EventRepository"

    suspend fun searchEvents(params: SearchParams): Result<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!params.isValid()) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Invalid search parameters")
                    )
                }

                Log.d(TAG, "Searching with params: ${params.toQueryMap()}")

                val response = apiService.searchEvents(params.toQueryMap())

                if (response.isSuccessful) {
                    val events = response.body()?.embedded?.events ?: emptyList()
                    Log.d(TAG, "Search successful: Found ${events.size} events")
                    Result.success(events)
                } else {
                    val error = "API Error: ${response.code()} ${response.message()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Search error", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getEventDetails(eventId: String): Result<EventDetails> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEventDetails(eventId)

                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Event details not found"))
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

    suspend fun getAutocompleteSuggestions(keyword: String): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                if (keyword.isBlank()) {
                    return@withContext Result.success(emptyList())
                }

                val response = apiService.getAutocompleteSuggestions(keyword)

                if (response.isSuccessful) {
                    val suggestions = response.body()?.suggestions ?: emptyList()
                    Result.success(suggestions)
                } else {
                    Result.success(emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Autocomplete error", e)
                Result.success(emptyList())
            }
        }
    }

    companion object {
        @Volatile
        private var instance: EventRepository? = null

        fun getInstance(): EventRepository {
            return instance ?: synchronized(this) {
                instance ?: EventRepository().also { instance = it }
            }
        }
    }
}