package com.kia.sensorhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kia.sensorhub.ui.navigation.Screen
import com.kia.sensorhub.ui.navigation.bottomNavItems
import com.kia.sensorhub.ui.screens.*
import com.kia.sensorhub.ui.theme.SensorHubTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SensorHubTheme {
                SensorHubApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorHubApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.padding(vertical = 16.dp))
                Text(
                    "SensorHub Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    icon = { Icon(Screen.Home.icon, contentDescription = null) },
                    label = { Text(Screen.Home.title) },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Home.route)
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Screen.About.icon, contentDescription = null) },
                    label = { Text(Screen.About.title) },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.About.route)
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                // Home
                composable(Screen.Home.route) {
                    HomeScreen(onNavigate = { route -> navController.navigate(route) })
                }
                
                // Sensors
                composable(Screen.Sensors.route) {
                    SensorsListScreen(onNavigate = { route -> navController.navigate(route) })
                }
                
                composable(Screen.Accelerometer.route) {
                    AccelerometerScreen()
                }
                
                composable(Screen.Gyroscope.route) {
                    GyroscopeScreen()
                }
                
                composable(Screen.Magnetometer.route) {
                    MagnetometerScreen()
                }
                
                composable(Screen.Light.route) {
                    LightSensorScreen()
                }
                
                composable(Screen.GPS.route) {
                    GpsScreen()
                }
                
                composable(Screen.Proximity.route) {
                    ProximityScreen()
                }
                
                // Interactions
                composable(Screen.Interactions.route) {
                    InteractionsMenuScreen(onNavigate = { route -> navController.navigate(route) })
                }
                
                composable(Screen.Gestures.route) {
                    GesturesScreen()
                }
                
                composable(Screen.Voice.route) {
                    VoiceRecognitionScreen()
                }
                
                composable(Screen.Haptics.route) {
                    HapticFeedbackScreen()
                }
                
                // Affective
                composable(Screen.Affective.route) {
                    AffectiveComputingScreen()
                }
                
                // Barometer
                composable(Screen.Barometer.route) {
                    BarometerScreen()
                }
                
                // Statistics & Export
                composable("statistics") {
                    StatisticsDashboardScreen()
                }
                
                // Enhanced Dashboard
                composable("dashboard") {
                    EnhancedDashboardScreen(
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                
                // Settings
                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
                
                // About
                composable(Screen.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(title: String, message: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InteractionsMenuScreen(onNavigate: (String) -> Unit) {
    HomeScreen(onNavigate = onNavigate)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About SensorHub") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SensorHub",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Educational Mobile Sensor Platform",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Version 1.0.0-alpha",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SensorHub is a comprehensive educational platform designed to demonstrate mobile sensor integration, user interactions, and affective computing concepts.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Features",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("• 7 Different Sensors", style = MaterialTheme.typography.bodyMedium)
                    Text("• Real-time Data Visualization", style = MaterialTheme.typography.bodyMedium)
                    Text("• Gesture Recognition", style = MaterialTheme.typography.bodyMedium)
                    Text("• Material Design 3", style = MaterialTheme.typography.bodyMedium)
                    Text("• MVVM Architecture", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
