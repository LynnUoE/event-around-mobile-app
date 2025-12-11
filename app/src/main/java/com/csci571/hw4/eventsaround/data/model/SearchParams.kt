package com.csci571.hw4.eventsaround.data.model

import com.google.gson.annotations.SerializedName

data class SearchParams(
    val keyword: String,
    val distance: Int = 10,
    val category: String = "Default",
    val location: String = "",
    val autoDetect: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    companion object {
        const val DEFAULT_DISTANCE = 10
        const val CATEGORY_DEFAULT = "Default"
        const val CATEGORY_MUSIC = "Music"
        const val CATEGORY_SPORTS = "Sports"
        const val CATEGORY_ARTS = "Arts & Theatre"
        const val CATEGORY_FILM = "Film"
        const val CATEGORY_MISCELLANEOUS = "Miscellaneous"

        // Default Los Angeles coordinates
        const val DEFAULT_LAT = 34.0522
        const val DEFAULT_LNG = -118.2437
    }

    fun toQueryMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()

        map["keyword"] = keyword
        map["distance"] = distance.toString()

        if (category != CATEGORY_DEFAULT) {
            map["category"] = category
        }

        // Always need coordinates for this API
        val lat = latitude ?: DEFAULT_LAT
        val lng = longitude ?: DEFAULT_LNG

        map["lat"] = lat.toString()
        map["lng"] = lng.toString()

        return map
    }

    fun isValid(): Boolean {
        return keyword.isNotBlank()
    }
}

data class AutocompleteResponse(
    @SerializedName("suggestions")
    val suggestions: List<String>
)