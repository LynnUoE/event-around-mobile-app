package com.csci571.hw4.eventsaround.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.csci571.hw4.eventsaround.data.model.SearchParams
import com.csci571.hw4.eventsaround.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Search Screen - Event search form with autocomplete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToResults: () -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: SearchViewModel = viewModel()
) {
    // Form state variables
    var keyword by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf(10) }
    var selectedCategory by remember { mutableIntStateOf(0) }
    var useCurrentLocation by remember { mutableStateOf(true) }
    var manualLocation by remember { mutableStateOf("") }

    // Autocomplete state
    val suggestions by viewModel.suggestions.collectAsState()
    var showSuggestions by remember { mutableStateOf(false) }

    // Error states
    var showKeywordError by remember { mutableStateOf(false) }
    var showLocationError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Category names and mapping
    val categories = listOf("All", "Music", "Sports", "Arts & Theatre", "Film", "Miscellaneous")
    val categorySegmentIds = mapOf(
        0 to SearchParams.CATEGORY_ALL,
        1 to SearchParams.CATEGORY_MUSIC,
        2 to SearchParams.CATEGORY_SPORTS,
        3 to SearchParams.CATEGORY_ARTS,
        4 to SearchParams.CATEGORY_FILM,
        5 to SearchParams.CATEGORY_MISCELLANEOUS
    )

    // Debounced autocomplete fetching
    LaunchedEffect(keyword) {
        if (keyword.trim().length > 0) {
            delay(300) // Debounce 300ms
            viewModel.getAutocompleteSuggestions(keyword)
            showSuggestions = suggestions.isNotEmpty()
        } else {
            showSuggestions = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Search input field with autocomplete
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = keyword,
                            onValueChange = {
                                keyword = it
                                showKeywordError = false
                            },
                            placeholder = {
                                Text(
                                    "Search events...",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                            keyboardOptions = KeyboardOptions(
                                autoCorrect = false,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            trailingIcon = {
                                if (keyword.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            keyword = ""
                                            showSuggestions = false
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        )

                        // Autocomplete dropdown
                        if (showSuggestions && suggestions.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 56.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                ) {
                                    // First item: user's input
                                    item {
                                        Text(
                                            text = keyword,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    showSuggestions = false
                                                }
                                                .padding(12.dp),
                                            fontSize = 14.sp
                                        )
                                        HorizontalDivider()
                                    }

                                    // API suggestions
                                    items(suggestions.filter {
                                        it.lowercase() != keyword.lowercase()
                                    }) { suggestion ->
                                        Text(
                                            text = suggestion,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    keyword = suggestion
                                                    showSuggestions = false
                                                }
                                                .padding(12.dp),
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Validate and perform search
                        showKeywordError = keyword.isBlank()
                        showLocationError = !useCurrentLocation && manualLocation.isBlank()

                        if (!showKeywordError && !showLocationError) {
                            val searchParams = SearchParams(
                                keyword = keyword.trim(),
                                distance = distance,
                                category = categorySegmentIds[selectedCategory] ?: "",
                                autoDetect = useCurrentLocation,
                                location = if (!useCurrentLocation) manualLocation else "",
                                latitude = null,
                                longitude = null
                            )
                            viewModel.searchEvents(searchParams)
                            onNavigateToResults()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Error message for keyword
            if (showKeywordError) {
                Text(
                    text = "Keyword is required",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Location and Distance row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Location selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            useCurrentLocation = !useCurrentLocation
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (useCurrentLocation) "Current Location" else "Other",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Distance controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { if (distance > 1) distance-- },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease distance",
                            tint = Color.Gray
                        )
                    }

                    Text(
                        text = distance.toString(),
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.widthIn(min = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { distance++ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase distance",
                            tint = Color.Gray
                        )
                    }

                    Text(
                        text = "mi",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }

            // Manual location input
            if (!useCurrentLocation) {
                OutlinedTextField(
                    value = manualLocation,
                    onValueChange = {
                        manualLocation = it
                        showLocationError = false
                    },
                    placeholder = { Text("Enter location") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    isError = showLocationError,
                    singleLine = true
                )
                if (showLocationError) {
                    Text(
                        text = "Location is required",
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Category tabs
            ScrollableTabRow(
                selectedTabIndex = selectedCategory,
                containerColor = Color.Transparent,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedCategory == index,
                        onClick = { selectedCategory = index },
                        text = {
                            Text(
                                text = category,
                                fontSize = 14.sp,
                                color = if (selectedCategory == index) Color.Blue else Color.Gray
                            )
                        }
                    )
                }
            }

            HorizontalDivider()

            // Results area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "No events found",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}