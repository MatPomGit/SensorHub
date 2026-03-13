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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kia.sensorhub.ui.components.SensorCard
import com.kia.sensorhub.ui.components.SensorInfoDialog
import com.kia.sensorhub.ui.theme.SensorAccelerometer
import com.kia.sensorhub.viewmodel.AccelerometerViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccelerometerScreen(
    viewModel: AccelerometerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accelerometer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SensorAccelerometer,
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
                            contentDescription = null,
                            tint = if (uiState.isAvailable) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
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
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error"
                            )
                        }
                    }
                }
            }
            
            // Current Values
            Text(
                text = "Current Values",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            SensorCard(
                label = "X-Axis",
                value = uiState.currentData.x,
                unit = "m/s²",
                color = Color.Red
            )
            
            SensorCard(
                label = "Y-Axis",
                value = uiState.currentData.y,
                unit = "m/s²",
                color = Color.Green
            )
            
            SensorCard(
                label = "Z-Axis",
                value = uiState.currentData.z,
                unit = "m/s²",
                color = Color.Blue
            )
            
            SensorCard(
                label = "Magnitude",
                value = uiState.currentData.magnitude,
                unit = "m/s²",
                color = SensorAccelerometer
            )
            
            // Visualization
            Text(
                text = "Real-time Visualization",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            AccelerometerVisualization(
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
    
    // Info Dialog
    if (showInfoDialog && uiState.sensorInfo != null) {
        SensorInfoDialog(
            sensorInfo = uiState.sensorInfo!!,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@Composable
fun AccelerometerVisualization(
    x: Float,
    y: Float,
    z: Float,
    modifier: Modifier = Modifier
) {
    val animatedX by animateFloatAsState(targetValue = x, label = "x")
    val animatedY by animateFloatAsState(targetValue = y, label = "y")

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
            val scale = 20f // Scale factor for visualization
            
            // Draw axes
            drawLine(
                color = Color.Gray,
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Gray,
                start = Offset(centerX, 0f),
                end = Offset(centerX, size.height),
                strokeWidth = 2f
            )
            
            // Draw X vector (red)
            drawLine(
                color = Color.Red,
                start = Offset(centerX, centerY),
                end = Offset(centerX + animatedX * scale, centerY),
                strokeWidth = 4f
            )
            
            // Draw Y vector (green)
            drawLine(
                color = Color.Green,
                start = Offset(centerX, centerY),
                end = Offset(centerX, centerY - animatedY * scale),
                strokeWidth = 4f
            )
            
            // Draw resultant vector (blue-ish)
            drawLine(
                color = Color.Cyan,
                start = Offset(centerX, centerY),
                end = Offset(
                    centerX + animatedX * scale,
                    centerY - animatedY * scale
                ),
                strokeWidth = 6f
            )
            
            // Draw center point
            drawCircle(
                color = Color.Black,
                radius = 6f,
                center = Offset(centerX, centerY)
            )
        }
    }
}
