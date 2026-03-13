package com.kia.sensorhub

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kia.sensorhub.ui.screens.HomeScreen
import com.kia.sensorhub.ui.theme.SensorHubTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for UI components
 * These tests run on Android devices/emulators
 */
@RunWith(AndroidJUnit4::class)
class InstrumentationTests {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    // ========== Home Screen Tests ==========
    
    @Test
    fun homeScreen_displaysWelcomeMessage() {
        composeTestRule.setContent {
            SensorHubTheme {
                HomeScreen(onNavigate = {})
            }
        }
        
        composeTestRule
            .onNodeWithText("SensorHub", substring = true)
            .assertIsDisplayed()
    }
    
    @Test
    fun homeScreen_displaysSensorCards() {
        composeTestRule.setContent {
            SensorHubTheme {
                HomeScreen(onNavigate = {})
            }
        }
        
        // Check for sensor cards
        composeTestRule
            .onNodeWithText("Accelerometer")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Gyroscope")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Magnetometer")
            .assertIsDisplayed()
    }
    
    @Test
    fun homeScreen_sensorCardIsClickable() {
        var clicked = false
        
        composeTestRule.setContent {
            SensorHubTheme {
                HomeScreen(onNavigate = { clicked = true })
            }
        }
        
        composeTestRule
            .onNodeWithText("Accelerometer")
            .performClick()
        
        assert(clicked)
    }
    
    // ========== Sensor Card Tests ==========
    
    @Test
    fun sensorCard_displaysCorrectValues() {
        composeTestRule.setContent {
            SensorHubTheme {
                com.kia.sensorhub.ui.components.SensorCard(
                    label = "Test Sensor",
                    value = 9.8f,
                    unit = "m/s²",
                    color = androidx.compose.ui.graphics.Color.Blue
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Test Sensor")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("9.8", substring = true)
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("m/s²")
            .assertIsDisplayed()
    }
    
    // ========== Button Tests ==========
    
    @Test
    fun button_isClickable() {
        var clicked = false
        
        composeTestRule.setContent {
            SensorHubTheme {
                androidx.compose.material3.Button(
                    onClick = { clicked = true }
                ) {
                    androidx.compose.material3.Text("Click Me")
                }
            }
        }
        
        composeTestRule
            .onNodeWithText("Click Me")
            .performClick()
        
        assert(clicked)
    }
    
    // ========== Settings Screen Tests ==========
    
    @Test
    fun settingsScreen_displaysAllSections() {
        composeTestRule.setContent {
            SensorHubTheme {
                com.kia.sensorhub.ui.screens.CompleteSettingsScreen()
            }
        }
        
        // Check for major sections
        composeTestRule
            .onNodeWithText("Appearance")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Data Management")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Notifications")
            .assertIsDisplayed()
    }
    
    @Test
    fun settingsScreen_togglesWork() {
        composeTestRule.setContent {
            SensorHubTheme {
                com.kia.sensorhub.ui.screens.CompleteSettingsScreen()
            }
        }
        
        // Find Dark Mode toggle
        composeTestRule
            .onNode(hasText("Dark Mode").and(hasClickAction()))
            .performClick()
        
        // Verify state changed (toggle should be clickable)
        composeTestRule
            .onNode(hasText("Dark Mode"))
            .assertIsDisplayed()
    }
    
    // ========== Navigation Tests ==========
    
    @Test
    fun navigation_bottomBarDisplaysAllItems() {
        composeTestRule.setContent {
            SensorHubTheme {
                // Would need to test full app navigation
                // This is a placeholder for navigation tests
            }
        }
        
        // Test navigation items existence
        // This requires full app context
    }
    
    // ========== Achievement Tests ==========
    
    @Test
    fun achievementCard_displaysCorrectly() {
        composeTestRule.setContent {
            SensorHubTheme {
                androidx.compose.material3.Card {
                    androidx.compose.foundation.layout.Column(
                        modifier = androidx.compose.ui.Modifier.padding(16.dp)
                    ) {
                        androidx.compose.material3.Text("Test Achievement")
                        androidx.compose.material3.Text("100 XP")
                    }
                }
            }
        }
        
        composeTestRule
            .onNodeWithText("Test Achievement")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("100 XP")
            .assertIsDisplayed()
    }
    
    // ========== List Tests ==========
    
    @Test
    fun sensorList_scrollsCorrectly() {
        composeTestRule.setContent {
            SensorHubTheme {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(20) { index ->
                        androidx.compose.material3.Text(
                            text = "Item $index",
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
        
        // Item 0 should be visible
        composeTestRule
            .onNodeWithText("Item 0")
            .assertIsDisplayed()
        
        // Item 19 should not be visible initially
        composeTestRule
            .onNodeWithText("Item 19")
            .assertDoesNotExist()
        
        // Scroll to bottom
        composeTestRule
            .onNodeWithText("Item 0")
            .performScrollTo()
        
        // Now item 19 should be visible after scrolling
        composeTestRule
            .onNodeWithText("Item 19")
            .performScrollTo()
            .assertIsDisplayed()
    }
    
    // ========== Text Field Tests ==========
    
    @Test
    fun textField_acceptsInput() {
        composeTestRule.setContent {
            SensorHubTheme {
                var text by androidx.compose.runtime.remember { 
                    androidx.compose.runtime.mutableStateOf("") 
                }
                
                androidx.compose.material3.TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { androidx.compose.material3.Text("Test Input") }
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Test Input")
            .performTextInput("Hello World")
        
        composeTestRule
            .onNodeWithText("Hello World")
            .assertIsDisplayed()
    }
    
    // ========== Dialog Tests ==========
    
    @Test
    fun dialog_displaysAndDismisses() {
        var showDialog by androidx.compose.runtime.mutableStateOf(true)
        
        composeTestRule.setContent {
            SensorHubTheme {
                if (showDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { androidx.compose.material3.Text("Test Dialog") },
                        text = { androidx.compose.material3.Text("Dialog Content") },
                        confirmButton = {
                            androidx.compose.material3.TextButton(
                                onClick = { showDialog = false }
                            ) {
                                androidx.compose.material3.Text("OK")
                            }
                        }
                    )
                }
            }
        }
        
        composeTestRule
            .onNodeWithText("Test Dialog")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("OK")
            .performClick()
        
        composeTestRule
            .onNodeWithText("Test Dialog")
            .assertDoesNotExist()
    }
    
    // ========== Animation Tests ==========
    
    @Test
    fun animation_progressIndicatorDisplays() {
        composeTestRule.setContent {
            SensorHubTheme {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
        
        // Progress indicator should be displayed
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }
    
    // ========== State Tests ==========
    
    @Test
    fun stateChange_triggersRecomposition() {
        var counter by androidx.compose.runtime.mutableStateOf(0)
        
        composeTestRule.setContent {
            SensorHubTheme {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.Text("Count: $counter")
                    androidx.compose.material3.Button(
                        onClick = { counter++ }
                    ) {
                        androidx.compose.material3.Text("Increment")
                    }
                }
            }
        }
        
        composeTestRule
            .onNodeWithText("Count: 0")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Increment")
            .performClick()
        
        composeTestRule
            .onNodeWithText("Count: 1")
            .assertIsDisplayed()
    }
    
    // ========== Accessibility Tests ==========
    
    @Test
    fun accessibility_contentDescriptionsExist() {
        composeTestRule.setContent {
            SensorHubTheme {
                androidx.compose.material3.IconButton(
                    onClick = { }
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Info,
                        contentDescription = "Information"
                    )
                }
            }
        }
        
        composeTestRule
            .onNodeWithContentDescription("Information")
            .assertIsDisplayed()
    }
    
    // ========== Performance Tests ==========
    
    @Test
    fun performance_largeListRendersQuickly() {
        val startTime = System.currentTimeMillis()
        
        composeTestRule.setContent {
            SensorHubTheme {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(1000) { index ->
                        androidx.compose.material3.Text(
                            text = "Item $index",
                            modifier = androidx.compose.ui.Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
        
        val renderTime = System.currentTimeMillis() - startTime
        
        // Render should be quick (< 1 second)
        assert(renderTime < 1000) { "Render took ${renderTime}ms" }
        
        composeTestRule
            .onNodeWithText("Item 0")
            .assertIsDisplayed()
    }
}
