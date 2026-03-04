package com.kia.sensorhub.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for the app
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Sensors : Screen("sensors", "Sensors", Icons.Default.Sensors)
    object Accelerometer : Screen("accelerometer", "Accelerometer", Icons.Default.Speed)
    object Gyroscope : Screen("gyroscope", "Gyroscope", Icons.Default.ScreenRotation) // Changed from ThreeDRotation
    object Magnetometer : Screen("magnetometer", "Magnetometer", Icons.Default.Explore)
    object Light : Screen("light", "Light", Icons.Default.LightMode)
    object GPS : Screen("gps", "GPS", Icons.Default.MyLocation)
    object Proximity : Screen("proximity", "Proximity", Icons.Default.SensorsOff)
    object Barometer : Screen("barometer", "Barometer", Icons.Default.Compress)
    object Interactions : Screen("interactions", "Interactions", Icons.Default.TouchApp)
    object Gestures : Screen("gestures", "Gestures", Icons.Default.Gesture)
    object Voice : Screen("voice", "Voice", Icons.Default.Mic)
    object Haptics : Screen("haptics", "Haptics", Icons.Default.Vibration)
    object Affective : Screen("affective", "Affective", Icons.Default.Psychology)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object About : Screen("about", "About", Icons.Default.Info)
}

/**
 * Bottom navigation items
 */
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Sensors,
    Screen.Interactions,
    Screen.Affective,
    Screen.Settings
)

/**
 * Sensor submenu items
 */
val sensorItems = listOf(
    Screen.Accelerometer,
    Screen.Gyroscope,
    Screen.Magnetometer,
    Screen.Light,
    Screen.GPS,
    Screen.Proximity,
    Screen.Barometer
)

/**
 * Interaction submenu items
 */
val interactionItems = listOf(
    Screen.Gestures,
    Screen.Voice,
    Screen.Haptics
)
