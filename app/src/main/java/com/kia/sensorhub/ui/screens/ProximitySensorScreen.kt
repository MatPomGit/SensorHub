package com.kia.sensorhub.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kia.sensorhub.data.model.ProximityData
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.sensors.ProximitySensorManager
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
 * Proximity Sensor Screen - Complete Implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProximitySensorScreen(
    viewModel: ProximitySensorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proximity Sensor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9C27B0),
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
                    containerColor = if (uiState.isAvailable)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.currentData.isNear)
                            Icons.Default.Warning
                        else
                            Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (uiState.currentData.isNear) 
                            Color(0xFFFF9800) 
                        else 
                            Color(0xFF4CAF50)
                    )
                    Column {
                        Text(
                            text = if (uiState.isAvailable) "Sensor Available" else "Sensor Not Available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.isMonitoring) {
                            Text(
                                text = if (uiState.currentData.isNear) "Object Nearby" else "Clear",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.currentData.isNear) 
                                    Color(0xFFFF9800) 
                                else 
                                    Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
            
            // Distance Visualization
            Text(
                text = "Distance Detection",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            ProximityVisualization(
                distance = uiState.currentData.distance,
                maxRange = uiState.currentData.maxRange,
                isNear = uiState.currentData.isNear,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            
            // Current Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SensorCard(
                    label = "Distance",
                    value = uiState.currentData.distance,
                    unit = "cm",
                    color = if (uiState.currentData.isNear) 
                        Color(0xFFFF9800) 
                    else 
                        Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
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
                            text = "Max Range",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${uiState.currentData.maxRange.roundToInt()}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "cm",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            
            // Detection Info
            DetectionInfoCard(
                distance = uiState.currentData.distance,
                maxRange = uiState.currentData.maxRange,
                isNear = uiState.currentData.isNear
            )
            
            // Detection History
            if (uiState.detectionHistory.isNotEmpty()) {
                Text(
                    text = "Detection Events",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.detectionHistory.takeLast(5).forEach { event ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (event.isNear) 
                                            Icons.Default.Circle 
                                        else 
                                            Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (event.isNear) 
                                            Color(0xFFFF9800) 
                                        else 
                                            Color(0xFF4CAF50)
                                    )
                                    Text(
                                        text = if (event.isNear) "Near" else "Far",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = "${event.distance.roundToInt()} cm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (event != uiState.detectionHistory.last()) {
                                Divider()
                            }
                        }
                    }
                }
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
                    enabled = uiState.isAvailable
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
                        onClick = { viewModel.saveReading() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
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
fun ProximityVisualization(
    distance: Float,
    maxRange: Float,
    isNear: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedDistance by animateFloatAsState(
        targetValue = distance,
        animationSpec = spring(),
        label = "distance"
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
                val maxRadius = minOf(size.width, size.height) / 2
                
                // Draw detection zones (3 circles)
                for (i in 3 downTo 1) {
                    val radius = maxRadius * i / 3
                    val alpha = if (animatedDistance < maxRange * i / 3) 0.6f else 0.2f
                    
                    drawCircle(
                        color = when (i) {
                            1 -> Color(0xFFFF5722).copy(alpha = alpha) // Very close - Red
                            2 -> Color(0xFFFF9800).copy(alpha = alpha) // Close - Orange
                            else -> Color(0xFFFFC107).copy(alpha = alpha) // Medium - Yellow
                        },
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2f)
                    )
                }
                
                // Draw phone/sensor (center circle)
                drawCircle(
                    color = Color(0xFF2196F3),
                    radius = 20f,
                    center = Offset(centerX, centerY)
                )
                
                // Draw object indicator
                val normalizedDistance = (animatedDistance / maxRange).coerceIn(0f, 1f)
                val objectRadius = maxRadius * normalizedDistance
                
                if (objectRadius > 25f) {
                    // Draw line to object
                    drawLine(
                        color = if (isNear) Color(0xFFFF9800) else Color(0xFF4CAF50),
                        start = Offset(centerX, centerY),
                        end = Offset(centerX, centerY - objectRadius),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    
                    // Draw object
                    drawCircle(
                        color = if (isNear) Color(0xFFFF9800) else Color(0xFF4CAF50),
                        radius = 15f,
                        center = Offset(centerX, centerY - objectRadius)
                    )
                }
                
                // Draw distance arc
                val sweepAngle = 60f
                drawArc(
                    color = Color.White.copy(alpha = 0.5f),
                    startAngle = -120f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - objectRadius, centerY - objectRadius),
                    size = androidx.compose.ui.geometry.Size(objectRadius * 2, objectRadius * 2),
                    style = Stroke(width = 2f)
                )
            }
            
            // Center text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 80.dp)
            ) {
                Text(
                    text = "${animatedDistance.roundToInt()}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "cm",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun DetectionInfoCard(
    distance: Float,
    maxRange: Float,
    isNear: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getProximityIcon(distance, maxRange),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = getProximityColor(distance, maxRange)
                )
                Column {
                    Text(
                        text = "Detection Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getProximityDescription(distance, maxRange),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Divider()
            
            // Distance percentage
            val percentage = ((maxRange - distance) / maxRange * 100).coerceIn(0f, 100f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Proximity Level",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${percentage.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = getProximityColor(distance, maxRange)
            )
            
            // Use cases
            Text(
                text = "💡 Common Uses:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "• Screen off during calls\n• Auto-brightness adjustment\n• Gesture control\n• Object detection",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

fun getProximityIcon(distance: Float, maxRange: Float): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        distance < 1 -> Icons.Default.Warning
        distance < maxRange / 2 -> Icons.Default.Circle
        else -> Icons.Default.RadioButtonUnchecked
    }
}

fun getProximityColor(distance: Float, maxRange: Float): Color {
    return when {
        distance < 1 -> Color(0xFFFF5722) // Very close - Red
        distance < maxRange / 3 -> Color(0xFFFF9800) // Close - Orange
        distance < maxRange * 2 / 3 -> Color(0xFFFFC107) // Medium - Yellow
        else -> Color(0xFF4CAF50) // Far - Green
    }
}

fun getProximityDescription(distance: Float, maxRange: Float): String {
    return when {
        distance < 1 -> "Very Close (<1cm)"
        distance < 3 -> "Close (${distance.roundToInt()}cm)"
        distance < maxRange -> "Near (${distance.roundToInt()}cm)"
        else -> "Far (>${maxRange.roundToInt()}cm)"
    }
}

// ViewModel
@HiltViewModel
class ProximitySensorViewModel @Inject constructor(
    private val proximitySensorManager: ProximitySensorManager,
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProximitySensorUiState())
    val uiState: StateFlow<ProximitySensorUiState> = _uiState.asStateFlow()
    
    private var monitoringJob: Job? = null
    private val detectionEvents = mutableListOf<ProximityData>()
    
    init {
        checkSensorAvailability()
    }
    
    private fun checkSensorAvailability() {
        _uiState.value = _uiState.value.copy(
            isAvailable = proximitySensorManager.isAvailable(),
            sensorInfo = proximitySensorManager.getSensorInfo(),
            currentData = ProximityData(maxRange = proximitySensorManager.getMaxRange())
        )
    }
    
    fun startMonitoring() {
        monitoringJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMonitoring = true)
            
            proximitySensorManager.getProximityFlow()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isMonitoring = false,
                        isAvailable = false
                    )
                }
                .collect { data ->
                    detectionEvents.add(data)
                    if (detectionEvents.size > 20) {
                        detectionEvents.removeAt(0)
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        currentData = data,
                        detectionHistory = detectionEvents.toList()
                    )
                }
        }
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }
    
    fun saveReading() {
        viewModelScope.launch {
            // TODO: Save to database
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}

data class ProximitySensorUiState(
    val isAvailable: Boolean = false,
    val isMonitoring: Boolean = false,
    val currentData: ProximityData = ProximityData(),
    val detectionHistory: List<ProximityData> = emptyList(),
    val sensorInfo: SensorInfo? = null
)
