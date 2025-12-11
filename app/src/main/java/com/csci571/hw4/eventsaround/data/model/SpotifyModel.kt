package com.csci571.hw4.eventsaround.data.model

import com.google.gson.annotations.SerializedName

/**
 * Spotify API response models
 */

// Artist search response
data class SpotifyArtistSearchResponse(
    @SerializedName("artists")
    val artists: SpotifyArtists?
)

data class SpotifyArtists(
    @SerializedName("items")
    val items: List<SpotifyArtist>
)

data class SpotifyArtist(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("followers")
    val followers: SpotifyFollowers,

    @SerializedName("popularity")
    val popularity: Int,

    @SerializedName("genres")
    val genres: List<String>,

    @SerializedName("images")
    val images: List<SpotifyImage>,

    @SerializedName("external_urls")
    val external_urls: SpotifyExternalUrls
)

data class SpotifyFollowers(
    @SerializedName("total")
    val total: Int
)

data class SpotifyImage(
    @SerializedName("url")
    val url: String,

    @SerializedName("height")
    val height: Int?,

    @SerializedName("width")
    val width: Int?
)

data class SpotifyExternalUrls(
    @SerializedName("spotify")
    val spotify: String?
)

// Albums response
data class SpotifyAlbumsResponse(
    @SerializedName("items")
    val items: List<Album>
)

data class Album(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("release_date")
    val release_date: String,

    @SerializedName("total_tracks")
    val total_tracks: Int,

    @SerializedName("images")
    val images: List<SpotifyImage>,

    @SerializedName("external_urls")
    val external_urls: SpotifyExternalUrls
)