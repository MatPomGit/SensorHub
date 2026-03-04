package com.kia.sensorhub.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kia.sensorhub.ui.components.MultiSeriesLineChart
import com.kia.sensorhub.ui.components.RadarChart
import com.kia.sensorhub.ui.components.WaveformVisualization
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.random.Random

/**
 * Sensor Comparison Screen
 * Compare multiple sensors side by side with advanced analytics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorComparisonScreen(
    viewModel: ComparisonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Comparison") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sensor Selection
            item {
                SensorSelectionCard(
                    selectedSensors = uiState.selectedSensors,
                    onSensorToggle = { sensor -> viewModel.toggleSensor(sensor) }
                )
            }
            
            // Comparison Chart
            if (uiState.selectedSensors.size >= 2) {
                item {
                    Text(
                        text = "Real-time Comparison",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    MultiSeriesLineChart(
                        series = uiState.comparisonData,
                        colors = uiState.selectedSensors.map { it.color },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }
            }
            
            // Performance Radar
            if (uiState.selectedSensors.isNotEmpty()) {
                item {
                    Text(
                        text = "Performance Analysis",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    RadarChart(
                        values = uiState.performanceMetrics,
                        labels = listOf("Accuracy", "Speed", "Stability", "Range", "Power"),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }
            
            // Detailed Comparison Cards
            items(uiState.selectedSensors) { sensor ->
                DetailedSensorCard(
                    sensor = sensor,
                    stats = uiState.sensorStats[sensor.id] ?: SensorStats()
                )
            }
        }
    }
}

@Composable
fun SensorSelectionCard(
    selectedSensors: List<ComparisonSensorInfo>,
    onSensorToggle: (ComparisonSensorInfo) -> Unit
) {
    val availableSensors = remember {
        listOf(
            ComparisonSensorInfo("accelerometer", "Accelerometer", Icons.Default.Speed, Color(0xFF2196F3)),
            ComparisonSensorInfo("gyroscope", "Gyroscope", Icons.Default.ScreenRotation, Color(0xFF4CAF50)),
            ComparisonSensorInfo("magnetometer", "Magnetometer", Icons.Default.Explore, Color(0xFF9C27B0)),
            ComparisonSensorInfo("light", "Light Sensor", Icons.Default.LightMode, Color(0xFFFFEB3B)),
            ComparisonSensorInfo("proximity", "Proximity", Icons.Default.SensorsOff, Color(0xFFFF5722))
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Sensors to Compare",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            availableSensors.forEach { sensor ->
                ComparisonSensorChip(
                    sensor = sensor,
                    isSelected = selectedSensors.contains(sensor),
                    onClick = { onSensorToggle(sensor) }
                )
            }
        }
    }
}

@Composable
fun ComparisonSensorChip(
    sensor: ComparisonSensorInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) sensor.color.copy(alpha = 0.3f) else Color.Transparent,
        label = "bg"
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, sensor.color)
        else 
            androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = sensor.icon,
                contentDescription = null,
                tint = sensor.color
            )
            Text(
                text = sensor.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = sensor.color
                )
            }
        }
    }
}

@Composable
fun DetailedSensorCard(
    sensor: ComparisonSensorInfo,
    stats: SensorStats
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = sensor.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = sensor.icon,
                        contentDescription = null,
                        tint = sensor.color,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = sensor.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${stats.readingsCount} readings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) 
                            Icons.Default.ExpandLess 
                        else 
                            Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }
            
            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickStat("Avg", String.format("%.2f", stats.average))
                QuickStat("Min", String.format("%.2f", stats.min))
                QuickStat("Max", String.format("%.2f", stats.max))
                QuickStat("σ", String.format("%.2f", stats.stdDev))
            }
            
            // Expanded Content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider()
                    
                    // Mini waveform
                    WaveformVisualization(
                        dataPoints = stats.recentData,
                        color = sensor.color,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                    
                    // Additional stats
                    StatsGrid(
                        stats = mapOf(
                            "Frequency" to "${stats.frequency} Hz",
                            "Accuracy" to "${stats.accuracy}%",
                            "Uptime" to stats.uptime,
                            "Last Update" to stats.lastUpdate
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStat(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatsGrid(stats: Map<String, String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stats.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (key, value) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                // Fill remaining space if odd number
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Trends Analysis Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsAnalysisScreen(
    viewModel: TrendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trends & Insights") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time Period Selector
            item {
                TimePeriodSelector(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = { viewModel.selectPeriod(it) }
                )
            }
            
            // Trend Cards
            items(uiState.trends) { trend ->
                TrendCard(trend = trend)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    val periods = TimePeriod.values()
    
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        periods.forEachIndexed { index, period ->
            SegmentedButton(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                shape = SegmentedButtonDefaults.itemShape(index, periods.size)
            ) {
                Text(period.label)
            }
        }
    }
}

@Composable
fun TrendCard(trend: Trend) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = trend.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = trend.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                TrendIndicator(
                    change = trend.changePercent,
                    isPositive = trend.isPositiveTrend
                )
            }
            
            // Mini chart
            WaveformVisualization(
                dataPoints = trend.data,
                color = if (trend.isPositiveTrend) Color.Green else Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        }
    }
}

@Composable
fun TrendIndicator(
    change: Float,
    isPositive: Boolean
) {
    val color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
    val icon = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${if (change > 0) "+" else ""}${String.format("%.1f", change)}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// Data classes
data class ComparisonSensorInfo(
    val id: String,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

data class SensorStats(
    val readingsCount: Int = 0,
    val average: Float = 0f,
    val min: Float = 0f,
    val max: Float = 0f,
    val stdDev: Float = 0f,
    val frequency: Int = 60,
    val accuracy: Int = 95,
    val uptime: String = "0h 0m",
    val lastUpdate: String = "Never",
    val recentData: List<Float> = emptyList()
)

data class Trend(
    val title: String,
    val description: String,
    val changePercent: Float,
    val isPositiveTrend: Boolean,
    val data: List<Float>
)

enum class TimePeriod(val label: String) {
    TODAY("Today"),
    WEEK("Week"),
    MONTH("Month"),
    ALL("All Time")
}

// ViewModels
@HiltViewModel
class ComparisonViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ComparisonUiState())
    val uiState: StateFlow<ComparisonUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun toggleSensor(sensor: ComparisonSensorInfo) {
        val currentSelected = _uiState.value.selectedSensors.toMutableList()
        if (currentSelected.contains(sensor)) {
            currentSelected.remove(sensor)
        } else {
            currentSelected.add(sensor)
        }
        _uiState.value = _uiState.value.copy(selectedSensors = currentSelected)
        loadComparisonData()
    }
    
    fun refreshData() {
        loadData()
    }
    
    private fun loadData() {
        // Mock data
        _uiState.value = _uiState.value.copy(
            performanceMetrics = listOf(0.9f, 0.8f, 0.85f, 0.75f, 0.7f)
        )
    }
    
    private fun loadComparisonData() {
        val data = _uiState.value.selectedSensors.map { sensor ->
            sensor.name to List(50) { Random.nextFloat() * 2 - 1 }
        }
        _uiState.value = _uiState.value.copy(comparisonData = data)
    }
}

data class ComparisonUiState(
    val selectedSensors: List<ComparisonSensorInfo> = emptyList(),
    val comparisonData: List<Pair<String, List<Float>>> = emptyList(),
    val performanceMetrics: List<Float> = emptyList(),
    val sensorStats: Map<String, SensorStats> = emptyMap()
)

@HiltViewModel
class TrendsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(TrendsUiState())
    val uiState: StateFlow<TrendsUiState> = _uiState.asStateFlow()
    
    init {
        loadTrends()
    }
    
    fun selectPeriod(period: TimePeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadTrends()
    }
    
    private fun loadTrends() {
        val trends = listOf(
            Trend(
                title = "Activity Level",
                description = "Your movement has increased by 25% this week",
                changePercent = 25f,
                isPositiveTrend = true,
                data = List(20) { Random.nextFloat() * 0.5f + 0.5f }
            ),
            Trend(
                title = "Stress Levels",
                description = "Stress indicators are decreasing",
                changePercent = -15f,
                isPositiveTrend = true,
                data = List(20) { Random.nextFloat() * 0.5f }
            ),
            Trend(
                title = "Focus Time",
                description = "Focus duration has improved",
                changePercent = 12f,
                isPositiveTrend = true,
                data = List(20) { Random.nextFloat() * 0.8f + 0.2f }
            )
        )
        
        _uiState.value = _uiState.value.copy(trends = trends)
    }
}

data class TrendsUiState(
    val selectedPeriod: TimePeriod = TimePeriod.WEEK,
    val trends: List<Trend> = emptyList()
)
