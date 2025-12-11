package com.csci571.hw4.eventsaround.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csci571.hw4.eventsaround.R
import kotlinx.coroutines.delay

/**
 * Splash Screen - Displayed when app starts
 * Shows app logo for 2 seconds before navigating to main content
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Auto-navigate after 2 seconds
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // App logo/icon
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp)
        )
    }
}