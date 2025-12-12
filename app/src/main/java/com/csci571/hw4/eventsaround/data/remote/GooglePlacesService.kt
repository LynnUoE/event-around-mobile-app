package com.csci571.hw4.eventsaround.data.remote

import android.util.Log
import com.csci571.hw4.eventsaround.BuildConfig
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Data classes for Google Places Autocomplete API
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
    val status: String,

    @SerializedName("error_message")
    val errorMessage: String?
)

/**
 * Data classes for Google Geocoding API
 * Using Google prefix to avoid conflicts with ApiService types
 */
data class GoogleGeocodeResult(
    @SerializedName("formatted_address")
    val formattedAddress: String,

    @SerializedName("geometry")
    val geometry: GoogleGeometry
)

data class GoogleGeometry(
    @SerializedName("location")
    val location: GoogleLatLng
)

data class GoogleLatLng(
    @SerializedName("lat")
    val lat: Double,

    @SerializedName("lng")
    val lng: Double
)

data class GoogleGeocodeResponse(
    @SerializedName("results")
    val results: List<GoogleGeocodeResult>,

    @SerializedName("status")
    val status: String,

    @SerializedName("error_message")
    val errorMessage: String?
)

/**
 * Retrofit API interface for Google Maps APIs
 */
interface GoogleMapsApi {
    /**
     * Google Places Autocomplete API
     */
    @GET("place/autocomplete/json")
    suspend fun getPlacePredictions(
        @Query("input") input: String,
        @Query("types") types: String,
        @Query("key") apiKey: String
    ): PlacesAutocompleteResponse

    /**
     * Google Geocoding API
     */
    @GET("geocode/json")
    suspend fun geocodeAddress(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GoogleGeocodeResponse
}

/**
 * Service class for Google Maps API operations
 * Handles Places Autocomplete and Geocoding
 */
class GooglePlacesService private constructor() {

    companion object {
        private const val TAG = "GooglePlacesService"
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

        // Get API key from BuildConfig (configured in build.gradle.kts)
        private val GOOGLE_API_KEY = BuildConfig.GOOGLE_PLACES_API_KEY

        @Volatile
        private var instance: GooglePlacesService? = null

        fun getInstance(): GooglePlacesService {
            return instance ?: synchronized(this) {
                instance ?: GooglePlacesService().also { instance = it }
            }
        }
    }

    private val api: GoogleMapsApi by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleMapsApi::class.java)
    }

    /**
     * Get location suggestions based on user input
     * @param input User's text input (minimum 2 characters)
     * @return List of location suggestion strings
     */
    suspend fun getLocationSuggestions(input: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (input.length < 2) {
                    return@withContext emptyList()
                }

                // Check if API key is configured
                if (GOOGLE_API_KEY.isBlank() || GOOGLE_API_KEY == "YOUR_GOOGLE_PLACES_API_KEY") {
                    Log.w(TAG, "Google API Key not configured, using mock suggestions")
                    return@withContext getMockSuggestions(input)
                }

                Log.d(TAG, "Fetching location suggestions for: $input")

                val response = api.getPlacePredictions(
                    input = input,
                    types = "(cities)",
                    apiKey = GOOGLE_API_KEY
                )

                when (response.status) {
                    "OK" -> {
                        val suggestions = response.predictions.map { it.description }
                        Log.d(TAG, "Successfully retrieved ${suggestions.size} suggestions")
                        return@withContext suggestions
                    }
                    "ZERO_RESULTS" -> {
                        Log.d(TAG, "No suggestions found")
                        return@withContext emptyList()
                    }
                    "REQUEST_DENIED" -> {
                        Log.e(TAG, "API request denied: ${response.errorMessage}")
                        return@withContext getMockSuggestions(input)
                    }
                    else -> {
                        Log.w(TAG, "API returned status: ${response.status}")
                        return@withContext getMockSuggestions(input)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching place predictions", e)
                return@withContext getMockSuggestions(input)
            }
        }
    }

    /**
     * Geocode an address to coordinates
     * @param address Address string to geocode
     * @return Pair of (latitude, longitude) or null if failed
     */
    suspend fun geocodeAddress(address: String): Pair<Double, Double>? {
        return withContext(Dispatchers.IO) {
            try {
                // Check if API key is configured
                if (GOOGLE_API_KEY.isBlank() || GOOGLE_API_KEY == "YOUR_GOOGLE_PLACES_API_KEY") {
                    Log.w(TAG, "Google API Key not configured")
                    return@withContext null
                }

                Log.d(TAG, "Geocoding address: $address")

                val response = api.geocodeAddress(
                    address = address,
                    apiKey = GOOGLE_API_KEY
                )

                when (response.status) {
                    "OK" -> {
                        if (response.results.isNotEmpty()) {
                            val location = response.results[0].geometry.location
                            Log.d(TAG, "Successfully geocoded to: (${location.lat}, ${location.lng})")
                            return@withContext Pair(location.lat, location.lng)
                        }
                    }
                    "ZERO_RESULTS" -> {
                        Log.w(TAG, "No results found for address")
                    }
                    else -> {
                        Log.e(TAG, "Geocoding failed: ${response.status} - ${response.errorMessage}")
                    }
                }

                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "Error geocoding address", e)
                return@withContext null
            }
        }
    }

    /**
     * Fallback mock suggestions when API is unavailable
     * Provides common city suggestions based on input
     */
    private fun getMockSuggestions(input: String): List<String> {
        val lowerInput = input.lowercase()

        val cityDatabase = mapOf(
            "a" to listOf("Atlanta, GA, USA", "Austin, TX, USA", "Albuquerque, NM, USA"),
            "b" to listOf("Boston, MA, USA", "Baltimore, MD, USA", "Buffalo, NY, USA", "Birmingham, AL, USA"),
            "c" to listOf("Chicago, IL, USA", "Charlotte, NC, USA", "Columbus, OH, USA", "Cleveland, OH, USA"),
            "d" to listOf("Dallas, TX, USA", "Denver, CO, USA", "Detroit, MI, USA"),
            "e" to listOf("El Paso, TX, USA", "Eugene, OR, USA"),
            "f" to listOf("Fort Worth, TX, USA", "Fresno, CA, USA"),
            "h" to listOf("Houston, TX, USA", "Hartford, CT, USA"),
            "i" to listOf("Indianapolis, IN, USA", "Irvine, CA, USA"),
            "j" to listOf("Jacksonville, FL, USA", "Jersey City, NJ, USA"),
            "k" to listOf("Kansas City, MO, USA"),
            "l" to listOf("Los Angeles, CA, USA", "Las Vegas, NV, USA", "Louisville, KY, USA", "Long Beach, CA, USA"),
            "m" to listOf("Miami, FL, USA", "Milwaukee, WI, USA", "Minneapolis, MN, USA", "Memphis, TN, USA"),
            "n" to listOf("New York, NY, USA", "New Orleans, LA, USA", "Newark, NJ, USA", "Nashville, TN, USA", "New Haven, CT, USA"),
            "o" to listOf("Orlando, FL, USA", "Oakland, CA, USA", "Oklahoma City, OK, USA"),
            "p" to listOf("Philadelphia, PA, USA", "Phoenix, AZ, USA", "Portland, OR, USA", "Pittsburgh, PA, USA"),
            "r" to listOf("Raleigh, NC, USA", "Richmond, VA, USA"),
            "s" to listOf("San Francisco, CA, USA", "Seattle, WA, USA", "San Diego, CA, USA", "San Jose, CA, USA", "San Antonio, TX, USA", "Sacramento, CA, USA"),
            "t" to listOf("Tampa, FL, USA", "Tucson, AZ, USA", "Toledo, OH, USA"),
            "w" to listOf("Washington, DC, USA", "Wichita, KS, USA")
        )

        // Find matches based on first letter
        val firstLetter = lowerInput.firstOrNull()?.toString() ?: ""
        val candidates = cityDatabase[firstLetter] ?: emptyList()

        // Filter candidates that contain the input
        val matches = candidates.filter { city ->
            city.lowercase().contains(lowerInput)
        }

        // If no matches found, provide a generic suggestion
        if (matches.isEmpty() && input.length >= 2) {
            return listOf("$input, USA")
        }

        return matches.take(5)
    }
}