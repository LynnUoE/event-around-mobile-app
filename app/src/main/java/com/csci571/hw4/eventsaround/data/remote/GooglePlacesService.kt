package com.csci571.hw4.eventsaround.data.remote

import android.util.Log
import com.csci571.hw4.eventsaround.util.Constants
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Data class for Google Places Autocomplete API response
 */
data class PlacePrediction(
    @SerializedName("description")
    val description: String,

    @SerializedName("place_id")
    val placeId: String,

    @SerializedName("structured_formatting")
    val structuredFormatting: StructuredFormatting?
)

data class StructuredFormatting(
    @SerializedName("main_text")
    val mainText: String,

    @SerializedName("secondary_text")
    val secondaryText: String?
)

data class PlacesAutocompleteResponse(
    @SerializedName("predictions")
    val predictions: List<PlacePrediction>,

    @SerializedName("status")
    val status: String
)

/**
 * Retrofit API interface for Google Places Autocomplete
 */
interface GooglePlacesApi {
    @GET("place/autocomplete/json")
    suspend fun getPlacePredictions(
        @Query("input") input: String,
        @Query("types") types: String = "(cities)",
        @Query("key") apiKey: String
    ): PlacesAutocompleteResponse
}

/**
 * Service class for Google Places API operations
 */
class GooglePlacesService {

    companion object {
        private const val TAG = "GooglePlacesService"
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

        // TODO: Replace with your actual Google Places API key
        // You can get it from: https://console.cloud.google.com/
        private const val GOOGLE_API_KEY = "YOUR_GOOGLE_PLACES_API_KEY"
    }

    private val api: GooglePlacesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GooglePlacesApi::class.java)
    }

    /**
     * Get location suggestions based on user input
     * @param input User's text input
     * @return List of location suggestion strings
     */
    suspend fun getLocationSuggestions(input: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (input.length < 2) {
                    return@withContext emptyList()
                }

                val response = api.getPlacePredictions(
                    input = input,
                    types = "(cities)",
                    apiKey = GOOGLE_API_KEY
                )

                if (response.status == "OK") {
                    response.predictions.map { it.description }
                } else {
                    Log.w(TAG, "Places API returned status: ${response.status}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching place predictions", e)
                // Return mock suggestions as fallback
                getMockSuggestions(input)
            }
        }
    }

    /**
     * Fallback mock suggestions when API is unavailable
     */
    private fun getMockSuggestions(input: String): List<String> {
        val lowerInput = input.lowercase()
        return when {
            lowerInput.startsWith("bos") -> listOf(
                "Boston, MA, USA",
                "Boston, UK",
                "Bossier City, LA, USA"
            )
            lowerInput.startsWith("new") -> listOf(
                "New York, NY, USA",
                "New Orleans, LA, USA",
                "Newark, NJ, USA",
                "New Haven, CT, USA"
            )
            lowerInput.startsWith("los") -> listOf(
                "Los Angeles, CA, USA",
                "Los Gatos, CA, USA",
                "Los Alamos, NM, USA"
            )
            lowerInput.startsWith("san") -> listOf(
                "San Francisco, CA, USA",
                "San Diego, CA, USA",
                "San Jose, CA, USA",
                "San Antonio, TX, USA"
            )
            lowerInput.startsWith("chi") -> listOf(
                "Chicago, IL, USA",
                "Chico, CA, USA"
            )
            lowerInput.startsWith("sea") -> listOf(
                "Seattle, WA, USA",
                "Seaside, CA, USA"
            )
            lowerInput.startsWith("por") -> listOf(
                "Portland, OR, USA",
                "Portland, ME, USA"
            )
            lowerInput.startsWith("mia") -> listOf(
                "Miami, FL, USA",
                "Miami Beach, FL, USA"
            )
            else -> listOf(
                "$input, USA"
            )
        }.filter {
            it.lowercase().contains(lowerInput)
        }.take(5)
    }
}