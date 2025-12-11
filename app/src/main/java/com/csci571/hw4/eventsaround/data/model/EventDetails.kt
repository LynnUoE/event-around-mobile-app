package com.csci571.hw4.eventsaround.data.model

import com.google.gson.annotations.SerializedName

/**
 * Complete event details from Ticketmaster API
 */
data class EventDetails(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("url")
    val url: String?,

    @SerializedName("images")
    val images: List<EventImage>?,

    @SerializedName("dates")
    val dates: EventDates?,

    @SerializedName("classifications")
    val classifications: List<EventClassification>?,

    @SerializedName("priceRanges")
    val priceRanges: List<PriceRange>?,

    @SerializedName("seatmap")
    val seatmap: Seatmap?,

    @SerializedName("_embedded")
    val _embedded: EventDetailsEmbedded?
)

data class EventDates(
    @SerializedName("start")
    val start: EventStartDate?,

    @SerializedName("status")
    val status: EventStatus?
)

data class EventStartDate(
    @SerializedName("localDate")
    val localDate: String?,

    @SerializedName("localTime")
    val localTime: String?
)

data class EventStatus(
    @SerializedName("code")
    val code: String?  // "onsale", "offsale", "canceled", etc.
)

data class EventClassification(
    @SerializedName("segment")
    val segment: EventSegment?,

    @SerializedName("genre")
    val genre: EventGenre?,

    @SerializedName("subGenre")
    val subGenre: EventSubGenre?,

    @SerializedName("type")
    val type: EventType?,

    @SerializedName("subType")
    val subType: EventSubType?
)

data class EventSegment(
    @SerializedName("name")
    val name: String?
)

data class EventGenre(
    @SerializedName("name")
    val name: String?
)

data class EventSubGenre(
    @SerializedName("name")
    val name: String?
)

data class EventType(
    @SerializedName("name")
    val name: String?
)

data class EventSubType(
    @SerializedName("name")
    val name: String?
)

data class Seatmap(
    @SerializedName("staticUrl")
    val staticUrl: String?
)

data class EventDetailsEmbedded(
    @SerializedName("venues")
    val venues: List<VenueDetails>?,

    @SerializedName("attractions")
    val attractions: List<Attraction>?
)

data class VenueDetails(
    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: VenueAddress?,

    @SerializedName("city")
    val city: VenueCity?,

    @SerializedName("state")
    val state: VenueState?,

    @SerializedName("location")
    val location: VenueLocation?
)

data class VenueAddress(
    @SerializedName("line1")
    val line1: String?
)

data class VenueCity(
    @SerializedName("name")
    val name: String?
)

data class VenueState(
    @SerializedName("name")
    val name: String?,

    @SerializedName("stateCode")
    val stateCode: String?
)

data class VenueLocation(
    @SerializedName("latitude")
    val latitude: String?,

    @SerializedName("longitude")
    val longitude: String?
)