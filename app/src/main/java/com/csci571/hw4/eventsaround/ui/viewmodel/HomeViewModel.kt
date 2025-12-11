package com.csci571.hw4.eventsaround.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.csci571.hw4.eventsaround.data.model.Event
import com.csci571.hw4.eventsaround.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Home Screen
 * Manages favorite events list
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesRepository = FavoritesRepository.getInstance(application)

    // Favorites list
    private val _favorites = MutableStateFlow<List<Event>>(emptyList())
    val favorites: StateFlow<List<Event>> = _favorites.asStateFlow()

    init {
        loadFavorites()
    }

    /**
     * Load all favorite events
     */
    fun loadFavorites() {
        viewModelScope.launch {
            _favorites.value = favoritesRepository.getAllFavorites()
        }
    }

    /**
     * Remove event from favorites
     */
    fun removeFavorite(eventId: String) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(eventId)
            loadFavorites()
        }
    }
}