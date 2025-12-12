package com.csci571.hw4.eventsaround.data.model

import com.google.gson.annotations.SerializedName

/**
 * Complete event details from Ticketmaster API
 * Note: VenueCity, VenueState, VenueLocation are defined in Event.kt
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
    val dates: EventDetailsDate?,  // ✅ 使用 EventDetailsDate 避免冲突

    @SerializedName("classifications")
    val classifications: List<EventClassification>?,

    @SerializedName("priceRanges")
    val priceRanges: List<PriceRange>?,

    @SerializedName("seatmap")
    val seatmap: Seatmap?,

    @SerializedName("_embedded")
    val _embedded: EventDetailsEmbedded?
)

// ========================================
// Date and Time information (renamed to avoid conflict)
// ========================================

data class EventDetailsDate(  // ✅ 重命名避免与 Event.kt 中的 EventDates 冲突
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
    val code: String?  // Values: "onsale", "offsale", "canceled", "postponed", etc.
)

// ========================================
// Classification (Category/Genre) information
// ========================================

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

// ========================================
// Seatmap information
// ========================================

data class Seatmap(
    @SerializedName("staticUrl")
    val staticUrl: String?
)

// ========================================
// Embedded data (Venues and Attractions)
// ========================================

data class EventDetailsEmbedded(
    @SerializedName("venues")
    val venues: List<VenueDetails>?,

    @SerializedName("attractions")
    val attractions: List<Attraction>?  // Attraction is defined in Event.kt
)

// ========================================
// Venue information (VenueDetails to avoid conflict with Venue)
// ========================================

data class VenueDetails(
    @SerializedName("name")
    val name: String,

    @SerializedName("url")
    val url: String? = null,  // ✅ Add Ticketmaster URL for venue

    @SerializedName("images")
    val images: List<EventImage>? = null,  // ✅ Add venue images

    @SerializedName("address")
    val address: VenueAddress?,

    @SerializedName("city")
    val city: VenueCity?,  // ✅ VenueCity is defined in Event.kt

    @SerializedName("state")
    val state: VenueState?,  // ✅ VenueState is defined in Event.kt

    @SerializedName("postalCode")
    val postalCode: String? = null,  // ✅ Add postal code

    @SerializedName("location")
    val location: VenueLocation?  // ✅ VenueLocation is defined in Event.kt
)

data class VenueAddress(
    @SerializedName("line1")
    val line1: String?
)
// ========================================
// Note: VenueCity, VenueState, VenueLocation are NOT defined here
// They are defined in Event.kt and shared between both files
// ========================================