package com.csci571.hw4.eventsaround.data.model

import com.google.gson.annotations.SerializedName

/**
 * Spotify Artist data model
 */
data class SpotifyArtist(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("followers")
    val followers: SpotifyFollowers? = null,

    @SerializedName("popularity")
    val popularity: Int = 0,

    @SerializedName("images")
    val images: List<SpotifyImage>? = null,

    @SerializedName("genres")
    val genres: List<String>? = null,

    @SerializedName("external_urls")
    val externalUrls: SpotifyExternalUrls? = null
) {
    /**
     * Format followers count as M (millions) or K (thousands)
     */
    fun getFormattedFollowers(): String {
        val total = followers?.total ?: return "0"
        return when {
            total >= 1_000_000 -> "${total / 1_000_000}M"
            total >= 1_000 -> "${total / 1_000}K"
            else -> total.toString()
        }
    }

    /**
     * Get Spotify URL for the artist
     */
    fun getSpotifyUrl(): String {
        return externalUrls?.spotify ?: ""
    }

    /**
     * Get best quality image URL
     */
    fun getImageUrl(): String {
        return images?.firstOrNull()?.url ?: ""
    }
}

data class SpotifyFollowers(
    @SerializedName("total")
    val total: Int
)

data class SpotifyImage(
    @SerializedName("url")
    val url: String,

    @SerializedName("height")
    val height: Int? = null,

    @SerializedName("width")
    val width: Int? = null
)

data class SpotifyExternalUrls(
    @SerializedName("spotify")
    val spotify: String
)

/**
 * Response wrapper for artist search
 */
data class SpotifyArtistSearchResponse(
    @SerializedName("artist")
    val artist: SpotifyArtist
)

/**
 * Response wrapper for albums
 */
data class SpotifyAlbumsResponse(
    @SerializedName("albums")
    val albums: List<Album>
)

/**
 * Venue details from Google Places API
 */
data class VenueDetails(
    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("city")
    val city: String? = null,

    @SerializedName("state")
    val state: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("openHours")
    val openHours: String? = null,

    @SerializedName("website")
    val website: String? = null,

    @SerializedName("location")
    val location: Location? = null,

    @SerializedName("photos")
    val photos: List<String>? = null
)