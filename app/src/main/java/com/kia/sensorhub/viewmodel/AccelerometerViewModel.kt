package com.kia.sensorhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kia.sensorhub.data.model.AccelerometerData
import com.kia.sensorhub.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Accelerometer screen
 * Manages accelerometer data and state
 */
@HiltViewModel
class AccelerometerViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AccelerometerUiState())
    val uiState: StateFlow<AccelerometerUiState> = _uiState.asStateFlow()
    
    private var monitoringJob: Job? = null
    
    init {
        checkSensorAvailability()
    }
    
    /**
     * Check if accelerometer is available
     */
    private fun checkSensorAvailability() {
        val isAvailable = repository.isAccelerometerAvailable()
        val sensorInfo = repository.getAccelerometerInfo()
        
        _uiState.value = _uiState.value.copy(
            isAvailable = isAvailable,
            sensorInfo = sensorInfo
        )
    }
    
    /**
     * Start monitoring accelerometer
     */
    fun startMonitoring() {
        if (!_uiState.value.isAvailable) {
            _uiState.value = _uiState.value.copy(
                error = "Accelerometer is not available on this device"
            )
            return
        }
        
        if (monitoringJob?.isActive == true) {
            return // Already monitoring
        }
        
        monitoringJob = viewModelScope.launch {
            repository.getAccelerometerFlow()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isMonitoring = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { data ->
                    _uiState.value = _uiState.value.copy(
                        isMonitoring = true,
                        currentData = data,
                        dataHistory = (_uiState.value.dataHistory + data).takeLast(100),
                        error = null
                    )
                    
                    // Optionally save to database
                    if (_uiState.value.isSavingEnabled) {
                        repository.saveSensorReading(data)
                    }
                }
        }
    }
    
    /**
     * Stop monitoring accelerometer
     */
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }
    
    /**
     * Toggle saving to database
     */
    fun toggleSaving() {
        _uiState.value = _uiState.value.copy(
            isSavingEnabled = !_uiState.value.isSavingEnabled
        )
    }
    
    /**
     * Clear data history
     */
    fun clearHistory() {
        _uiState.value = _uiState.value.copy(dataHistory = emptyList())
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}

/**
 * UI State for Accelerometer screen
 */
data class AccelerometerUiState(
    val isAvailable: Boolean = false,
    val isMonitoring: Boolean = false,
    val isSavingEnabled: Boolean = false,
    val currentData: AccelerometerData = AccelerometerData(),
    val dataHistory: List<AccelerometerData> = emptyList(),
    val sensorInfo: com.kia.sensorhub.sensors.SensorInfo? = null,
    val error: String? = null
)
