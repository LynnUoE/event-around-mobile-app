package com.csci571.hw4.eventsaround.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Main response wrapper for event search
@Serializable
data class EventsResponse(
    @SerialName("_embedded")
    val embedded: EmbeddedEvents? = null,
    val page: PageInfo? = null
)

@Serializable
data class EmbeddedEvents(
    val events: List<Event> = emptyList()
)

// Main Event model
@Serializable
data class Event(
    val id: String = "",
    val name: String = "",
    val url: String? = null,
    val images: List<EventImage> = emptyList(),
    val dates: EventDates? = null,
    val classifications: List<Classification> = emptyList(),
    @SerialName("_embedded")
    val embedded: EventEmbedded? = null,
    val priceRanges: List<PriceRange>? = null,
    val seatmap: SeatMap? = null
)

// Event Image
@Serializable
data class EventImage(
    val url: String = "",
    val ratio: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

// Date and Time information
@Serializable
data class EventDates(
    val start: DateStart? = null,
    val status: DateStatus? = null,
    val timezone: String? = null
)

@Serializable
data class DateStart(
    val localDate: String? = null,
    val localTime: String? = null,
    val dateTime: String? = null
)

@Serializable
data class DateStatus(
    val code: String? = null
)

// Classification - Category information
@Serializable
data class Classification(
    val segment: Segment? = null,
    val genre: Genre? = null,
    val subGenre: SubGenre? = null
)

@Serializable
data class Segment(
    val id: String? = null,
    val name: String? = null
)

@Serializable
data class Genre(
    val id: String? = null,
    val name: String? = null
)

@Serializable
data class SubGenre(
    val id: String? = null,
    val name: String? = null
)

// Embedded data - venues and attractions
@Serializable
data class EventEmbedded(
    val venues: List<Venue> = emptyList(),
    val attractions: List<Attraction> = emptyList()
)

// Venue information
@Serializable
data class Venue(
    val id: String = "",
    val name: String = "",
    val url: String? = null,
    val city: City? = null,
    val state: State? = null,
    val location: Location? = null,
    val address: Address? = null,
    val images: List<EventImage> = emptyList()
)

@Serializable
data class City(
    val name: String? = null
)

@Serializable
data class State(
    val name: String? = null,
    val stateCode: String? = null
)

@Serializable
data class Location(
    val longitude: String? = null,
    val latitude: String? = null
)

@Serializable
data class Address(
    val line1: String? = null
)

// Attraction - Artist or Team
@Serializable
data class Attraction(
    val id: String = "",
    val name: String = "",
    val url: String? = null,
    val images: List<EventImage> = emptyList(),
    val classifications: List<Classification> = emptyList()
)

// Price Range
@Serializable
data class PriceRange(
    val type: String? = null,
    val currency: String? = null,
    val min: Double? = null,
    val max: Double? = null
)

// Seat Map
@Serializable
data class SeatMap(
    val staticUrl: String? = null
)

// Page information
@Serializable
data class PageInfo(
    val size: Int = 0,
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val number: Int = 0
)