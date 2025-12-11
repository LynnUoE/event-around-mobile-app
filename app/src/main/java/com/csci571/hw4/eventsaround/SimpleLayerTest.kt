package com.csci571.hw4.eventsaround

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.csci571.hw4.eventsaround.data.model.SearchParams
import com.csci571.hw4.eventsaround.data.repository.EventRepository
import com.csci571.hw4.eventsaround.data.repository.FavoritesRepository
import kotlinx.coroutines.launch

class SimpleDataLayerTest {

    companion object {
        private const val TAG = "DataLayerTest"

        fun runQuickTests(activity: ComponentActivity) {
            activity.lifecycleScope.launch {
                Log.d(TAG, "========================================")
                Log.d(TAG, "Starting Data Layer Tests...")
                Log.d(TAG, "========================================")

                val repository = EventRepository.getInstance()
                val favRepo = FavoritesRepository.getInstance(activity)

                // Test 1: Search with coordinates
                testSearchWithCoordinates(repository)

                // Test 2: Search different category
                testSearchSports(repository)

                // Test 3: Favorites
                testFavorites(repository, favRepo)

                Log.d(TAG, "========================================")
                Log.d(TAG, "All Tests Completed!")
                Log.d(TAG, "========================================")
            }
        }

        private suspend fun testSearchWithCoordinates(repository: EventRepository) {
            Log.d(TAG, "\n--- Test 1: Search Concerts in LA ---")

            val params = SearchParams(
                keyword = "Concert",
                distance = 10,
                category = SearchParams.CATEGORY_MUSIC,
                latitude = 34.0522,
                longitude = -118.2437
            )

            Log.d(TAG, "Searching with lat=${params.latitude}, lng=${params.longitude}")

            val result = repository.searchEvents(params)

            if (result.isSuccess) {
                val events = result.getOrNull() ?: emptyList()
                Log.d(TAG, "✓ SUCCESS: Found ${events.size} events")

                events.take(3).forEach { event ->
                    Log.d(TAG, "  📅 ${event.name}")
                    Log.d(TAG, "     Venue: ${event.venue}")
                    Log.d(TAG, "     Date: ${event.date}")
                    Log.d(TAG, "     Genre: ${event.genre}")
                }
            } else {
                Log.e(TAG, "✗ FAILED: ${result.exceptionOrNull()?.message}")
            }
        }

        private suspend fun testSearchSports(repository: EventRepository) {
            Log.d(TAG, "\n--- Test 2: Search Sports Events ---")

            val params = SearchParams(
                keyword = "Lakers",
                distance = 10,
                category = SearchParams.CATEGORY_SPORTS,
                latitude = 34.0522,
                longitude = -118.2437
            )

            val result = repository.searchEvents(params)

            if (result.isSuccess) {
                val events = result.getOrNull() ?: emptyList()
                Log.d(TAG, "✓ SUCCESS: Found ${events.size} Lakers events")
            } else {
                Log.e(TAG, "✗ FAILED: ${result.exceptionOrNull()?.message}")
            }
        }

        private suspend fun testFavorites(
            eventRepo: EventRepository,
            favRepo: FavoritesRepository
        ) {
            Log.d(TAG, "\n--- Test 3: Favorites ---")

            val params = SearchParams(
                keyword = "Music",
                distance = 10,
                latitude = 34.0522,
                longitude = -118.2437
            )

            val searchResult = eventRepo.searchEvents(params)

            if (searchResult.isSuccess) {
                val events = searchResult.getOrNull()

                if (!events.isNullOrEmpty()) {
                    val testEvent = events.first()
                    Log.d(TAG, "Testing with event: ${testEvent.name}")

                    favRepo.addFavorite(testEvent)
                    Log.d(TAG, "✓ Added to favorites")

                    val isFav = favRepo.isFavorite(testEvent.id)
                    Log.d(TAG, "Is favorite: $isFav")

                    val allFavorites = favRepo.getAllFavorites()
                    Log.d(TAG, "Total favorites: ${allFavorites.size}")

                    favRepo.removeFavorite(testEvent.id)
                    Log.d(TAG, "✓ Removed from favorites")
                }
            }
        }
    }
}