package com.csci571.hw4.eventsaround.data.model

import com.google.gson.annotations.SerializedName

// Main response wrapper for event search
data class EventsResponse(
    @SerializedName("_embedded")
    val embedded: EmbeddedEvents? = null,

    @SerializedName("page")
    val page: PageInfo? = null
)

data class EmbeddedEvents(
    @SerializedName("events")
    val events: List<Event> = emptyList()
)

// Main Event model
data class Event(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("images")
    val images: List<EventImage> = emptyList(),

    @SerializedName("dates")
    val dates: EventDates? = null,

    @SerializedName("classifications")
    val classifications: List<Classification> = emptyList(),

    @SerializedName("_embedded")
    val embedded: EventEmbedded? = null,

    @SerializedName("priceRanges")
    val priceRanges: List<PriceRange>? = null,

    @SerializedName("seatmap")
    val seatmap: SeatMap? = null
)

// Event Image
data class EventImage(
    @SerializedName("url")
    val url: String = "",

    @SerializedName("ratio")
    val ratio: String? = null,

    @SerializedName("width")
    val width: Int? = null,

    @SerializedName("height")
    val height: Int? = null
)

// Date and Time information
data class EventDates(
    @SerializedName("start")
    val start: DateStart? = null,

    @SerializedName("status")
    val status: DateStatus? = null,

    @SerializedName("timezone")
    val timezone: String? = null
)

data class DateStart(
    @SerializedName("localDate")
    val localDate: String? = null,

    @SerializedName("localTime")
    val localTime: String? = null,

    @SerializedName("dateTime")
    val dateTime: String? = null
)

data class DateStatus(
    @SerializedName("code")
    val code: String? = null
)

// Classification - Category information
data class Classification(
    @SerializedName("segment")
    val segment: Segment? = null,

    @SerializedName("genre")
    val genre: Genre? = null,

    @SerializedName("subGenre")
    val subGenre: SubGenre? = null
)

data class Segment(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null
)

data class Genre(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null
)

data class SubGenre(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null
)

// Embedded data - venues and attractions
data class EventEmbedded(
    @SerializedName("venues")
    val venues: List<Venue> = emptyList(),

    @SerializedName("attractions")
    val attractions: List<Attraction> = emptyList()
)

// Venue information
data class Venue(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("city")
    val city: City? = null,

    @SerializedName("state")
    val state: State? = null,

    @SerializedName("location")
    val location: Location? = null,

    @SerializedName("address")
    val address: Address? = null,

    @SerializedName("images")
    val images: List<EventImage> = emptyList()
)

data class City(
    @SerializedName("name")
    val name: String? = null
)

data class State(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("stateCode")
    val stateCode: String? = null
)

data class Location(
    @SerializedName("longitude")
    val longitude: String? = null,

    @SerializedName("latitude")
    val latitude: String? = null
)

data class Address(
    @SerializedName("line1")
    val line1: String? = null
)

// Attraction - Artist or Team
data class Attraction(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("images")
    val images: List<EventImage> = emptyList(),

    @SerializedName("classifications")
    val classifications: List<Classification> = emptyList()
)

// Price Range
data class PriceRange(
    @SerializedName("type")
    val type: String? = null,

    @SerializedName("currency")
    val currency: String? = null,

    @SerializedName("min")
    val min: Double? = null,

    @SerializedName("max")
    val max: Double? = null
)

// Seat Map
data class SeatMap(
    @SerializedName("staticUrl")
    val staticUrl: String? = null
)

// Page information
data class PageInfo(
    @SerializedName("size")
    val size: Int = 0,

    @SerializedName("totalElements")
    val totalElements: Int = 0,

    @SerializedName("totalPages")
    val totalPages: Int = 0,

    @SerializedName("number")
    val number: Int = 0
)