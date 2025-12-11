package com.csci571.hw4.eventsaround.data.model

data class SearchParams(
    val keyword: String,
    val distance: Int = 10,
    val category: String = "All",
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val useCurrentLocation: Boolean = true
)