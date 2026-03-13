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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kia.sensorhub.data.model.LightData
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.sensors.LightSensorManager
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
 * Light Sensor Screen - Complete Implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightSensorScreen(
    viewModel: LightSensorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Light Sensor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFC107),
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
                        imageVector = if (uiState.isAvailable)
                            Icons.Default.WbSunny
                        else
                            Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = if (uiState.isAvailable) "Sensor Available" else "Sensor Not Available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.isMonitoring) {
                            Text(
                                text = uiState.lightLevel,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Light Level Visualization
            Text(
                text = "Light Level",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            LightLevelIndicator(
                lux = uiState.currentData.illuminance,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            // Current Value
            SensorCard(
                label = "Illuminance",
                value = uiState.currentData.illuminance,
                unit = "lux",
                color = getLightColor(uiState.currentData.illuminance)
            )
            
            // Recommendations
            RecommendationCard(
                lux = uiState.currentData.illuminance
            )
            
            // Light History Chart
            if (uiState.history.isNotEmpty()) {
                Text(
                    text = "Light History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                LightHistoryChart(
                    history = uiState.history.takeLast(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
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
fun LightLevelIndicator(
    lux: Float,
    modifier: Modifier = Modifier
) {
    val animatedLux by animateFloatAsState(
        targetValue = lux,
        animationSpec = spring(),
        label = "lux"
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
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxLux = 10000f
                val normalizedLux = (animatedLux / maxLux).coerceIn(0f, 1f)
                
                // Draw gradient background
                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A), // Very Dark
                        Color(0xFF4A4A4A), // Dark
                        Color(0xFF808080), // Medium
                        Color(0xFFFFEB3B), // Bright
                        Color(0xFFFFFFFF)  // Very Bright
                    ),
                    startY = size.height,
                    endY = 0f
                )
                
                drawRect(
                    brush = gradient,
                    topLeft = Offset(0f, size.height * (1 - normalizedLux)),
                    size = androidx.compose.ui.geometry.Size(
                        size.width,
                        size.height * normalizedLux
                    )
                )
                
                // Draw level indicator line
                val indicatorY = size.height * (1 - normalizedLux)
                drawLine(
                    color = Color.Red,
                    start = Offset(0f, indicatorY),
                    end = Offset(size.width, indicatorY),
                    strokeWidth = 4f
                )
            }
            
            // Center value display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${animatedLux.roundToInt()}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "lux",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RecommendationCard(lux: Float) {
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
                    imageVector = getLightIcon(lux),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = getLightColor(lux)
                )
                Column {
                    Text(
                        text = "Environment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getLightDescription(lux),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Divider()
            
            // Reading suitability
            val isGoodForReading = lux in 300f..1000f
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isGoodForReading) Icons.Default.Check else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGoodForReading) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
                Text(
                    text = if (isGoodForReading) 
                        "Good lighting for reading" 
                    else 
                        "Not ideal for reading (300-1000 lux recommended)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Brightness recommendation
            val recommendedBrightness = getRecommendedBrightness(lux)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness6,
                    contentDescription = null
                )
                Text(
                    text = "Recommended screen brightness: ${(recommendedBrightness * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun LightHistoryChart(
    history: List<Float>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (history.isEmpty()) return@Canvas
            
            val stepX = size.width / (history.size - 1).coerceAtLeast(1)
            val minLux = history.minOrNull() ?: 0f
            val maxLux = history.maxOrNull() ?: 100f
            val range = (maxLux - minLux).coerceAtLeast(1f)
            
            val path = androidx.compose.ui.graphics.Path()
            
            history.forEachIndexed { index, lux ->
                val x = index * stepX
                val y = size.height - ((lux - minLux) / range * size.height)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            drawPath(
                path = path,
                color = Color(0xFFFFC107),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
        }
    }
}

// Helper functions
fun getLightColor(lux: Float): Color {
    return when {
        lux < 10 -> Color(0xFF1A1A1A) // Very Dark
        lux < 50 -> Color(0xFF4A4A4A) // Dark
        lux < 200 -> Color(0xFF808080) // Dim
        lux < 1000 -> Color(0xFFFFC107) // Normal
        else -> Color(0xFFFFEB3B) // Bright
    }
}

fun getLightIcon(lux: Float): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        lux < 50 -> Icons.Default.DarkMode
        lux < 1000 -> Icons.Default.WbTwilight
        else -> Icons.Default.WbSunny
    }
}

fun getLightDescription(lux: Float): String {
    return when {
        lux < 1 -> "Pitch Black"
        lux < 10 -> "Very Dark"
        lux < 50 -> "Dark"
        lux < 200 -> "Dim"
        lux < 400 -> "Normal Indoor"
        lux < 1000 -> "Bright Indoor"
        lux < 10000 -> "Overcast Daylight"
        lux < 25000 -> "Full Daylight"
        else -> "Direct Sunlight"
    }
}

fun getRecommendedBrightness(lux: Float): Float {
    return when {
        lux < 10 -> 0.1f
        lux < 50 -> 0.3f
        lux < 200 -> 0.5f
        lux < 1000 -> 0.7f
        lux < 10000 -> 0.9f
        else -> 1.0f
    }
}

// ViewModel
@HiltViewModel
class LightSensorViewModel @Inject constructor(
    private val lightSensorManager: LightSensorManager,
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LightSensorUiState())
    val uiState: StateFlow<LightSensorUiState> = _uiState.asStateFlow()
    
    private var monitoringJob: Job? = null
    private val luxHistory = mutableListOf<Float>()
    
    init {
        checkSensorAvailability()
    }
    
    private fun checkSensorAvailability() {
        _uiState.value = _uiState.value.copy(
            isAvailable = lightSensorManager.isAvailable(),
            sensorInfo = lightSensorManager.getSensorInfo()
        )
    }
    
    fun startMonitoring() {
        monitoringJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMonitoring = true)
            
            lightSensorManager.getLightFlow()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isMonitoring = false,
                        isAvailable = false
                    )
                }
                .collect { data ->
                    luxHistory.add(data.illuminance)
                    if (luxHistory.size > 100) {
                        luxHistory.removeAt(0)
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        currentData = data,
                        lightLevel = getLightDescription(data.illuminance),
                        history = luxHistory.toList()
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

data class LightSensorUiState(
    val isAvailable: Boolean = false,
    val isMonitoring: Boolean = false,
    val currentData: LightData = LightData(),
    val lightLevel: String = "Unknown",
    val history: List<Float> = emptyList(),
    val sensorInfo: SensorInfo? = null
)
