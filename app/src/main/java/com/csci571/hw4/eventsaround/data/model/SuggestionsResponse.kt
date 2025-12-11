package com.csci571.hw4.eventsaround.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuggestionsResponse(
    @SerialName("_embedded") val embedded: EmbeddedSuggestions? = null
)

@Serializable
data class EmbeddedSuggestions(
    val attractions: List<Suggestion> = emptyList()
)

@Serializable
data class Suggestion(
    val id: String,
    val name: String,
    val type: String? = null,
    val url: String? = null
)