package com.kia.sensorhub.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.kia.sensorhub.data.model.MagnetometerData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Manager for magnetometer sensor
 * Provides real-time magnetic field data and compass heading via Flow
 */
class MagnetometerManager(context: Context) {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    
    fun isAvailable(): Boolean = magnetometer != null
    
    fun getMagnetometerFlow(
        samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_UI
    ): Flow<MagnetometerData> = callbackFlow {
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        gravity = event.values.clone()
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        geomagnetic = event.values.clone()
                        
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        
                        // Calculate magnitude
                        val magnitude = sqrt(x * x + y * y + z * z)
                        
                        // Calculate azimuth (compass heading)
                        var azimuth = 0f
                        if (gravity != null && geomagnetic != null) {
                            val R = FloatArray(9)
                            val I = FloatArray(9)
                            
                            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                                val orientation = FloatArray(3)
                                SensorManager.getOrientation(R, orientation)
                                // Convert radians to degrees
                                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                                // Normalize to 0-360
                                if (azimuth < 0) azimuth += 360f
                            }
                        }
                        
                        val data = MagnetometerData(
                            timestamp = System.currentTimeMillis(),
                            x = x,
                            y = y,
                            z = z,
                            magnitude = magnitude,
                            azimuth = azimuth
                        )
                        
                        trySend(data)
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        
        // Register both sensors for azimuth calculation
        magnetometer?.let {
            sensorManager.registerListener(listener, it, samplingPeriodUs)
        }
        accelerometer?.let {
            sensorManager.registerListener(listener, it, samplingPeriodUs)
        }
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    fun getSensorInfo(): SensorInfo? {
        return magnetometer?.let {
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
