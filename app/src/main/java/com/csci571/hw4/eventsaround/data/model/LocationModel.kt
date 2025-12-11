package com.csci571.hw4.eventsaround.data.model

import com.google.gson.annotations.SerializedName

/**
 * Location-related models
 */

// IPInfo location response
data class IPLocation(
    @SerializedName("loc")
    val loc: String  // Format: "latitude,longitude"
) {
    val latitude: Double
        get() = loc.split(",")[0].toDoubleOrNull() ?: 0.0

    val longitude: Double
        get() = loc.split(",")[1].toDoubleOrNull() ?: 0.0
}

// Google Geocoding response (if you use it)
data class GeocodeResponse(
    @SerializedName("results")
    val results: List<GeocodeResult>
)

data class GeocodeResult(
    @SerializedName("geometry")
    val geometry: GeocodeGeometry
)

data class GeocodeGeometry(
    @SerializedName("location")
    val location: GeocodeLocation
)

data class GeocodeLocation(
    @SerializedName("lat")
    val lat: Double,

    @SerializedName("lng")
    val lng: Double
)