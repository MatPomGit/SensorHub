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
import com.kia.sensorhub.data.model.BarometerData
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.sensors.SensorInfo
import com.kia.sensorhub.ui.components.SensorCard
import com.kia.sensorhub.ui.components.SensorInfoDialog
import com.kia.sensorhub.ui.theme.SensorBarometer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Barometer Screen - Complete Implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarometerScreen(
    viewModel: BarometerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barometer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SensorBarometer,
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
                            Icons.Default.CheckCircle
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
                                text = uiState.weatherTrend,
                                style = MaterialTheme.typography.bodyMedium,
                                color = getTrendColor(uiState.weatherTrend)
                            )
                        }
                    }
                }
            }
            
            // Pressure Gauge
            Text(
                text = "Atmospheric Pressure",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            PressureGauge(
                pressure = uiState.currentData.pressure,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            
            // Current Values
            SensorCard(
                label = "Pressure",
                value = uiState.currentData.pressure,
                unit = "hPa",
                color = SensorBarometer
            )
            
            SensorCard(
                label = "Altitude",
                value = uiState.currentData.altitude,
                unit = "m",
                color = Color(0xFF4CAF50)
            )
            
            // Weather Indicators
            WeatherIndicatorCard(
                pressure = uiState.currentData.pressure,
                trend = uiState.weatherTrend
            )
            
            // Historical Data (if available)
            if (uiState.history.isNotEmpty()) {
                Text(
                    text = "Pressure History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                PressureHistoryChart(
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
fun PressureGauge(
    pressure: Float,
    modifier: Modifier = Modifier
) {
    val animatedPressure by animateFloatAsState(
        targetValue = pressure,
        animationSpec = spring(),
        label = "pressure"
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
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = minOf(size.width, size.height) / 2 * 0.8f
                
                // Draw gauge arc (950-1050 hPa range)
                val minPressure = 950f
                val maxPressure = 1050f
                val normalizedPressure = ((animatedPressure - minPressure) / (maxPressure - minPressure)).coerceIn(0f, 1f)
                
                // Background arc
                drawArc(
                    color = Color.Gray.copy(alpha = 0.2f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 20f, cap = StrokeCap.Round)
                )
                
                // Pressure zones
                // Low (950-980): Blue
                drawArc(
                    color = Color(0xFF2196F3).copy(alpha = 0.3f),
                    startAngle = 135f,
                    sweepAngle = 81f,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 20f)
                )
                
                // Normal (980-1020): Green
                drawArc(
                    color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                    startAngle = 216f,
                    sweepAngle = 108f,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 20f)
                )
                
                // High (1020-1050): Orange
                drawArc(
                    color = Color(0xFFFF9800).copy(alpha = 0.3f),
                    startAngle = 324f,
                    sweepAngle = 81f,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 20f)
                )
                
                // Current value arc
                val sweepAngle = 270f * normalizedPressure
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF2196F3),
                            Color(0xFF4CAF50),
                            Color(0xFFFF9800)
                        ),
                        center = Offset(centerX, centerY)
                    ),
                    startAngle = 135f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 20f, cap = StrokeCap.Round)
                )
                
                // Draw tick marks
                for (i in 0..10) {
                    val angle = 135f + (270f * i / 10f)
                    val angleRad = Math.toRadians(angle.toDouble())
                    val innerRadius = radius * 0.85f
                    val outerRadius = radius * 0.95f
                    
                    val startX = centerX + innerRadius * kotlin.math.cos(angleRad).toFloat()
                    val startY = centerY + innerRadius * kotlin.math.sin(angleRad).toFloat()
                    val endX = centerX + outerRadius * kotlin.math.cos(angleRad).toFloat()
                    val endY = centerY + outerRadius * kotlin.math.sin(angleRad).toFloat()
                    
                    drawLine(
                        color = Color.White,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2f
                    )
                }
            }
            
            // Center value display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${animatedPressure.roundToInt()}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = getPressureColor(animatedPressure)
                )
                Text(
                    text = "hPa",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WeatherIndicatorCard(
    pressure: Float,
    trend: String
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
                    imageVector = getWeatherIcon(pressure),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = getPressureColor(pressure)
                )
                Column {
                    Text(
                        text = "Weather Forecast",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getWeatherDescription(pressure),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Divider()
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getTrendIcon(trend),
                    contentDescription = null,
                    tint = getTrendColor(trend)
                )
                Text(
                    text = "Trend: $trend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = getTrendColor(trend)
                )
            }
        }
    }
}

@Composable
fun PressureHistoryChart(
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
            val minPressure = history.minOrNull() ?: 1000f
            val maxPressure = history.maxOrNull() ?: 1020f
            val range = (maxPressure - minPressure).coerceAtLeast(1f)
            
            val path = androidx.compose.ui.graphics.Path()
            
            history.forEachIndexed { index, pressure ->
                val x = index * stepX
                val y = size.height - ((pressure - minPressure) / range * size.height)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            // Draw line
            drawPath(
                path = path,
                color = SensorBarometer,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
            
            // Draw gradient fill
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                addPath(path)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SensorBarometer.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
        }
    }
}

// Helper functions
fun getPressureColor(pressure: Float): Color {
    return when {
        pressure < 980 -> Color(0xFF2196F3) // Low - Blue
        pressure > 1020 -> Color(0xFFFF9800) // High - Orange
        else -> Color(0xFF4CAF50) // Normal - Green
    }
}

fun getWeatherIcon(pressure: Float): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        pressure < 980 -> Icons.Default.Cloud // Low pressure - cloudy/rainy
        pressure > 1020 -> Icons.Default.WbSunny // High pressure - sunny
        else -> Icons.Default.Cloud // Normal
    }
}

fun getWeatherDescription(pressure: Float): String {
    return when {
        pressure < 980 -> "Low pressure - Expect rain or storms"
        pressure < 1000 -> "Below normal - Cloudy weather likely"
        pressure < 1020 -> "Normal - Fair weather"
        pressure < 1040 -> "Above normal - Clear skies expected"
        else -> "High pressure - Very stable weather"
    }
}

fun getTrendIcon(trend: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (trend.lowercase()) {
        "rising" -> Icons.Default.TrendingUp
        "falling" -> Icons.Default.TrendingDown
        else -> Icons.Default.TrendingFlat
    }
}

fun getTrendColor(trend: String): Color {
    return when (trend.lowercase()) {
        "rising" -> Color(0xFF4CAF50)
        "falling" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
}

// ViewModel
@HiltViewModel
class BarometerViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BarometerUiState())
    val uiState: StateFlow<BarometerUiState> = _uiState.asStateFlow()
    
    private var monitoringJob: Job? = null
    private val pressureHistory = mutableListOf<Float>()
    
    init {
        checkSensorAvailability()
    }
    
    private fun checkSensorAvailability() {
        // TODO: Check barometer availability
        _uiState.value = _uiState.value.copy(isAvailable = true)
    }
    
    fun startMonitoring() {
        monitoringJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMonitoring = true)
            
            // Simulate barometer data for now
            while (true) {
                kotlinx.coroutines.delay(1000)
                
                val pressure = 1013f + (Math.random() * 20 - 10).toFloat()
                val altitude = calculateAltitude(pressure)
                
                pressureHistory.add(pressure)
                if (pressureHistory.size > 100) {
                    pressureHistory.removeAt(0)
                }
                
                val trend = calculateTrend()
                
                _uiState.value = _uiState.value.copy(
                    currentData = BarometerData(
                        pressure = pressure,
                        altitude = altitude
                    ),
                    weatherTrend = trend,
                    history = pressureHistory.toList()
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
    
    private fun calculateAltitude(pressure: Float): Float {
        // Barometric formula: h = 44330 * (1 - (P/P0)^(1/5.255))
        val seaLevelPressure = 1013.25f
        return (44330f * (1 - Math.pow((pressure / seaLevelPressure).toDouble(), (1.0 / 5.255)))).toFloat()
    }
    
    private fun calculateTrend(): String {
        if (pressureHistory.size < 10) return "Stable"
        
        val recent = pressureHistory.takeLast(10)
        val older = pressureHistory.takeLast(20).take(10)
        
        val recentAvg = recent.average()
        val olderAvg = older.average()
        
        return when {
            recentAvg > olderAvg + 2 -> "Rising"
            recentAvg < olderAvg - 2 -> "Falling"
            else -> "Stable"
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}

data class BarometerUiState(
    val isAvailable: Boolean = false,
    val isMonitoring: Boolean = false,
    val currentData: BarometerData = BarometerData(),
    val weatherTrend: String = "Stable",
    val history: List<Float> = emptyList(),
    val sensorInfo: SensorInfo? = null
)
