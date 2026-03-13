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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kia.sensorhub.ui.components.SensorCard
import com.kia.sensorhub.ui.components.SensorInfoDialog
import com.kia.sensorhub.ui.theme.SensorGyroscope
import com.kia.sensorhub.ui.theme.SensorMagnetometer
import com.kia.sensorhub.viewmodel.GyroscopeViewModel
import com.kia.sensorhub.viewmodel.MagnetometerViewModel
import kotlin.math.cos
import kotlin.math.sin

/**
 * Gyroscope Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GyroscopeScreen(
    viewModel: GyroscopeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gyroscope") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SensorGyroscope,
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isAvailable) 
                                Icons.Default.CheckCircle 
                            else 
                                Icons.Default.Error,
                            contentDescription = null
                        )
                        Text(
                            text = if (uiState.isAvailable) "Sensor Available" else "Sensor Not Available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (uiState.isMonitoring) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Monitoring Active", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            
            // Error message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, "Dismiss")
                        }
                    }
                }
            }
            
            // Current Values
            Text(
                text = "Rotation Rate",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            SensorCard(
                label = "X-Axis (Roll)",
                value = uiState.currentData.x,
                unit = "rad/s",
                color = Color.Red
            )
            
            SensorCard(
                label = "Y-Axis (Pitch)",
                value = uiState.currentData.y,
                unit = "rad/s",
                color = Color.Green
            )
            
            SensorCard(
                label = "Z-Axis (Yaw)",
                value = uiState.currentData.z,
                unit = "rad/s",
                color = Color.Blue
            )
            
            SensorCard(
                label = "Total Rotation Rate",
                value = uiState.currentData.rotationRate,
                unit = "rad/s",
                color = SensorGyroscope
            )
            
            // Visualization
            Text(
                text = "3D Rotation Visualization",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            GyroscopeVisualization(
                x = uiState.currentData.x,
                y = uiState.currentData.y,
                z = uiState.currentData.z,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
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
                
                FilledTonalButton(
                    onClick = { viewModel.toggleSaving() },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.isMonitoring
                ) {
                    Icon(
                        imageVector = if (uiState.isSavingEnabled) 
                            Icons.Default.Save 
                        else 
                            Icons.Default.SaveAs,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (uiState.isSavingEnabled) "Saving" else "Save")
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
fun GyroscopeVisualization(
    x: Float,
    y: Float,
    z: Float,
    modifier: Modifier = Modifier
) {
    val animatedZ by animateFloatAsState(targetValue = z, label = "z")
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = minOf(size.width, size.height) / 3
            
            // Draw outer circle
            drawCircle(
                color = Color.Gray.copy(alpha = 0.3f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )
            
            // Draw rotation vectors
            rotate(animatedZ * 57.3f, Offset(centerX, centerY)) {
                // X rotation (red)
                drawLine(
                    color = Color.Red,
                    start = Offset(centerX, centerY),
                    end = Offset(centerX + radius * 0.8f, centerY),
                    strokeWidth = 4f
                )
                
                // Y rotation (green)
                drawLine(
                    color = Color.Green,
                    start = Offset(centerX, centerY),
                    end = Offset(centerX, centerY - radius * 0.8f),
                    strokeWidth = 4f
                )
            }
            
            // Center dot
            drawCircle(
                color = Color.Black,
                radius = 8f,
                center = Offset(centerX, centerY)
            )
        }
    }
}

/**
 * Magnetometer Screen with Compass
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagnetometerScreen(
    viewModel: MagnetometerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Magnetometer & Compass") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SensorMagnetometer,
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isAvailable) 
                                Icons.Default.CheckCircle 
                            else 
                                Icons.Default.Error,
                            contentDescription = null
                        )
                        Text(
                            text = if (uiState.isAvailable) "Sensor Available" else "Sensor Not Available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (uiState.isMonitoring) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Monitoring Active")
                        }
                    }
                }
            }
            
            // Compass
            Text(
                text = "Digital Compass",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            CompassVisualization(
                azimuth = uiState.currentData.azimuth,
                direction = viewModel.getCompassDirection(uiState.currentData.azimuth),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            
            // Magnetic Field Values
            Text(
                text = "Magnetic Field Strength",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            SensorCard(
                label = "X-Axis",
                value = uiState.currentData.x,
                unit = "µT",
                color = Color.Red
            )
            
            SensorCard(
                label = "Y-Axis",
                value = uiState.currentData.y,
                unit = "µT",
                color = Color.Green
            )
            
            SensorCard(
                label = "Z-Axis",
                value = uiState.currentData.z,
                unit = "µT",
                color = Color.Blue
            )
            
            SensorCard(
                label = "Total Magnitude",
                value = uiState.currentData.magnitude,
                unit = "µT",
                color = SensorMagnetometer
            )
            
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
                
                FilledTonalButton(
                    onClick = { viewModel.toggleSaving() },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.isMonitoring
                ) {
                    Icon(
                        imageVector = if (uiState.isSavingEnabled) 
                            Icons.Default.Save 
                        else 
                            Icons.Default.SaveAs,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (uiState.isSavingEnabled) "Saving" else "Save")
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
fun CompassVisualization(
    azimuth: Float,
    direction: String,
    modifier: Modifier = Modifier
) {
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        label = "azimuth"
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
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = minOf(size.width, size.height) / 2
                
                // Draw compass circle
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f),
                    radius = radius,
                    center = Offset(centerX, centerY)
                )
                
                // Draw compass border
                drawCircle(
                    color = Color.Gray,
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 4f)
                )
                
                // Draw cardinal directions
                val directions = listOf("N", "E", "S", "W")
                directions.forEachIndexed { index, dir ->
                    val angle = index * 90.0
                    val rad = Math.toRadians(angle)
                    val x = centerX + (radius * 0.85f * cos(rad)).toFloat()
                    val y = centerY + (radius * 0.85f * sin(rad)).toFloat()
                    
                    drawCircle(
                        color = if (dir == "N") Color.Red else Color.Gray,
                        radius = if (dir == "N") 12f else 8f,
                        center = Offset(x, y)
                    )
                }
                
                // Draw compass needle (pointing north)
                rotate(-animatedAzimuth, Offset(centerX, centerY)) {
                    // North pointer (red)
                    val path = Path().apply {
                        moveTo(centerX, centerY - radius * 0.7f)
                        lineTo(centerX - 15f, centerY)
                        lineTo(centerX + 15f, centerY)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = Color.Red
                    )
                    
                    // South pointer (white/gray)
                    val pathSouth = Path().apply {
                        moveTo(centerX, centerY + radius * 0.7f)
                        lineTo(centerX - 15f, centerY)
                        lineTo(centerX + 15f, centerY)
                        close()
                    }
                    drawPath(
                        path = pathSouth,
                        color = Color.White
                    )
                }
                
                // Center circle
                drawCircle(
                    color = Color.DarkGray,
                    radius = 10f,
                    center = Offset(centerX, centerY)
                )
            }
            
            // Direction text overlay
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) {
                Text(
                    text = direction,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${animatedAzimuth.toInt()}°",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
