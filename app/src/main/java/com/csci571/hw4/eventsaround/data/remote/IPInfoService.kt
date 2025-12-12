package com.csci571.hw4.eventsaround.data.remote

import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Response from IPInfo API
 */
data class IPInfoResponse(
    @SerializedName("ip")
    val ip: String?,

    @SerializedName("city")
    val city: String?,

    @SerializedName("region")
    val region: String?,

    @SerializedName("country")
    val country: String?,

    @SerializedName("loc")
    val loc: String?, // Format: "latitude,longitude"

    @SerializedName("org")
    val org: String?,

    @SerializedName("postal")
    val postal: String?,

    @SerializedName("timezone")
    val timezone: String?
)

/**
 * Retrofit API interface for IPInfo service
 */
interface IPInfoApi {
    /**
     * Get current location based on IP address
     * @param token Optional IPInfo token for higher rate limits
     */
    @GET("json")
    suspend fun getCurrentLocation(
        @Query("token") token: String? = null
    ): IPInfoResponse
}

/**
 * Service for getting current location based on IP address
 * Uses IPInfo.io service
 */
class IPInfoService private constructor() {

    companion object {
        private const val TAG = "IPInfoService"
        private const val BASE_URL = "https://ipinfo.io/"

        // Optional: Add your IPInfo token for higher rate limits
        // Get free token at: https://ipinfo.io/signup
        private const val IPINFO_TOKEN = "bf8f570fb8f455" // Your token or null

        // Default coordinates (Los Angeles)
        private const val DEFAULT_LAT = 34.0522
        private const val DEFAULT_LNG = -118.2437

        @Volatile
        private var instance: IPInfoService? = null

        fun getInstance(): IPInfoService {
            return instance ?: synchronized(this) {
                instance ?: IPInfoService().also { instance = it }
            }
        }
    }

    private val api: IPInfoApi by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IPInfoApi::class.java)
    }

    /**
     * Get current location based on IP address
     * @return Pair of (latitude, longitude) or default LA coordinates if failed
     */
    suspend fun getCurrentLocation(): Pair<Double, Double> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching current location from IP")

                val response = api.getCurrentLocation(
                    token = IPINFO_TOKEN?.takeIf { it.isNotBlank() }
                )

                if (!response.loc.isNullOrBlank()) {
                    val parts = response.loc.split(",")
                    if (parts.size == 2) {
                        val lat = parts[0].toDoubleOrNull()
                        val lng = parts[1].toDoubleOrNull()

                        if (lat != null && lng != null) {
                            Log.d(TAG, "Location detected: ${response.city}, ${response.region} ($lat, $lng)")
                            return@withContext Pair(lat, lng)
                        }
                    }
                }

                Log.w(TAG, "Invalid location data in response")

                // Return default location
                Log.d(TAG, "Using default location: Los Angeles")
                return@withContext Pair(DEFAULT_LAT, DEFAULT_LNG)

            } catch (e: Exception) {
                Log.e(TAG, "Error getting current location", e)
                Log.d(TAG, "Using default location: Los Angeles")
                return@withContext Pair(DEFAULT_LAT, DEFAULT_LNG)
            }
        }
    }

    /**
     * Get current location with additional info
     * @return IPInfoResponse or null if failed
     */
    suspend fun getCurrentLocationInfo(): IPInfoResponse? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching detailed location info from IP")

                val response = api.getCurrentLocation(
                    token = IPINFO_TOKEN?.takeIf { it.isNotBlank() }
                )

                Log.d(TAG, "Location info: ${response.city}, ${response.region}, ${response.country}")
                return@withContext response
            } catch (e: Exception) {
                Log.e(TAG, "Error getting location info", e)
                return@withContext null
            }
        }
    }
}