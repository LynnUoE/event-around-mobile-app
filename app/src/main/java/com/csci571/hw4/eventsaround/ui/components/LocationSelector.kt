package com.csci571.hw4.eventsaround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Location Selector Component
 * Shows a button with current location or manual location
 * Opens a dialog for location selection with autocomplete
 */
@Composable
fun LocationSelector(
    useCurrentLocation: Boolean,
    manualLocation: String,
    onLocationTypeChange: (Boolean) -> Unit,
    onManualLocationChange: (String) -> Unit,
    onLocationSuggestionSelected: (String) -> Unit,
    locationSuggestions: List<String>,
    onLoadLocationSuggestions: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    // Location display button
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        // Clickable location display
        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable { showDialog = true },
            color = Color.Transparent
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (useCurrentLocation) "Current Location" else manualLocation.ifBlank { "Select Location" },
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Change location",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Swap/toggle icon
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Toggle location mode",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Location selection dialog
    if (showDialog) {
        LocationPickerDialog(
            useCurrentLocation = useCurrentLocation,
            manualLocation = manualLocation,
            locationSuggestions = locationSuggestions,
            onDismiss = { showDialog = false },
            onLocationTypeChange = {
                onLocationTypeChange(it)
                if (it) {
                    // If switching to current location, close dialog
                    showDialog = false
                }
            },
            onManualLocationChange = onManualLocationChange,
            onLocationSelected = {
                onLocationSuggestionSelected(it)
                showDialog = false
            },
            onLoadSuggestions = onLoadLocationSuggestions
        )
    }
}

/**
 * Dialog for selecting location type and manual location input
 */
@Composable
private fun LocationPickerDialog(
    useCurrentLocation: Boolean,
    manualLocation: String,
    locationSuggestions: List<String>,
    onDismiss: () -> Unit,
    onLocationTypeChange: (Boolean) -> Unit,
    onManualLocationChange: (String) -> Unit,
    onLocationSelected: (String) -> Unit,
    onLoadSuggestions: (String) -> Unit
) {
    var tempUseCurrentLocation by remember { mutableStateOf(useCurrentLocation) }
    var tempManualLocation by remember { mutableStateOf(manualLocation) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Dialog title
                Text(
                    text = "Select Location",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Radio button for Current Location
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            tempUseCurrentLocation = true
                            onLocationTypeChange(true)
                            onDismiss()
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tempUseCurrentLocation,
                        onClick = {
                            tempUseCurrentLocation = true
                            onLocationTypeChange(true)
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Location",
                        fontSize = 16.sp
                    )
                }

                // Radio button for Manual Location
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { tempUseCurrentLocation = false }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !tempUseCurrentLocation,
                        onClick = { tempUseCurrentLocation = false }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Manual Location",
                        fontSize = 16.sp
                    )
                }

                // Manual location input
                if (!tempUseCurrentLocation) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tempManualLocation,
                        onValueChange = { newValue ->
                            tempManualLocation = newValue
                            onManualLocationChange(newValue)

                            // Trigger suggestions loading with debounce
                            coroutineScope.launch {
                                delay(300) // Debounce delay
                                if (newValue.length >= 2) {
                                    onLoadSuggestions(newValue)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter location (e.g., Boston, MA, USA)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Location suggestions
                    if (locationSuggestions.isNotEmpty() && tempManualLocation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(locationSuggestions) { suggestion ->
                                    Text(
                                        text = suggestion,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                tempManualLocation = suggestion
                                                onLocationSelected(suggestion)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        fontSize = 14.sp
                                    )
                                    if (suggestion != locationSuggestions.last()) {
                                        Divider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!tempUseCurrentLocation && tempManualLocation.isNotBlank()) {
                                onLocationTypeChange(false)
                                onLocationSelected(tempManualLocation)
                            } else if (!tempUseCurrentLocation) {
                                // Show error or do nothing if manual location is blank
                                return@Button
                            }
                            onDismiss()
                        },
                        enabled = tempUseCurrentLocation || tempManualLocation.isNotBlank()
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}