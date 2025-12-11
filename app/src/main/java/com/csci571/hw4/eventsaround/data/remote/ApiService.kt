package com.csci571.hw4.eventsaround.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

import com.csci571.hw4.eventsaround.data.model.EventDetails
import com.csci571.hw4.eventsaround.data.model.AutocompleteResponse
import com.csci571.hw4.eventsaround.data.model.EventListResponse

interface ApiService {

    @GET("api/search")
    suspend fun searchEvents(
        @QueryMap params: Map<String, String>
    ): Response<EventListResponse>

    @GET("api/events/{eventId}")
    suspend fun getEventDetails(
        @Path("eventId") eventId: String
    ): Response<EventDetails>

    @GET("api/autocomplete")
    suspend fun getAutocompleteSuggestions(
        @Query("keyword") keyword: String
    ): Response<AutocompleteResponse>

    @GET("api/venue")
    suspend fun getVenueDetails(
        @Query("name") venueName: String
    ): Response<Map<String, Any>>
}