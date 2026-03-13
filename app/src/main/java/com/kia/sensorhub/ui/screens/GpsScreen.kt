package com.kia.sensorhub.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kia.sensorhub.data.model.GpsData
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.sensors.GpsManager
import com.kia.sensorhub.sensors.SensorInfo
import com.kia.sensorhub.ui.components.SensorCard
import com.kia.sensorhub.ui.components.SensorInfoDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * GPS/Location Screen - Complete Implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsScreen(
    viewModel: GpsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS / Location") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00BCD4),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Sensor Info",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        !uiState.hasPermission -> MaterialTheme.colorScheme.errorContainer
                        !uiState.isAvailable -> MaterialTheme.colorScheme.errorContainer
                        uiState.currentData.accuracy > 50 -> MaterialTheme.colorScheme.errorContainer // Fallback if warningContainer missing
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            !uiState.hasPermission -> Icons.Default.LocationOff
                            uiState.currentData.accuracy < 20 -> Icons.Default.GpsFixed
                            else -> Icons.Default.GpsNotFixed
                        },
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = when {
                                !uiState.hasPermission -> "Permission Required"
                                !uiState.isAvailable -> "GPS Not Available"
                                uiState.currentData.accuracy < 10 -> "Excellent Signal"
                                uiState.currentData.accuracy < 20 -> "Good Signal"
                                uiState.currentData.accuracy < 50 -> "Fair Signal"
                                else -> "Poor Signal"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.isMonitoring) {
                            Text(
                                text = "Accuracy: ${uiState.currentData.accuracy.roundToInt()}m",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Map Visualization
            if (uiState.isMonitoring) {
                Text(
                    text = "Location Map",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                LocationMapView(
                    currentLocation = uiState.currentData,
                    locationHistory = uiState.locationHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }
            
            // Coordinates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Latitude",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatCoordinate(uiState.currentData.latitude, true),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Longitude",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatCoordinate(uiState.currentData.longitude, false),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Additional Data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SensorCard(
                    label = "Altitude",
                    value = uiState.currentData.altitude.toFloat(),
                    unit = "m",
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
                
                SensorCard(
                    label = "Speed",
                    value = uiState.currentData.speed * 3.6f, // Convert m/s to km/h
                    unit = "km/h",
                    color = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SensorCard(
                    label = "Bearing",
                    value = uiState.currentData.bearing,
                    unit = "°",
                    color = Color(0xFF00BCD4),
                    modifier = Modifier.weight(1f)
                )
                
                SensorCard(
                    label = "Accuracy",
                    value = uiState.currentData.accuracy,
                    unit = "m",
                    color = getAccuracyColor(uiState.currentData.accuracy),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Compass
            Text(
                text = "Direction Compass",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            CompassView(
                bearing = uiState.currentData.bearing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            // Statistics
            if (uiState.locationHistory.size > 1) {
                LocationStatisticsCard(
                    totalDistance = uiState.totalDistance,
                    averageSpeed = uiState.averageSpeed,
                    maxSpeed = uiState.maxSpeed,
                    pointsRecorded = uiState.locationHistory.size
                )
            }
            
            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (uiState.isMonitoring) {
                            viewModel.stopMonitoring()
                        } else {
                            viewModel.startMonitoring()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isMonitoring)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    ),
                    enabled = uiState.isAvailable && uiState.hasPermission
                ) {
                    Icon(
                        imageVector = if (uiState.isMonitoring)
                            Icons.Default.Stop
                        else
                            Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (uiState.isMonitoring) "Stop" else "Start")
                }
                
                if (uiState.isMonitoring) {
                    FilledTonalButton(
                        onClick = { viewModel.saveLocation() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
            
            if (!uiState.hasPermission) {
                Button(
                    onClick = { viewModel.requestPermission() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Grant Location Permission")
                }
            }
        }
    }
    
    if (showInfoDialog && uiState.sensorInfo != null) {
        SensorInfoDialog(
            sensorInfo = uiState.sensorInfo!!,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@Composable
fun LocationMapView(
    currentLocation: GpsData,
    locationHistory: List<GpsData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (locationHistory.isEmpty()) return@Canvas
            
            // Calculate bounds
            val lats = locationHistory.map { it.latitude }
            val lons = locationHistory.map { it.longitude }
            val minLat = lats.minOrNull() ?: 0.0
            val maxLat = lats.maxOrNull() ?: 0.0
            val minLon = lons.minOrNull() ?: 0.0
            val maxLon = lons.maxOrNull() ?: 0.0
            
            val latRange = (maxLat - minLat).coerceAtLeast(0.001)
            val lonRange = (maxLon - minLon).coerceAtLeast(0.001)
            
            // Draw grid
            for (i in 0..4) {
                val y = size.height * i / 4
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                
                val x = size.width * i / 4
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }
            
            // Draw path
            if (locationHistory.size > 1) {
                val path = Path()
                locationHistory.forEachIndexed { index, location ->
                    val x = ((location.longitude - minLon) / lonRange * size.width).toFloat()
                    val y = size.height - ((location.latitude - minLat) / latRange * size.height).toFloat()
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                
                drawPath(
                    path = path,
                    color = Color(0xFF2196F3),
                    style = Stroke(width = 3f)
                )
            }
            
            // Draw current location
            val currentX = ((currentLocation.longitude - minLon) / lonRange * size.width).toFloat()
            val currentY = size.height - ((currentLocation.latitude - minLat) / latRange * size.height).toFloat()
            
            drawCircle(
                color = Color(0xFFFF5722),
                radius = 8f,
                center = Offset(currentX, currentY)
            )
            
            drawCircle(
                color = Color(0xFFFF5722).copy(alpha = 0.3f),
                radius = 16f,
                center = Offset(currentX, currentY)
            )
        }
    }
}

@Composable
fun CompassView(
    bearing: Float,
    modifier: Modifier = Modifier
) {
    val animatedBearing by animateFloatAsState(
        targetValue = bearing,
        label = "bearing"
    )
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = minOf(size.width, size.height) / 2
                
                // Draw compass circle
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2f)
                )
                
                // Draw cardinal directions
                val directions = listOf("N", "E", "S", "W")
                directions.forEachIndexed { index, direction ->
                    val angle = index * 90f
                    val angleRad = Math.toRadians(angle.toDouble())
                    val textRadius = radius * 0.85f
                    
                    val x = centerX + textRadius * kotlin.math.sin(angleRad).toFloat()
                    val y = centerY - textRadius * kotlin.math.cos(angleRad).toFloat()
                    
                    // Draw direction markers
                    drawCircle(
                        color = if (direction == "N") Color.Red else Color.Gray,
                        radius = 6f,
                        center = Offset(x, y)
                    )
                }
                
                // Draw bearing needle
                val bearingRad = Math.toRadians(animatedBearing.toDouble())
                val needleLength = radius * 0.7f
                
                val needleEndX = centerX + needleLength * kotlin.math.sin(bearingRad).toFloat()
                val needleEndY = centerY - needleLength * kotlin.math.cos(bearingRad).toFloat()
                
                drawLine(
                    color = Color(0xFF00BCD4),
                    start = Offset(centerX, centerY),
                    end = Offset(needleEndX, needleEndY),
                    strokeWidth = 4f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            
            // Center text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${animatedBearing.roundToInt()}°",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = getCardinalDirection(animatedBearing),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LocationStatisticsCard(
    totalDistance: Float,
    averageSpeed: Float,
    maxSpeed: Float,
    pointsRecorded: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Trip Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Distance", "${(totalDistance / 1000).roundToInt()} km")
                StatItem("Avg Speed", "${(averageSpeed * 3.6f).roundToInt()} km/h")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Max Speed", "${(maxSpeed * 3.6f).roundToInt()} km/h")
                StatItem("Points", "$pointsRecorded")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatCoordinate(coordinate: Double, isLatitude: Boolean): String {
    val abs = kotlin.math.abs(coordinate)
    val direction = if (isLatitude) {
        if (coordinate >= 0) "N" else "S"
    } else {
        if (coordinate >= 0) "E" else "W"
    }
    return String.format("%.6f° %s", abs, direction)
}

fun getCardinalDirection(bearing: Float): String {
    return when {
        bearing < 22.5 || bearing >= 337.5 -> "North"
        bearing < 67.5 -> "Northeast"
        bearing < 112.5 -> "East"
        bearing < 157.5 -> "Southeast"
        bearing < 202.5 -> "South"
        bearing < 247.5 -> "Southwest"
        bearing < 292.5 -> "West"
        else -> "Northwest"
    }
}

fun getAccuracyColor(accuracy: Float): Color {
    return when {
        accuracy < 10 -> Color(0xFF4CAF50) // Excellent - Green
        accuracy < 20 -> Color(0xFF8BC34A) // Good - Light Green
        accuracy < 50 -> Color(0xFFFFC107) // Fair - Yellow
        else -> Color(0xFFFF5722) // Poor - Red
    }
}

// ViewModel
@HiltViewModel
class GpsViewModel @Inject constructor(
    private val gpsManager: GpsManager,
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GpsUiState())
    val uiState: StateFlow<GpsUiState> = _uiState.asStateFlow()
    
    private var monitoringJob: Job? = null
    private val locations = mutableListOf<GpsData>()
    
    init {
        checkAvailability()
    }
    
    private fun checkAvailability() {
        _uiState.value = _uiState.value.copy(
            isAvailable = gpsManager.isAvailable(),
            hasPermission = gpsManager.hasPermission(),
            sensorInfo = gpsManager.getSensorInfo()
        )
    }
    
    fun startMonitoring() {
        monitoringJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMonitoring = true)
            
            gpsManager.getLocationFlow()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isMonitoring = false,
                        hasPermission = false
                    )
                }
                .collect { data ->
                    locations.add(data)
                    if (locations.size > 100) {
                        locations.removeAt(0)
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        currentData = data,
                        locationHistory = locations.toList(),
                        totalDistance = calculateTotalDistance(),
                        averageSpeed = calculateAverageSpeed(),
                        maxSpeed = calculateMaxSpeed()
                    )
                }
        }
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }
    
    fun saveLocation() {
        viewModelScope.launch {
            // TODO: Save to database
        }
    }
    
    fun requestPermission() {
        // TODO: Trigger permission request
        checkAvailability()
    }
    
    private fun calculateTotalDistance(): Float {
        if (locations.size < 2) return 0f
        
        var total = 0f
        for (i in 1 until locations.size) {
            total += gpsManager.calculateDistance(
                locations[i - 1].latitude,
                locations[i - 1].longitude,
                locations[i].latitude,
                locations[i].longitude
            )
        }
        return total
    }
    
    private fun calculateAverageSpeed(): Float {
        if (locations.isEmpty()) return 0f
        return locations.map { it.speed }.average().toFloat()
    }
    
    private fun calculateMaxSpeed(): Float {
        if (locations.isEmpty()) return 0f
        return locations.maxOf { it.speed }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}

data class GpsUiState(
    val isAvailable: Boolean = false,
    val hasPermission: Boolean = false,
    val isMonitoring: Boolean = false,
    val currentData: GpsData = GpsData(),
    val locationHistory: List<GpsData> = emptyList(),
    val totalDistance: Float = 0f,
    val averageSpeed: Float = 0f,
    val maxSpeed: Float = 0f,
    val sensorInfo: SensorInfo? = null
)
