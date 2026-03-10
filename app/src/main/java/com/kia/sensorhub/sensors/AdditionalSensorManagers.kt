package com.kia.sensorhub.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager as AndroidSensorManager
import com.kia.sensorhub.data.model.LightData
import com.kia.sensorhub.data.model.ProximityData
import com.kia.sensorhub.utils.ErrorHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Light Sensor Manager
 * Measures ambient light in lux
 */
@Singleton
class LightSensorManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as AndroidSensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    
    /**
     * Check if light sensor is available
     */
    fun isAvailable(): Boolean = lightSensor != null
    
    /**
     * Get light sensor data as Flow
     */
    fun getLightFlow(): Flow<LightData> = callbackFlow {
        if (lightSensor == null) {
            ErrorHandler.logError(
                tag = "LightSensorManager",
                message = "Light sensor not available"
            )
            close()
            return@callbackFlow
        }
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LIGHT) {
                    val illuminance = event.values[0]
                    trySend(LightData(illuminance = illuminance))
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not used for light sensor
            }
        }
        
        val registered = sensorManager.registerListener(
            listener,
            lightSensor,
            AndroidSensorManager.SENSOR_DELAY_NORMAL
        )
        
        if (!registered) {
            ErrorHandler.logError(
                tag = "LightSensorManager",
                message = "Failed to register light sensor listener"
            )
            close()
        }
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    /**
     * Get sensor information
     */
    fun getSensorInfo(): SensorInfo? {
        return lightSensor?.toSensorInfo()
    }
    
    /**
     * Get light level description
     */
    fun getLightLevelDescription(lux: Float): String {
        return when {
            lux < 1 -> "Pitch Black"
            lux < 10 -> "Very Dark"
            lux < 50 -> "Dark"
            lux < 200 -> "Dim"
            lux < 400 -> "Normal Indoor"
            lux < 1000 -> "Bright Indoor"
            lux < 10000 -> "Overcast Daylight"
            lux < 25000 -> "Full Daylight"
            else -> "Direct Sunlight"
        }
    }
    
    /**
     * Check if environment is suitable for reading
     */
    fun isGoodForReading(lux: Float): Boolean {
        return lux in 300f..1000f
    }
    
    /**
     * Get recommended screen brightness (0.0 - 1.0)
     */
    fun getRecommendedBrightness(lux: Float): Float {
        return when {
            lux < 10 -> 0.1f
            lux < 50 -> 0.3f
            lux < 200 -> 0.5f
            lux < 1000 -> 0.7f
            lux < 10000 -> 0.9f
            else -> 1.0f
        }
    }
}

/**
 * Proximity Sensor Manager
 * Detects nearby objects
 */
@Singleton
class ProximitySensorManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as AndroidSensorManager
    private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    
    /**
     * Check if proximity sensor is available
     */
    fun isAvailable(): Boolean = proximitySensor != null
    
    /**
     * Get proximity sensor data as Flow
     */
    fun getProximityFlow(): Flow<ProximityData> = callbackFlow {
        if (proximitySensor == null) {
            ErrorHandler.logError(
                tag = "ProximitySensorManager",
                message = "Proximity sensor not available"
            )
            close()
            return@callbackFlow
        }
        
        val maxRange = proximitySensor.maximumRange
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                    val distance = event.values[0]
                    val isNear = distance < maxRange
                    
                    trySend(
                        ProximityData(
                            distance = distance,
                            isNear = isNear,
                            maxRange = maxRange
                        )
                    )
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not critical for proximity sensor
            }
        }
        
        val registered = sensorManager.registerListener(
            listener,
            proximitySensor,
            AndroidSensorManager.SENSOR_DELAY_NORMAL
        )
        
        if (!registered) {
            ErrorHandler.logError(
                tag = "ProximitySensorManager",
                message = "Failed to register proximity sensor listener"
            )
            close()
        }
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    /**
     * Get sensor information
     */
    fun getSensorInfo(): SensorInfo? {
        return proximitySensor?.toSensorInfo()
    }
    
    /**
     * Get max detection range
     */
    fun getMaxRange(): Float {
        return proximitySensor?.maximumRange ?: 5f
    }
    
    /**
     * Get proximity state description
     */
    fun getProximityDescription(distance: Float, maxRange: Float): String {
        return when {
            distance < 1 -> "Very Close (<1cm)"
            distance < 3 -> "Close (${distance.toInt()}cm)"
            distance < maxRange -> "Near (${distance.toInt()}cm)"
            else -> "Far (>${maxRange.toInt()}cm)"
        }
    }
}

/**
 * Barometer (Pressure) Sensor Manager
 */
@Singleton
class BarometerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as AndroidSensorManager
    private val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
    
    /**
     * Check if barometer is available
     */
    fun isAvailable(): Boolean = pressureSensor != null
    
    /**
     * Get pressure sensor data as Flow
     */
    fun getPressureFlow(): Flow<Float> = callbackFlow {
        if (pressureSensor == null) {
            ErrorHandler.logError(
                tag = "BarometerManager",
                message = "Pressure sensor not available"
            )
            close()
            return@callbackFlow
        }
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                    val pressure = event.values[0] // in hPa (millibar)
                    trySend(pressure)
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not critical for pressure sensor
            }
        }
        
        val registered = sensorManager.registerListener(
            listener,
            pressureSensor,
            AndroidSensorManager.SENSOR_DELAY_NORMAL
        )
        
        if (!registered) {
            ErrorHandler.logError(
                tag = "BarometerManager",
                message = "Failed to register pressure sensor listener"
            )
            close()
        }
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    /**
     * Get sensor information
     */
    fun getSensorInfo(): SensorInfo? {
        return pressureSensor?.toSensorInfo()
    }
    
    /**
     * Calculate altitude from pressure
     * Using standard barometric formula
     */
    fun calculateAltitude(pressure: Float, seaLevelPressure: Float = 1013.25f): Float {
        return 44330f * (1f - Math.pow((pressure / seaLevelPressure).toDouble(), (1.0 / 5.255))).toFloat()
    }
    
    /**
     * Get weather forecast based on pressure
     */
    fun getWeatherForecast(pressure: Float): String {
        return when {
            pressure < 980 -> "Stormy"
            pressure < 1000 -> "Rainy"
            pressure < 1013 -> "Cloudy"
            pressure < 1020 -> "Fair"
            pressure < 1030 -> "Clear"
            else -> "Very Dry"
        }
    }
    
    /**
     * Get pressure trend from history
     */
    fun getPressureTrend(history: List<Float>): String {
        if (history.size < 2) return "Unknown"
        
        val recent = history.takeLast(5).average()
        val older = history.take(5).average()
        
        return when {
            recent > older + 2 -> "Rising Rapidly"
            recent > older + 0.5 -> "Rising"
            recent < older - 2 -> "Falling Rapidly"
            recent < older - 0.5 -> "Falling"
            else -> "Steady"
        }
    }
}

/**
 * All Sensors Manager
 * Provides access to all available sensors
 */
@Singleton
class AllSensorsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as AndroidSensorManager
    
    /**
     * Get list of all available sensors on device
     */
    fun getAllSensors(): List<SensorInfo> {
        return sensorManager.getSensorList(Sensor.TYPE_ALL)
            .map { it.toSensorInfo() }
    }
    
    /**
     * Get sensors by type
     */
    fun getSensorsByType(type: Int): List<SensorInfo> {
        return sensorManager.getSensorList(type)
            .map { it.toSensorInfo() }
    }
    
    /**
     * Get sensor count
     */
    fun getSensorCount(): Int {
        return sensorManager.getSensorList(Sensor.TYPE_ALL).size
    }
    
    /**
     * Check if specific sensor type exists
     */
    fun hasSensor(type: Int): Boolean {
        return sensorManager.getDefaultSensor(type) != null
    }
    
    /**
     * Get sensor capabilities summary
     */
    fun getSensorCapabilities(): Map<String, Boolean> {
        return mapOf(
            "Accelerometer" to hasSensor(Sensor.TYPE_ACCELEROMETER),
            "Gyroscope" to hasSensor(Sensor.TYPE_GYROSCOPE),
            "Magnetometer" to hasSensor(Sensor.TYPE_MAGNETIC_FIELD),
            "Light" to hasSensor(Sensor.TYPE_LIGHT),
            "Proximity" to hasSensor(Sensor.TYPE_PROXIMITY),
            "Pressure" to hasSensor(Sensor.TYPE_PRESSURE),
            "Temperature" to hasSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
            "Humidity" to hasSensor(Sensor.TYPE_RELATIVE_HUMIDITY),
            "Gravity" to hasSensor(Sensor.TYPE_GRAVITY),
            "Linear Acceleration" to hasSensor(Sensor.TYPE_LINEAR_ACCELERATION),
            "Rotation Vector" to hasSensor(Sensor.TYPE_ROTATION_VECTOR),
            "Step Counter" to hasSensor(Sensor.TYPE_STEP_COUNTER),
            "Step Detector" to hasSensor(Sensor.TYPE_STEP_DETECTOR),
            "Heart Rate" to hasSensor(Sensor.TYPE_HEART_RATE)
        )
    }
}
