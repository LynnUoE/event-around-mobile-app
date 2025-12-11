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
    }

    fun toQueryMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map["keyword"] = keyword
        map["distance"] = distance.toString()

        if (category != CATEGORY_DEFAULT) {
            map["category"] = category
        }

        if (autoDetect && latitude != null && longitude != null) {
            map["lat"] = latitude.toString()
            map["lng"] = longitude.toString()
        } else if (location.isNotEmpty()) {
            map["location"] = location
        }

        return map
    }

    fun isValid(): Boolean {
        if (keyword.isBlank()) return false
        if (autoDetect && (latitude == null || longitude == null)) return false
        if (!autoDetect && location.isBlank()) return false
        return true
    }
}

data class AutocompleteResponse(
    @SerializedName("suggestions")
    val suggestions: List<String>
)