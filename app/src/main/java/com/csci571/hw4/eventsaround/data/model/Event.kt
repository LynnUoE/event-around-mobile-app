package com.csci571.hw4.eventsaround.data.model

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("dates")
    val dates: EventDates? = null,

    @SerializedName("_embedded")
    val embedded: EventEmbedded? = null,

    @SerializedName("classifications")
    val classifications: List<Classification>? = null,

    @SerializedName("images")
    val images: List<EventImage>? = null,

    @SerializedName("priceRanges")
    val priceRanges: List<PriceRange>? = null
) {
    // Helper properties for easier access
    val date: String
        get() = dates?.start?.localDate ?: ""

    val time: String
        get() = dates?.start?.localTime ?: ""

    val venue: String
        get() = embedded?.venues?.firstOrNull()?.name ?: ""

    val genre: String
        get() = classifications?.firstOrNull()?.genre?.name ?: ""

    val imageUrl: String
        get() = images?.firstOrNull()?.url ?: ""

    val category: String
        get() = classifications?.firstOrNull()?.segment?.name ?: ""

    val priceRange: String
        get() {
            val min = priceRanges?.firstOrNull()?.min
            val max = priceRanges?.firstOrNull()?.max
            return when {
                min != null && max != null -> "$$min - $$max"
                min != null -> "From $$min"
                else -> "N/A"
            }
        }
}

data class EventDates(
    @SerializedName("start")
    val start: EventDate?
)

data class EventDate(
    @SerializedName("localDate")
    val localDate: String?,

    @SerializedName("localTime")
    val localTime: String?
)

data class EventEmbedded(
    @SerializedName("venues")
    val venues: List<Venue>?,

    @SerializedName("attractions")
    val attractions: List<Attraction>?
)

data class Venue(
    @SerializedName("name")
    val name: String = "",

    @SerializedName("city")
    val city: VenueCity? = null,

    @SerializedName("state")
    val state: VenueState? = null,

    @SerializedName("location")
    val location: VenueLocation? = null
)

data class VenueCity(
    @SerializedName("name")
    val name: String = ""
)

data class VenueState(
    @SerializedName("stateCode")
    val stateCode: String = ""
)

data class VenueLocation(
    @SerializedName("latitude")
    val latitude: String = "",

    @SerializedName("longitude")
    val longitude: String = ""
)

data class Classification(
    @SerializedName("segment")
    val segment: Segment?,

    @SerializedName("genre")
    val genre: Genre?
)

data class Segment(
    @SerializedName("name")
    val name: String = ""
)

data class Genre(
    @SerializedName("name")
    val name: String = ""
)

data class EventImage(
    @SerializedName("url")
    val url: String = "",

    @SerializedName("width")
    val width: Int = 0,

    @SerializedName("height")
    val height: Int = 0
)

data class PriceRange(
    @SerializedName("min")
    val min: Double?,

    @SerializedName("max")
    val max: Double?
)

data class Attraction(
    @SerializedName("name")
    val name: String = "",

    @SerializedName("images")
    val images: List<EventImage>? = null,

    @SerializedName("url")
    val url: String? = null
)

/**
 * API Response wrapper
 */
data class EventListResponse(
    @SerializedName("_embedded")
    val embedded: EmbeddedEvents?
)

data class EmbeddedEvents(
    @SerializedName("events")
    val events: List<Event>
)