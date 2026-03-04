package com.kia.sensorhub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.ui.navigation.Screen
import com.kia.sensorhub.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for Sensors List Screen
 */
@HiltViewModel
class SensorsListViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SensorsListUiState())
    val uiState: StateFlow<SensorsListUiState> = _uiState.asStateFlow()
    
    init {
        checkAvailableSensors()
    }
    
    private fun checkAvailableSensors() {
        val sensors = listOf(
            SensorItem(
                name = "Accelerometer",
                description = "Measures device acceleration",
                icon = Icons.Default.Speed,
                color = SensorAccelerometer,
                isAvailable = repository.isAccelerometerAvailable(),
                route = Screen.Accelerometer.route
            ),
            SensorItem(
                name = "Gyroscope",
                description = "Measures device rotation",
                icon = Icons.Default.ScreenRotation,
                color = SensorGyroscope,
                isAvailable = repository.isGyroscopeAvailable(),
                route = Screen.Gyroscope.route
            ),
            SensorItem(
                name = "Magnetometer",
                description = "Measures magnetic field & compass",
                icon = Icons.Default.Explore,
                color = SensorMagnetometer,
                isAvailable = repository.isMagnetometerAvailable(),
                route = Screen.Magnetometer.route
            ),
            SensorItem(
                name = "Light Sensor",
                description = "Measures ambient light level",
                icon = Icons.Default.LightMode,
                color = SensorLight,
                isAvailable = false, // Will be implemented
                route = Screen.Light.route
            ),
            SensorItem(
                name = "GPS / Location",
                description = "Provides location data",
                icon = Icons.Default.MyLocation,
                color = SensorGPS,
                isAvailable = false, // Will be implemented
                route = Screen.GPS.route
            ),
            SensorItem(
                name = "Proximity",
                description = "Detects nearby objects",
                icon = Icons.Default.SensorsOff,
                color = SensorProximity,
                isAvailable = false, // Will be implemented
                route = Screen.Proximity.route
            ),
            SensorItem(
                name = "Barometer",
                description = "Measures atmospheric pressure",
                icon = Icons.Default.Compress,
                color = SensorBarometer,
                isAvailable = false, // Will be implemented
                route = Screen.Barometer.route
            )
        )
        
        _uiState.value = _uiState.value.copy(
            sensors = sensors,
            availableCount = sensors.count { it.isAvailable }
        )
    }
}

data class SensorsListUiState(
    val sensors: List<SensorItem> = emptyList(),
    val availableCount: Int = 0
)

data class SensorItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val isAvailable: Boolean,
    val route: String
)

/**
 * Sensors List Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsListScreen(
    onNavigate: (String) -> Unit,
    viewModel: SensorsListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Sensors") },
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
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sensors Available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.availableCount} of ${uiState.sensors.size} sensors detected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Sensors List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.sensors) { sensor ->
                    SensorListItem(
                        sensor = sensor,
                        onClick = { onNavigate(sensor.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun SensorListItem(
    sensor: SensorItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = sensor.isAvailable,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (sensor.isAvailable)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (sensor.isAvailable) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = sensor.color.copy(alpha = if (sensor.isAvailable) 0.2f else 0.1f)
            ) {
                Icon(
                    imageVector = sensor.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(32.dp),
                    tint = if (sensor.isAvailable) 
                        sensor.color 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            // Text
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = sensor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (sensor.isAvailable)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = sensor.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (sensor.isAvailable)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            // Status Badge
            if (sensor.isAvailable) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Available",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Not Available",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Arrow indicator for available sensors
            if (sensor.isAvailable) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
