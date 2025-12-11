package com.csci571.hw4.eventsaround.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SearchBar component with autocomplete suggestions
 * Provides a text input field with dropdown suggestions as user types
 *
 * Features:
 * - Search icon and clear button
 * - Autocomplete dropdown with suggestions
 * - Error state display
 * - Keyboard handling (submit on Enter)
 * - Focus management
 *
 * @param query Current search query text
 * @param onQueryChange Callback when query text changes
 * @param onSearch Callback when search is submitted (Enter key or search button)
 * @param suggestions List of autocomplete suggestions to display
 * @param onSuggestionSelected Callback when a suggestion is clicked
 * @param placeholder Placeholder text for the search field
 * @param isError Whether to show error state (red border)
 * @param errorMessage Error message to display below the field
 * @param enabled Whether the search bar is enabled for input
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    suggestions: List<String> = emptyList(),
    onSuggestionSelected: (String) -> Unit = {},
    placeholder: String = "Search events...",
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Show suggestions when focused and has query text
    val showSuggestions = isFocused && query.isNotEmpty() && suggestions.isNotEmpty()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Main search input field
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                // Show clear button when text is not empty
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onQueryChange("")
                            keyboardController?.hide()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    onSearch()
                }
            ),
            isError = isError,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        // Error message display
        AnimatedVisibility(
            visible = isError && errorMessage.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        // Autocomplete suggestions dropdown
        AnimatedVisibility(
            visible = showSuggestions,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(suggestions) { suggestion ->
                        SuggestionItem(
                            text = suggestion,
                            query = query,
                            onClick = {
                                onSuggestionSelected(suggestion)
                                keyboardController?.hide()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual suggestion item in the dropdown list
 * Displays a search icon and suggestion text
 *
 * @param text The suggestion text to display
 * @param query The current search query (for potential highlighting)
 * @param onClick Callback when the suggestion is clicked
 */
@Composable
private fun SuggestionItem(
    text: String,
    query: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search icon for each suggestion
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Suggestion text
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        // Divider between suggestions (except for the last item)
        Divider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
    }
}

/**
 * Debounced search bar with automatic API calls for suggestions
 * Automatically fetches suggestions with a delay after user stops typing
 *
 * This is useful to avoid making too many API calls while user is typing.
 * It waits for the specified delay after the last keystroke before fetching suggestions.
 *
 * @param query Current search query text
 * @param onQueryChange Callback when query text changes
 * @param onSearch Callback for performing the main search
 * @param onGetSuggestions Suspend function to fetch autocomplete suggestions from API
 * @param placeholder Placeholder text for the search field
 * @param isError Whether to show error state
 * @param errorMessage Error message to display
 * @param debounceMillis Delay in milliseconds before triggering suggestion fetch (default: 300ms)
 * @param enabled Whether the search bar is enabled
 */
@Composable
fun DebouncedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onGetSuggestions: suspend (String) -> List<String>,
    placeholder: String = "Search events...",
    isError: Boolean = false,
    errorMessage: String = "",
    debounceMillis: Long = 300L,
    enabled: Boolean = true
) {
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingSuggestions by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Handle query changes with debounce for suggestions
    LaunchedEffect(query) {
        // Cancel previous job if user is still typing
        searchJob?.cancel()

        if (query.isNotEmpty()) {
            isLoadingSuggestions = true
            searchJob = coroutineScope.launch {
                // Wait for debounce delay
                delay(debounceMillis)

                try {
                    // Fetch suggestions from API
                    suggestions = onGetSuggestions(query)
                } catch (e: Exception) {
                    // If API call fails, clear suggestions
                    suggestions = emptyList()
                } finally {
                    isLoadingSuggestions = false
                }
            }
        } else {
            // Clear suggestions when query is empty
            suggestions = emptyList()
            isLoadingSuggestions = false
        }
    }

    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        suggestions = suggestions,
        onSuggestionSelected = { suggestion ->
            // When user selects a suggestion, update query and trigger search
            onQueryChange(suggestion)
            suggestions = emptyList()
            onSearch()
        },
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled
    )
}

/**
 * Compact search bar for displaying in app bar or toolbar
 * More minimal design without border, suitable for top app bars
 *
 * @param query Current search query text
 * @param onQueryChange Callback when query text changes
 * @param onSearch Callback when search is submitted
 * @param placeholder Placeholder text
 * @param modifier Modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onQueryChange("")
                        keyboardController?.hide()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch()
            }
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Simple search bar without autocomplete
 * Basic search input field for simple use cases
 *
 * @param query Current search query
 * @param onQueryChange Callback when query changes
 * @param onSearch Callback when search is submitted
 * @param placeholder Placeholder text
 * @param isError Whether to show error state
 * @param errorMessage Error message to display
 * @param modifier Modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String = "Search...",
    isError: Boolean = false,
    errorMessage: String = "",
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    onSearch()
                }
            ),
            shape = RoundedCornerShape(8.dp)
        )

        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}