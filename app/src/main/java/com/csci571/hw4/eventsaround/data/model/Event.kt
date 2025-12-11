package com.csci571.hw4.eventsaround.data.model

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("time")
    val time: String,

    @SerializedName("venue")
    val venue: String,

    @SerializedName("genre")
    val genre: String,

    @SerializedName("image")
    val imageUrl: String,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("priceRange")
    val priceRange: String? = null
)

data class EventListResponse(
    @SerializedName("events")
    val events: List<Event>
)