package com.kia.sensorhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kia.sensorhub.data.model.GyroscopeData
import com.kia.sensorhub.data.model.MagnetometerData
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.sensors.SensorInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Gyroscope screen
 */
@HiltViewModel
class GyroscopeViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GyroscopeUiState())
    val uiState: StateFlow<GyroscopeUiState> = _uiState.asStateFlow()
    
    private var monitoringJob: Job? = null
    
    init {
        checkSensorAvailability()
    }
    
    private fun checkSensorAvailability() {
        val isAvailable = repository.isGyroscopeAvailable()
        val sensorInfo = repository.getGyroscopeInfo()
        
        _uiState.value = _uiState.value.copy(
            isAvailable = isAvailable,
            sensorInfo = sensorInfo
        )
    }
    
    fun startMonitoring() {
        if (!_uiState.value.isAvailable) {
            _uiState.value = _uiState.value.copy(
                error = "Gyroscope is not available on this device"
            )
            return
        }
        
        if (monitoringJob?.isActive == true) return
        
        monitoringJob = viewModelScope.launch {
            repository.getGyroscopeFlow()
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
                    
                    if (_uiState.value.isSavingEnabled) {
                        repository.saveSensorReading(data)
                    }
                }
        }
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }
    
    fun toggleSaving() {
        _uiState.value = _uiState.value.copy(
            isSavingEnabled = !_uiState.value.isSavingEnabled
        )
    }
    
    fun clearHistory() {
        _uiState.value = _uiState.value.copy(dataHistory = emptyList())
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}

data class GyroscopeUiState(
    val isAvailable: Boolean = false,
    val isMonitoring: Boolean = false,
    val isSavingEnabled: Boolean = false,
    val currentData: GyroscopeData = GyroscopeData(),
    val dataHistory: List<GyroscopeData> = emptyList(),
    val sensorInfo: SensorInfo? = null,
    val error: String? = null
)

/**
 * ViewModel for Magnetometer screen
 */
@HiltViewModel
class MagnetometerViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MagnetometerUiState())
    val uiState: StateFlow<MagnetometerUiState> = _uiState.asStateFlow()
    
    private var monitoringJob: Job? = null
    
    init {
        checkSensorAvailability()
    }
    
    private fun checkSensorAvailability() {
        val isAvailable = repository.isMagnetometerAvailable()
        val sensorInfo = repository.getMagnetometerInfo()
        
        _uiState.value = _uiState.value.copy(
            isAvailable = isAvailable,
            sensorInfo = sensorInfo
        )
    }
    
    fun startMonitoring() {
        if (!_uiState.value.isAvailable) {
            _uiState.value = _uiState.value.copy(
                error = "Magnetometer is not available on this device"
            )
            return
        }
        
        if (monitoringJob?.isActive == true) return
        
        monitoringJob = viewModelScope.launch {
            repository.getMagnetometerFlow()
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
                    
                    if (_uiState.value.isSavingEnabled) {
                        repository.saveSensorReading(data)
                    }
                }
        }
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }
    
    fun toggleSaving() {
        _uiState.value = _uiState.value.copy(
            isSavingEnabled = !_uiState.value.isSavingEnabled
        )
    }
    
    fun clearHistory() {
        _uiState.value = _uiState.value.copy(dataHistory = emptyList())
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getCompassDirection(azimuth: Float): String {
        return when (azimuth) {
            in 0f..22.5f, in 337.5f..360f -> "North"
            in 22.5f..67.5f -> "Northeast"
            in 67.5f..112.5f -> "East"
            in 112.5f..157.5f -> "Southeast"
            in 157.5f..202.5f -> "South"
            in 202.5f..247.5f -> "Southwest"
            in 247.5f..292.5f -> "West"
            in 292.5f..337.5f -> "Northwest"
            else -> "Unknown"
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}

data class MagnetometerUiState(
    val isAvailable: Boolean = false,
    val isMonitoring: Boolean = false,
    val isSavingEnabled: Boolean = false,
    val currentData: MagnetometerData = MagnetometerData(),
    val dataHistory: List<MagnetometerData> = emptyList(),
    val sensorInfo: SensorInfo? = null,
    val error: String? = null
)
