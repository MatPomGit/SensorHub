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
