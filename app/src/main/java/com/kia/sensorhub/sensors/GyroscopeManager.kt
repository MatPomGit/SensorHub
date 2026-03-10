package com.kia.sensorhub.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.kia.sensorhub.data.model.GyroscopeData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

/**
 * Manager for gyroscope sensor
 * Provides real-time gyroscope data (rotation rate) via Flow
 */
class GyroscopeManager(context: Context) {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    
    fun isAvailable(): Boolean = gyroscope != null
    
    fun getGyroscopeFlow(
        samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_UI
    ): Flow<GyroscopeData> = callbackFlow {
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    val x = event.values[0] // Rotation around x-axis (rad/s)
                    val y = event.values[1] // Rotation around y-axis (rad/s)
                    val z = event.values[2] // Rotation around z-axis (rad/s)
                    
                    // Calculate total rotation rate
                    val rotationRate = sqrt(x * x + y * y + z * z)
                    
                    val data = GyroscopeData(
                        timestamp = System.currentTimeMillis(),
                        x = x,
                        y = y,
                        z = z,
                        rotationRate = rotationRate
                    )
                    
                    trySend(data)
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        
        gyroscope?.let {
            sensorManager.registerListener(listener, it, samplingPeriodUs)
        }
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    fun getSensorInfo(): SensorInfo? {
        return gyroscope?.let {
            SensorInfo(
                name = it.name,
                vendor = it.vendor,
                version = it.version,
                type = it.type,
                maxRange = it.maximumRange,
                resolution = it.resolution,
                power = it.power,
                minDelay = it.minDelay
            )
        }
    }
}
