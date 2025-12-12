package com.csci571.hw4.eventsaround.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Enhanced SearchBar component with autocomplete suggestions
 *
 * Features:
 * - Real-time autocomplete suggestions dropdown
 * - User input as first suggestion option
 * - Loading indicator while fetching suggestions
 * - Clear button to reset input
 * - Error state with red border and message
 * - Keyboard handling (submit on Enter)
 * - Focus management
 *
 * @param query Current search query text
 * @param onQueryChange Callback when query text changes
 * @param onSearch Callback when search is submitted (Enter key or search icon)
 * @param suggestions List of autocomplete suggestions from API
 * @param onSuggestionSelected Callback when a suggestion is clicked
 * @param isLoadingSuggestions Whether suggestions are currently loading
 * @param placeholder Placeholder text for the search field
 * @param isError Whether to show error state (red border)
 * @param errorMessage Error message to display below the field
 * @param enabled Whether the search bar is enabled for input
 * @param showLabel Whether to show the "Keywords *" label (default: true)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    suggestions: List<String> = emptyList(),
    onSuggestionSelected: (String) -> Unit = {},
    isLoadingSuggestions: Boolean = false,
    placeholder: String = "Search events...",
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true,
    showLabel: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Show suggestions when focused and has query or suggestions
    LaunchedEffect(isFocused, query, suggestions) {
        showSuggestions = isFocused && query.trim().isNotEmpty()
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Optional label
        if (showLabel) {
            Text(
                text = "Keywords *",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Search input field with dropdown
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Loading indicator
                        if (isLoadingSuggestions) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = Color.Gray
                            )
                        }

                        // Clear button
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onQueryChange("")
                                    focusRequester.requestFocus()
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Dropdown indicator
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                enabled = enabled,
                singleLine = true,
                isError = isError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isError) Color.Red else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isError) Color.Red else Color.LightGray,
                    errorBorderColor = Color.Red,
                    disabledBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(4.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        onSearch()
                        showSuggestions = false
                    }
                )
            )

            // Autocomplete suggestions dropdown
            if (showSuggestions) {
                DropdownMenu(
                    expanded = showSuggestions,
                    onDismissRequest = { showSuggestions = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 240.dp)
                        .shadow(8.dp, RoundedCornerShape(4.dp)),
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    // First option: User's exact input
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = query.trim(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            onSuggestionSelected(query.trim())
                            showSuggestions = false
                            keyboardController?.hide()
                        },
                        modifier = Modifier.background(
                            color = Color(0xFFF5F5F5)
                        )
                    )

                    // Divider after user input
                    if (suggestions.isNotEmpty()) {
                        Divider(thickness = 1.dp, color = Color.LightGray)
                    }

                    // API suggestions (filter out duplicates of user input)
                    suggestions
                        .filter { it.trim().lowercase() != query.trim().lowercase() }
                        .take(7) // Show max 7 additional suggestions
                        .forEach { suggestion ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = suggestion,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                onClick = {
                                    onSuggestionSelected(suggestion)
                                    showSuggestions = false
                                    keyboardController?.hide()
                                }
                            )
                        }
                }
            }
        }

        // Error message
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = Color.Red,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

/**
 * Preview function for SearchBar with suggestions
 */
@Composable
fun SearchBarPreview() {
    var query by remember { mutableStateOf("Lakers") }
    var suggestions by remember {
        mutableStateOf(
            listOf(
                "Los Angeles Lakers",
                "Lakers vs Warriors",
                "Lakers Game",
                "LA Lakers Tickets"
            )
        )
    }

    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { /* Handle search */ },
                suggestions = suggestions,
                onSuggestionSelected = { query = it },
                isLoadingSuggestions = false,
                placeholder = "Search events...",
                isError = false,
                errorMessage = ""
            )
        }
    }
}