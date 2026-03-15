package com.kia.sensorhub.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.kia.sensorhub.data.model.AccelerometerData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

/**
 * Manager for accelerometer sensor
 * Provides real-time accelerometer data via Flow
 */
class AccelerometerManager(context: Context) {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    /**
     * Check if accelerometer is available on this device
     */
    fun isAvailable(): Boolean = accelerometer != null
    
    /**
     * Get accelerometer data as a Flow
     * @param samplingPeriodUs Sampling period in microseconds (default: SENSOR_DELAY_UI)
     */
    fun getAccelerometerFlow(
        samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_UI
    ): Flow<AccelerometerData> = callbackFlow {
        val accelerometerSensor = accelerometer
            ?: run {
                close(IllegalStateException("Accelerometer is not available on this device"))
                return@callbackFlow
            }
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    
                    // Calculate magnitude
                    val magnitude = sqrt(x * x + y * y + z * z)
                    
                    val data = AccelerometerData(
                        timestamp = System.currentTimeMillis(),
                        x = x,
                        y = y,
                        z = z,
                        magnitude = magnitude
                    )
                    
                    trySend(data)
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                // Handle accuracy changes if needed
            }
        }
        
        // Register listener
        val registered = sensorManager.registerListener(listener, accelerometerSensor, samplingPeriodUs)
        if (!registered) {
            close(IllegalStateException("Failed to register accelerometer listener"))
            return@callbackFlow
        }
        
        // Unregister when Flow is cancelled
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    /**
     * Get sensor information
     */
    fun getSensorInfo(): SensorInfo? {
        return accelerometer?.toSensorInfo()
    }
}
