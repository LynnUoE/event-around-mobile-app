package com.csci571.hw4.eventsaround

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.csci571.hw4.eventsaround.ui.test.ComponentsTestScreen
import com.csci571.hw4.eventsaround.ui.theme.EventsAroundTheme

/**
 * Test Activity for Components
 *
 * HOW TO USE:
 * 1. Temporarily replace MainActivity content with this test activity
 * 2. OR create a new activity in AndroidManifest.xml
 *
 * To replace MainActivity temporarily:
 * In MainActivity.kt, change setContent to:
 *
 * setContent {
 *     EventsAroundTheme {
 *         Surface(
 *             modifier = Modifier.fillMaxSize(),
 *             color = MaterialTheme.colorScheme.background
 *         ) {
 *             ComponentsTestScreen()
 *         }
 *     }
 * }
 */
class TestMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EventsAroundTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ComponentsTestScreen()
                }
            }
        }
    }
}

/**
 * Quick test launcher for MainActivity
 * Add this to your existing MainActivity temporarily for testing
 */
@Composable
fun QuickComponentTest() {
    EventsAroundTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ComponentsTestScreen()
        }
    }
}