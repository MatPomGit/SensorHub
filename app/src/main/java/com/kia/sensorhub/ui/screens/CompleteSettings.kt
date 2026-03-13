package com.kia.sensorhub.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kia.sensorhub.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Comprehensive Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearDataDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Appearance Section
            item {
                SectionHeader("Appearance")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = if (uiState.isDarkMode) "Enabled" else "Disabled",
                    trailing = {
                        Switch(
                            checked = uiState.isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode() }
                        )
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Dynamic Colors",
                    subtitle = if (uiState.useDynamicColors) "System colors" else "Default theme",
                    trailing = {
                        Switch(
                            checked = uiState.useDynamicColors,
                            onCheckedChange = { viewModel.toggleDynamicColors() },
                            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        )
                    }
                )
            }
            
            // Data Management
            item {
                SectionHeader("Data Management")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Save,
                    title = "Auto-save Readings",
                    subtitle = if (uiState.autoSave) "Automatically save sensor data" else "Manual save only",
                    trailing = {
                        Switch(
                            checked = uiState.autoSave,
                            onCheckedChange = { viewModel.toggleAutoSave() }
                        )
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Storage Used",
                    subtitle = "${uiState.storageUsedMB} MB of data stored",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Clear All Data",
                    subtitle = "Delete all sensor readings",
                    onClick = { showClearDataDialog = true }
                )
            }
            
            // Sensor Configuration
            item {
                SectionHeader("Sensor Configuration")
            }
            
            item {
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
                            Icon(Icons.Default.Speed, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sampling Rate",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = getSamplingRateLabel(uiState.samplingRate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Slider(
                            value = uiState.samplingRate.toFloat(),
                            onValueChange = { viewModel.setSamplingRate(it.toInt()) },
                            valueRange = 0f..3f,
                            steps = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "Battery Optimization",
                    subtitle = if (uiState.batteryOptimization) "Reduce sensor frequency" else "Full speed",
                    trailing = {
                        Switch(
                            checked = uiState.batteryOptimization,
                            onCheckedChange = { viewModel.toggleBatteryOptimization() }
                        )
                    }
                )
            }
            
            // Notifications
            item {
                SectionHeader("Notifications")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Enable Notifications",
                    subtitle = if (uiState.notificationsEnabled) "Receive alerts and insights" else "No notifications",
                    trailing = {
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications() }
                        )
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Insights,
                    title = "Daily Insights",
                    subtitle = "Receive daily sensor analysis",
                    trailing = {
                        Switch(
                            checked = uiState.dailyInsights,
                            onCheckedChange = { viewModel.toggleDailyInsights() },
                            enabled = uiState.notificationsEnabled
                        )
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.EmojiEvents,
                    title = "Achievement Alerts",
                    subtitle = "Get notified when you unlock achievements",
                    trailing = {
                        Switch(
                            checked = uiState.achievementAlerts,
                            onCheckedChange = { viewModel.toggleAchievementAlerts() },
                            enabled = uiState.notificationsEnabled
                        )
                    }
                )
            }
            
            // Privacy & Security
            item {
                SectionHeader("Privacy & Security")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Anonymous Analytics",
                    subtitle = if (uiState.analytics) "Help improve the app" else "No data collection",
                    trailing = {
                        Switch(
                            checked = uiState.analytics,
                            onCheckedChange = { viewModel.toggleAnalytics() }
                        )
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    onClick = { viewModel.openPrivacyPolicy() }
                )
            }
            
            // About
            item {
                SectionHeader("About")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "3.0.0-alpha (Build 2)",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "Open Source Licenses",
                    subtitle = "View third-party licenses",
                    onClick = { viewModel.openLicenses() }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.BugReport,
                    title = "Report a Bug",
                    subtitle = "Help us improve SensorHub",
                    onClick = { viewModel.reportBug() }
                )
            }
            
            // Reset
            item {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.resetToDefaults() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reset to Defaults")
                }
            }
        }
    }
    
    // Clear Data Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Clear All Data?") },
            text = { 
                Text("This will permanently delete all sensor readings, achievements, and settings. This action cannot be undone.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

fun getSamplingRateLabel(rate: Int): String {
    return when (rate) {
        0 -> "Fastest (~200 Hz)"
        1 -> "Fast (~100 Hz)"
        2 -> "Normal (~5 Hz)"
        3 -> "Slow (~1 Hz)"
        else -> "Normal"
    }
}

// ViewModel
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            // Load from DataStore/Preferences
            _uiState.value = _uiState.value.copy(
                isDarkMode = false, // TODO: Load from DataStore
                autoSave = true,
                notificationsEnabled = true,
                storageUsedMB = 2.5 // TODO: Calculate actual size
            )
        }
    }
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isDarkMode
            _uiState.value = _uiState.value.copy(isDarkMode = newValue)
            // TODO: Save to DataStore
        }
    }
    
    fun toggleDynamicColors() {
        viewModelScope.launch {
            val newValue = !_uiState.value.useDynamicColors
            _uiState.value = _uiState.value.copy(useDynamicColors = newValue)
            // TODO: Save to DataStore
        }
    }
    
    fun toggleAutoSave() {
        viewModelScope.launch {
            val newValue = !_uiState.value.autoSave
            _uiState.value = _uiState.value.copy(autoSave = newValue)
            // TODO: Save to DataStore
        }
    }
    
    fun setSamplingRate(rate: Int) {
        _uiState.value = _uiState.value.copy(samplingRate = rate)
        // TODO: Save and apply to sensors
    }
    
    fun toggleBatteryOptimization() {
        viewModelScope.launch {
            val newValue = !_uiState.value.batteryOptimization
            _uiState.value = _uiState.value.copy(batteryOptimization = newValue)
            // TODO: Adjust sensor rates
        }
    }
    
    fun toggleNotifications() {
        viewModelScope.launch {
            val newValue = !_uiState.value.notificationsEnabled
            _uiState.value = _uiState.value.copy(notificationsEnabled = newValue)
            // TODO: Update notification settings
        }
    }
    
    fun toggleDailyInsights() {
        viewModelScope.launch {
            val newValue = !_uiState.value.dailyInsights
            _uiState.value = _uiState.value.copy(dailyInsights = newValue)
            // TODO: Schedule/cancel daily insights
        }
    }
    
    fun toggleAchievementAlerts() {
        viewModelScope.launch {
            val newValue = !_uiState.value.achievementAlerts
            _uiState.value = _uiState.value.copy(achievementAlerts = newValue)
        }
    }
    
    fun toggleAnalytics() {
        viewModelScope.launch {
            val newValue = !_uiState.value.analytics
            _uiState.value = _uiState.value.copy(analytics = newValue)
        }
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAllReadings()
            _uiState.value = _uiState.value.copy(storageUsedMB = 0.0)
        }
    }
    
    fun resetToDefaults() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState()
            // TODO: Clear DataStore
        }
    }
    
    fun openPrivacyPolicy() {
        // TODO: Open privacy policy URL
    }
    
    fun openLicenses() {
        // TODO: Show licenses screen
    }
    
    fun reportBug() {
        // TODO: Open bug report form/email
    }
}

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val useDynamicColors: Boolean = true,
    val autoSave: Boolean = true,
    val samplingRate: Int = 2,
    val batteryOptimization: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val dailyInsights: Boolean = true,
    val achievementAlerts: Boolean = true,
    val analytics: Boolean = false,
    val storageUsedMB: Double = 0.0
)
