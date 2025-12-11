package com.csci571.hw4.eventsaround.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteEvent(
    val id: String,
    val name: String,
    val date: String,
    val venue: String,
    val category: String,
    val imageUrl: String
)