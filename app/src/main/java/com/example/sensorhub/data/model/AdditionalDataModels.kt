package com.kia.sensorhub.data.model

/**
 * Sensor configuration
 */
data class SensorConfig(
    val sensorType: SensorType,
    val samplingRate: SamplingRate = SamplingRate.NORMAL,
    val isEnabled: Boolean = true,
    val autoSave: Boolean = false
)

// Other models (LightData, GpsData, etc.) are now in SensorData.kt to avoid redeclarations.
