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
     * @param keyword Search keyword
     */
    @GET("api/events/suggest")
    suspend fun getAutocompleteSuggestions(
        @Query("keyword") keyword: String
    ): Response<AutocompleteResponse>

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