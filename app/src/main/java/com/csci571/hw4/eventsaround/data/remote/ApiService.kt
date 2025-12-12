package com.csci571.hw4.eventsaround.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import com.csci571.hw4.eventsaround.data.model.*

/**
 * Retrofit API Service Interface
 * Defines all API endpoints for the backend
 */
interface ApiService {

    /**
     * Search for events
     * @param params Query parameters map (keyword, distance, category, lat, lng)
     */
    @GET("api/events/search")
    suspend fun searchEvents(
        @QueryMap params: Map<String, String>
    ): Response<EventListResponse>

    /**
     * Get detailed information for a specific event
     * @param eventId Ticketmaster event ID
     */
    @GET("api/events/{eventId}")
    suspend fun getEventDetails(
        @Path("eventId") eventId: String
    ): Response<EventDetails>

    /**
     * Get autocomplete suggestions for keyword search
     * Uses Ticketmaster suggest endpoint
     * @param keyword Search keyword
     */
    @GET("api/events/suggest")
    suspend fun getAutocompleteSuggestions(
        @Query("keyword") keyword: String
    ): Response<AutocompleteResponse>

    /**
     * Get current location based on IP address
     * Uses IPInfo service via backend
     * Backend endpoint: GET /api/location
     */
    @GET("api/location")
    suspend fun getCurrentLocation(): Response<LocationResponse>

    /**
     * Geocode a location string to coordinates
     * Uses Google Geocoding API via backend
     * Backend endpoint: GET /api/geocode?address=xxx
     * @param address Location string to geocode
     */
    @GET("api/geocode")
    suspend fun geocodeLocation(
        @Query("address") address: String
    ): Response<GeocodeResponse>

    /**
     * Search for artist on Spotify
     * Backend endpoint: GET /api/artists/search?keyword=xxx
     * @param keyword Artist name to search
     */
    @GET("api/artists/search")
    suspend fun searchSpotifyArtist(
        @Query("keyword") keyword: String
    ): Response<SpotifyArtist>

    /**
     * Get albums for a specific artist from Spotify
     * Backend endpoint: GET /api/artists/:id/albums
     * @param artistId Spotify artist ID
     */
    @GET("api/artists/{artistId}/albums")
    suspend fun getArtistAlbums(
        @Path("artistId") artistId: String
    ): Response<SpotifyAlbumsResponse>

    /**
     * Get venue details from Ticketmaster
     * @param venueName Name of the venue
     */
    @GET("api/events/venue/{venueName}")
    suspend fun getVenueDetails(
        @Path("venueName") venueName: String
    ): Response<VenueDetails>
}

/**
 * Location response from IPInfo service
 */
data class LocationResponse(
    val lat: Double,
    val lng: Double,
    val city: String? = null,
    val region: String? = null,
    val country: String? = null
)

/**
 * Geocoding response from Google Geocoding API
 */
data class GeocodeResponse(
    val lat: Double,
    val lng: Double,
    val formatted_address: String? = null
)