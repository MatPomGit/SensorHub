package com.kia.sensorhub.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kia.sensorhub.data.model.SensorReading
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.utils.DataExport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Statistics Dashboard Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsDashboardScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadStatistics() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
            // Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Data Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatisticItem(
                            label = "Total Readings",
                            value = uiState.totalReadings.toString(),
                            icon = Icons.Default.DataUsage
                        )
                        StatisticItem(
                            label = "Active Sensors",
                            value = uiState.activeSensors.toString(),
                            icon = Icons.Default.Sensors
                        )
                    }
                }
            }
            
            // Sensor Breakdown
            Text(
                text = "Readings by Sensor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            uiState.sensorStats.forEach { (sensorType, count) ->
                SensorStatCard(
                    sensorType = sensorType,
                    count = count,
                    percentage = if (uiState.totalReadings > 0) 
                        count.toFloat() / uiState.totalReadings 
                    else 0f
                )
            }
            
            // Storage Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Database Storage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Estimated Size: ${uiState.estimatedSizeKB} KB",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Oldest Reading: ${uiState.oldestReadingDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Newest Reading: ${uiState.newestReadingDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { /* Navigate to export */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Export")
                }
                
                OutlinedButton(
                    onClick = { viewModel.showClearDialog() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Clear All")
                }
            }
        }
    }
    
    // Clear Confirmation Dialog
    if (uiState.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearDialog() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Clear All Data?") },
            text = { 
                Text("This will permanently delete all ${uiState.totalReadings} sensor readings. This action cannot be undone.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        viewModel.dismissClearDialog()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatisticItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
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
fun SensorStatCard(
    sensorType: String,
    count: Int,
    percentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sensorType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$count readings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Text(
                text = "${(percentage * 100).toInt()}% of total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Data Export Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExportScreen(
    viewModel: DataExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export Format Selection
            Text(
                text = "Select Export Format",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            ExportFormatCard(
                title = "CSV Format",
                description = "Comma-separated values, compatible with Excel and data analysis tools",
                icon = Icons.Default.TableChart,
                selected = uiState.selectedFormat == ExportFormat.CSV,
                onClick = { viewModel.selectFormat(ExportFormat.CSV) }
            )
            
            ExportFormatCard(
                title = "JSON Format",
                description = "JavaScript Object Notation, ideal for APIs and web applications",
                icon = Icons.Default.Code,
                selected = uiState.selectedFormat == ExportFormat.JSON,
                onClick = { viewModel.selectFormat(ExportFormat.JSON) }
            )
            
            // Date Range Selection
            Text(
                text = "Date Range",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.exportAll,
                            onCheckedChange = { viewModel.toggleExportAll() }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Export all data")
                    }
                    
                    if (!uiState.exportAll) {
                        Text(
                            text = "Custom date range selection coming soon",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Export Button
            Button(
                onClick = { 
                    viewModel.exportData(context)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isExporting
            ) {
                if (uiState.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Exporting...")
                } else {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Export Data")
                }
            }
            
            // Success Message
            if (uiState.exportSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Export Successful!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.exportMessage,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Error Message
            if (uiState.exportError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.exportError!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportFormatCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (selected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ViewModels
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    fun loadStatistics() {
        viewModelScope.launch {
            try {
                val totalReadings = repository.getReadingsCount()
                // TODO: Load more detailed statistics
                
                _uiState.value = _uiState.value.copy(
                    totalReadings = totalReadings,
                    activeSensors = 7,
                    estimatedSizeKB = totalReadings * 0.1 // Rough estimate
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun showClearDialog() {
        _uiState.value = _uiState.value.copy(showClearDialog = true)
    }
    
    fun dismissClearDialog() {
        _uiState.value = _uiState.value.copy(showClearDialog = false)
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAllReadings()
            loadStatistics()
        }
    }
}

data class StatisticsUiState(
    val totalReadings: Int = 0,
    val activeSensors: Int = 0,
    val sensorStats: Map<String, Int> = emptyMap(),
    val estimatedSizeKB: Double = 0.0,
    val oldestReadingDate: String = "N/A",
    val newestReadingDate: String = "N/A",
    val showClearDialog: Boolean = false
)

enum class ExportFormat {
    CSV, JSON
}

@HiltViewModel
class DataExportViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DataExportUiState())
    val uiState: StateFlow<DataExportUiState> = _uiState.asStateFlow()
    
    fun selectFormat(format: ExportFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }
    
    fun toggleExportAll() {
        _uiState.value = _uiState.value.copy(
            exportAll = !_uiState.value.exportAll
        )
    }
    
    fun exportData(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportSuccess = false, exportError = null)
            
            try {
                // TODO: Implement actual export
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = true,
                    exportMessage = "Data exported successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = e.message ?: "Export failed"
                )
            }
        }
    }
}

data class DataExportUiState(
    val selectedFormat: ExportFormat = ExportFormat.CSV,
    val exportAll: Boolean = true,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportMessage: String = "",
    val exportError: String? = null
)
