package com.csci571.hw4.eventsaround

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.csci571.hw4.eventsaround.ui.navigation.AppNavigation
import com.csci571.hw4.eventsaround.ui.screens.SplashScreen
import com.csci571.hw4.eventsaround.ui.theme.EventsAroundTheme
import kotlinx.coroutines.delay

/**
 * Main Activity - Entry point of the application
 * Handles splash screen display and navigation setup
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch from splash theme to normal theme before super.onCreate()
        setTheme(R.style.Theme_EventFinder)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EventsAroundTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // State to control splash screen visibility
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        // Show splash screen for 2 seconds
                        SplashScreen(
                            onSplashFinished = {
                                showSplash = false
                            }
                        )
                    } else {
                        // Show main app navigation after splash
                        AppNavigation()
                    }
                }
            }
        }
    }
}