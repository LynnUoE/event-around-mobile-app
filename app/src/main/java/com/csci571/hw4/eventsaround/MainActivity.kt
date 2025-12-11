package com.csci571.hw4.eventsaround

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.csci571.hw4.eventsaround.ui.navigation.AppNavigation
import com.csci571.hw4.eventsaround.ui.screens.SplashScreen
import com.csci571.hw4.eventsaround.ui.theme.EventsAroundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch from splash theme to normal theme before super.onCreate()
        setTheme(R.style.Theme_EventFinder)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SimpleDataLayerTest.runQuickTests(this)

        setContent {
            EventsAroundTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = {
                            showSplash = false
                        }
                    )
                } else {
                    // Main app navigation
                    AppNavigation()
                }
            }
        }
    }
}