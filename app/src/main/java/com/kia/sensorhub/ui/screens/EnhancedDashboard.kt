package com.kia.sensorhub.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kia.sensorhub.ui.animations.ParticleEffect
import com.kia.sensorhub.ui.animations.PulsatingGlow
import com.kia.sensorhub.ui.animations.ShimmerEffect
import com.kia.sensorhub.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Enhanced Dashboard with Real-time Insights
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("SensorHub Dashboard")
                        Text(
                            text = "Real-time Monitoring",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            // Status Summary Card
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    StatusSummaryCard(
                        activeSensors = uiState.activeSensors,
                        totalReadings = uiState.totalReadings,
                        isMonitoring = uiState.isMonitoring
                    )
                }
            }
            
            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                QuickActionsRow(onNavigate = onNavigate)
            }
            
            // Real-time Insights
            item {
                Text(
                    text = "Real-time Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Insights Cards
            items(uiState.insights) { insight ->
                InsightCard(
                    insight = insight,
                    onClick = { onNavigate(insight.targetScreen) }
                )
            }
            
            // Recent Activity
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(uiState.recentActivity) { activity ->
                RecentActivityCard(activity = activity)
            }
        }
    }
}

@Composable
fun StatusSummaryCard(
    activeSensors: Int,
    totalReadings: Int,
    isMonitoring: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box {
            // Animated background effect
            ParticleEffect(
                isActive = isMonitoring,
                particleCount = 30,
                modifier = Modifier
                    .fillMaxSize()
                    .height(180.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "System Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isMonitoring) "Active Monitoring" else "Ready",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isMonitoring) 
                                Color.Green 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    
                    if (isMonitoring) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PulsatingGlow(
                                isActive = true,
                                color = Color.Green,
                                modifier = Modifier.size(48.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Color.Green
                            )
                        }
                    }
                }
                
                Divider(modifier = Modifier.alpha(0.3f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        icon = Icons.Default.Sensors,
                        value = activeSensors.toString(),
                        label = "Active Sensors"
                    )
                    
                    StatItem(
                        icon = Icons.Default.DataUsage,
                        value = formatNumber(totalReadings),
                        label = "Total Readings"
                    )
                    
                    StatItem(
                        icon = Icons.Default.Speed,
                        value = "60Hz",
                        label = "Update Rate"
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun QuickActionsRow(onNavigate: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            QuickActionCard(
                icon = Icons.Default.Speed,
                label = "Accelerometer",
                color = SensorAccelerometer,
                onClick = { onNavigate("accelerometer") }
            )
        }
        item {
            QuickActionCard(
                icon = Icons.Default.Explore,
                label = "Compass",
                color = SensorMagnetometer,
                onClick = { onNavigate("magnetometer") }
            )
        }
        item {
            QuickActionCard(
                icon = Icons.Default.Psychology,
                label = "Affective",
                color = Color(0xFF9C27B0),
                onClick = { onNavigate("affective") }
            )
        }
        item {
            QuickActionCard(
                icon = Icons.Default.BarChart,
                label = "Statistics",
                color = Color(0xFFFF9800),
                onClick = { onNavigate("statistics") }
            )
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable { 
                isPressed = true
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = color
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun InsightCard(
    insight: DashboardInsight,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = insight.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = insight.color.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = insight.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp),
                            tint = insight.color
                        )
                    }
                    
                    Column {
                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = insight.value,
                            style = MaterialTheme.typography.bodyLarge,
                            color = insight.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) 
                            Icons.Default.KeyboardArrowUp 
                        else 
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }
            
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider()
                    Text(
                        text = insight.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (insight.recommendation.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFEB3B)
                            )
                            Text(
                                text = insight.recommendation,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                    
                    FilledTonalButton(
                        onClick = onClick,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("View Details")
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentActivityCard(activity: ActivityItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = activity.color.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = activity.color
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = activity.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (activity.badge.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = activity.color.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = activity.badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = activity.color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Data classes
data class DashboardInsight(
    val title: String,
    val value: String,
    val description: String,
    val recommendation: String = "",
    val icon: ImageVector,
    val color: Color,
    val targetScreen: String
)

data class ActivityItem(
    val title: String,
    val timestamp: String,
    val icon: ImageVector,
    val color: Color,
    val badge: String = ""
)

// ViewModel
@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun refreshData() {
        loadData()
    }
    
    private fun loadData() {
        _uiState.value = _uiState.value.copy(
            activeSensors = 7,
            totalReadings = 15234,
            isMonitoring = true,
            insights = listOf(
                DashboardInsight(
                    title = "Movement Detected",
                    value = "High Activity",
                    description = "Your device has been moving frequently. Activity level is above normal.",
                    recommendation = "Consider using the Affective module to analyze your stress levels.",
                    icon = Icons.Default.DirectionsRun,
                    color = Color(0xFF2196F3),
                    targetScreen = "accelerometer"
                ),
                DashboardInsight(
                    title = "Emotional State",
                    value = "Focused",
                    description = "Analysis shows high focus with low stress levels.",
                    icon = Icons.Default.Psychology,
                    color = Color(0xFF9C27B0),
                    targetScreen = "affective"
                ),
                DashboardInsight(
                    title = "Light Environment",
                    value = "Bright",
                    description = "Ambient light is at optimal levels for productivity.",
                    icon = Icons.Default.LightMode,
                    color = Color(0xFFFFEB3B),
                    targetScreen = "light"
                )
            ),
            recentActivity = listOf(
                ActivityItem(
                    title = "Accelerometer data recorded",
                    timestamp = "2 minutes ago",
                    icon = Icons.Default.Speed,
                    color = SensorAccelerometer,
                    badge = "NEW"
                ),
                ActivityItem(
                    title = "Emotion analysis completed",
                    timestamp = "15 minutes ago",
                    icon = Icons.Default.Psychology,
                    color = Color(0xFF9C27B0)
                ),
                ActivityItem(
                    title = "Data exported to CSV",
                    timestamp = "1 hour ago",
                    icon = Icons.Default.FileDownload,
                    color = Color(0xFF4CAF50)
                )
            )
        )
    }
}

data class DashboardUiState(
    val activeSensors: Int = 0,
    val totalReadings: Int = 0,
    val isMonitoring: Boolean = false,
    val insights: List<DashboardInsight> = emptyList(),
    val recentActivity: List<ActivityItem> = emptyList()
)

fun formatNumber(number: Int): String {
    return when {
        number >= 1000000 -> "${number / 1000000}M+"
        number >= 1000 -> "${number / 1000}K+"
        else -> number.toString()
    }
}
