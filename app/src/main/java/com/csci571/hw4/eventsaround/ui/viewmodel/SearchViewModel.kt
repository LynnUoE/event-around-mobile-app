package com.csci571.hw4.eventsaround.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.csci571.hw4.eventsaround.data.model.Event
import com.csci571.hw4.eventsaround.data.model.SearchParams
import com.csci571.hw4.eventsaround.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Search and Results screens
 * Manages search state, results, and loading states
 */
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EventRepository.getInstance()

    // Search results state
    private val _searchResults = MutableStateFlow<List<Event>>(emptyList())
    val searchResults: StateFlow<List<Event>> = _searchResults.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Autocomplete suggestions
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    // Last search params for reference
    private var lastSearchParams: SearchParams? = null

    /**
     * Perform event search
     */
    fun searchEvents(params: SearchParams) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                lastSearchParams = params

                val result = repository.searchEvents(params)

                if (result.isSuccess) {
                    _searchResults.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message
                        ?: "Failed to search events"
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get autocomplete suggestions
     */
    fun getAutocompleteSuggestions(keyword: String) {
        if (keyword.length < 2) {
            _suggestions.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val result = repository.getAutocompleteSuggestions(keyword)

                if (result.isSuccess) {
                    _suggestions.value = result.getOrNull() ?: emptyList()
                } else {
                    _suggestions.value = emptyList()
                }
            } catch (e: Exception) {
                _suggestions.value = emptyList()
            }
        }
    }

    /**
     * Clear search results
     */
    fun clearResults() {
        _searchResults.value = emptyList()
        _error.value = null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Get last search parameters
     */
    fun getLastSearchParams(): SearchParams? = lastSearchParams
}