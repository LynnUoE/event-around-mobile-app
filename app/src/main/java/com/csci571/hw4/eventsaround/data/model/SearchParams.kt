package com.csci571.hw4.eventsaround.data.model

import com.google.gson.annotations.SerializedName

data class SearchParams(
    val keyword: String,
    val distance: Int = 10,
    val category: String = "",  // 改为空字符串默认值
    val location: String = "",
    val autoDetect: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    companion object {
        const val DEFAULT_DISTANCE = 10

        // Category segment IDs (这些是Ticketmaster的实际ID)
        const val CATEGORY_ALL = ""
        const val CATEGORY_MUSIC = "KZFzniwnSyZfZ7v7nJ"
        const val CATEGORY_SPORTS = "KZFzniwnSyZfZ7v7nE"
        const val CATEGORY_ARTS = "KZFzniwnSyZfZ7v7na"
        const val CATEGORY_FILM = "KZFzniwnSyZfZ7v7nn"
        const val CATEGORY_MISCELLANEOUS = "KZFzniwnSyZfZ7v7n1"

        // Default Los Angeles coordinates
        const val DEFAULT_LAT = 34.0522
        const val DEFAULT_LNG = -118.2437
    }

    fun toQueryMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()

        map["keyword"] = keyword
        map["radius"] = distance.toString()  // 改为 "radius"

        if (category.isNotEmpty()) {
            map["segmentId"] = category  // 改为 "segmentId"
        }

        // 使用提供的坐标或默认坐标
        val lat = latitude ?: DEFAULT_LAT
        val lng = longitude ?: DEFAULT_LNG

        map["lat"] = lat.toString()
        map["lng"] = lng.toString()

        return map
    }

    fun isValid(): Boolean {
        return keyword.isNotBlank() && distance > 0
    }
}

// Autocomplete response
data class AutocompleteResponse(
    @SerializedName("_embedded")
    val _embedded: AutocompleteEmbedded?
)

data class AutocompleteEmbedded(
    @SerializedName("attractions")
    val attractions: List<AttractionSuggestion>
)

data class AttractionSuggestion(
    @SerializedName("name")
    val name: String,

    @SerializedName("id")
    val id: String
)