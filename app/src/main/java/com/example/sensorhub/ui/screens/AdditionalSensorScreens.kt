package com.kia.sensorhub.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kia.sensorhub.data.model.*
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.sensors.SensorInfo
import com.kia.sensorhub.ui.components.SensorCard
import com.kia.sensorhub.ui.components.SensorInfoDialog
import com.kia.sensorhub.ui.theme.*
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
 * Proximity Sensor Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProximityScreen(
    viewModel: ProximityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proximity Sensor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SensorProximity,
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
            // Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.currentData.isNear)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.currentData.isNear)
                            Icons.Default.Warning
                        else
                            Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Column {
                        Text(
                            text = if (uiState.currentData.isNear) "NEAR" else "FAR",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.currentData.isNear)
                                "Object detected nearby"
                            else
                                "No object nearby",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Distance
            SensorCard(
                label = "Distance",
                value = uiState.currentData.distance,
                unit = "cm",
                color = SensorProximity
            )
            
            // Controls
            Button(
                onClick = { 
                    if (uiState.isMonitoring) {
                        viewModel.stopMonitoring()
                    } else {
                        viewModel.startMonitoring()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
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
        }
    }
    
    if (showInfoDialog && uiState.sensorInfo != null) {
        SensorInfoDialog(
            sensorInfo = uiState.sensorInfo!!,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@HiltViewModel
class ProximityViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProximityUiState())
    val uiState: StateFlow<ProximityUiState> = _uiState.asStateFlow()
    
    private var monitoringJob: Job? = null
    
    init {
        checkSensorAvailability()
    }
    
    private fun checkSensorAvailability() {
        _uiState.value = _uiState.value.copy(isAvailable = false) // TODO: Add to repository
    }
    
    fun startMonitoring() {
        // TODO: Implement
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }
}

data class ProximityUiState(
    val isAvailable: Boolean = false,
    val isMonitoring: Boolean = false,
    val currentData: ProximityData = ProximityData(),
    val sensorInfo: SensorInfo? = null
)
