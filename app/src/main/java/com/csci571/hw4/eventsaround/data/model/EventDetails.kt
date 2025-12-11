package com.csci571.hw4.eventsaround.data.model

import com.google.gson.annotations.SerializedName

data class EventDetails(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("time")
    val time: String,

    @SerializedName("artist")
    val artists: List<String>? = null,

    @SerializedName("venue")
    val venue: VenueInfo,

    @SerializedName("genre")
    val genres: List<String>? = null,

    @SerializedName("priceRange")
    val priceRange: String? = null,

    @SerializedName("ticketStatus")
    val ticketStatus: String? = null,

    @SerializedName("buyTicketAt")
    val buyTicketUrl: String? = null,

    @SerializedName("seatmap")
    val seatMapUrl: String? = null
)

data class VenueInfo(
    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("city")
    val city: String? = null,

    @SerializedName("state")
    val state: String? = null,

    @SerializedName("location")
    val location: Location? = null,

    @SerializedName("openHours")
    val openHours: String? = null,

    @SerializedName("generalRule")
    val generalRule: String? = null,

    @SerializedName("childRule")
    val childRule: String? = null
)

data class Location(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)

data class Album(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("image")
    val imageUrl: String,

    @SerializedName("releaseDate")
    val releaseDate: String
)
