package com.kia.sensorhub.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Result wrapper for operations that can fail
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Extension function to wrap Flow with Result
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .catch { emit(Result.Error(it)) }
}

/**
 * Error handler for sensor operations
 */
object ErrorHandler {
    private const val TAG = "SensorHub"
    
    /**
     * Log error with tag
     */
    fun logError(tag: String = TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
    
    /**
     * Log warning
     */
    fun logWarning(tag: String = TAG, message: String) {
        Log.w(tag, message)
    }
    
    /**
     * Log info
     */
    fun logInfo(tag: String = TAG, message: String) {
        Log.i(tag, message)
    }
    
    /**
     * Get user-friendly error message
     */
    fun getUserFriendlyMessage(throwable: Throwable): String {
        return when (throwable) {
            is SecurityException -> "Permission denied. Please grant required permissions."
            is IllegalStateException -> "The sensor is not available. Please check your device."
            is IllegalArgumentException -> "Invalid data provided. Please try again."
            is kotlinx.coroutines.CancellationException -> "Operation was cancelled."
            else -> throwable.message ?: "An unexpected error occurred. Please try again."
        }
    }
    
    /**
     * Handle error and return user message
     */
    fun handleError(throwable: Throwable, context: String = ""): String {
        val contextMsg = if (context.isNotEmpty()) "[$context] " else ""
        val message = "$contextMsg${getUserFriendlyMessage(throwable)}"
        logError(message = message, throwable = throwable)
        return message
    }
}

/**
 * Validator for sensor data
 */
object SensorDataValidator {
    
    /**
     * Validate accelerometer data
     */
    fun validateAccelerometerData(x: Float, y: Float, z: Float): Boolean {
        return DataValidator.isValidSensorValue(x) &&
               DataValidator.isValidSensorValue(y) &&
               DataValidator.isValidSensorValue(z) &&
               x in -20f..20f &&
               y in -20f..20f &&
               z in -20f..20f
    }
    
    /**
     * Validate gyroscope data
     */
    fun validateGyroscopeData(x: Float, y: Float, z: Float): Boolean {
        return DataValidator.isValidSensorValue(x) &&
               DataValidator.isValidSensorValue(y) &&
               DataValidator.isValidSensorValue(z) &&
               x in -10f..10f &&
               y in -10f..10f &&
               z in -10f..10f
    }
    
    /**
     * Validate magnetometer data
     */
    fun validateMagnetometerData(x: Float, y: Float, z: Float): Boolean {
        return DataValidator.isValidSensorValue(x) &&
               DataValidator.isValidSensorValue(y) &&
               DataValidator.isValidSensorValue(z) &&
               x in -500f..500f &&
               y in -500f..500f &&
               z in -500f..500f
    }
    
    /**
     * Validate light sensor data
     */
    fun validateLightData(illuminance: Float): Boolean {
        return DataValidator.isValidSensorValue(illuminance) &&
               illuminance >= 0f &&
               illuminance <= 100000f
    }
    
    /**
     * Validate GPS data
     */
    fun validateGpsData(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 &&
               longitude in -180.0..180.0
    }
    
    /**
     * Validate proximity data
     */
    fun validateProximityData(distance: Float): Boolean {
        return DataValidator.isValidSensorValue(distance) &&
               distance >= 0f
    }
    
    /**
     * Validate barometer data
     */
    fun validateBarometerData(pressure: Float): Boolean {
        return DataValidator.isValidSensorValue(pressure) &&
               pressure in 300f..1100f // Typical atmospheric pressure range
    }
}

/**
 * Try-catch wrapper for suspend functions
 */
suspend fun <T> tryCatch(
    onError: ((Throwable) -> Unit)? = null,
    block: suspend () -> T
): T? {
    return try {
        block()
    } catch (e: Exception) {
        onError?.invoke(e)
        ErrorHandler.logError(message = "Error in suspend function", throwable = e)
        null
    }
}

/**
 * Try-catch wrapper for regular functions
 */
fun <T> tryOrNull(
    onError: ((Throwable) -> Unit)? = null,
    block: () -> T
): T? {
    return try {
        block()
    } catch (e: Exception) {
        onError?.invoke(e)
        ErrorHandler.logError(message = "Error in function", throwable = e)
        null
    }
}

/**
 * Retry mechanism for operations
 */
suspend fun <T> retryIO(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            ErrorHandler.logWarning(message = "Retry attempt ${it + 1} failed")
        }
        kotlinx.coroutines.delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // Last attempt
}

/**
 * Safe flow collector
 */
fun <T> Flow<T>.catchAndLog(
    tag: String = "SensorHub",
    defaultValue: T? = null
): Flow<T> {
    return this.catch { throwable ->
        ErrorHandler.logError(tag = tag, message = "Flow error", throwable = throwable)
        defaultValue?.let { emit(it) }
    }
}

/**
 * Permission checker helper
 */
object PermissionHelper {
    
    /**
     * Check if sensor permission is required
     */
    fun isPermissionRequired(sensorType: String): Boolean {
        return when (sensorType.lowercase()) {
            "gps", "location" -> true
            "microphone", "audio" -> true
            else -> false
        }
    }
    
    /**
     * Get required permissions for sensor
     */
    fun getRequiredPermissions(sensorType: String): List<String> {
        return when (sensorType.lowercase()) {
            "gps", "location" -> listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            "microphone", "audio" -> listOf(
                android.Manifest.permission.RECORD_AUDIO
            )
            else -> emptyList()
        }
    }
}

/**
 * Data sanitizer for sensor readings
 */
object DataSanitizer {
    
    /**
     * Sanitize float value
     */
    fun sanitizeFloat(value: Float, min: Float = -1000f, max: Float = 1000f): Float {
        return when {
            value.isNaN() -> 0f
            value.isInfinite() -> if (value > 0) max else min
            value < min -> min
            value > max -> max
            else -> value
        }
    }
    
    /**
     * Sanitize double value
     */
    fun sanitizeDouble(value: Double, min: Double = -1000.0, max: Double = 1000.0): Double {
        return when {
            value.isNaN() -> 0.0
            value.isInfinite() -> if (value > 0) max else min
            value < min -> min
            value > max -> max
            else -> value
        }
    }
    
    /**
     * Remove outliers from list using IQR method
     */
    fun removeOutliers(data: List<Float>): List<Float> {
        if (data.size < 4) return data
        
        val sorted = data.sorted()
        val q1Index = (sorted.size * 0.25).toInt()
        val q3Index = (sorted.size * 0.75).toInt()
        
        val q1 = sorted[q1Index]
        val q3 = sorted[q3Index]
        val iqr = q3 - q1
        
        val lowerBound = q1 - 1.5f * iqr
        val upperBound = q3 + 1.5f * iqr
        
        return data.filter { it in lowerBound..upperBound }
    }
}

/**
 * Debouncer for sensor events
 */
class Debouncer(private val waitMs: Long = 300) {
    private var lastActionTime: Long = 0

    fun debounce(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime > waitMs) {
            lastActionTime = currentTime
            action()
        }
    }
}

/**
 * Throttler for sensor events
 */
class Throttler(private val periodMs: Long = 1000) {
    @Volatile private var isWaiting = false
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var resetJob: Job? = null

    @Synchronized
    fun throttle(action: () -> Unit) {
        if (!isWaiting) {
            action()
            isWaiting = true

            resetJob?.cancel()
            resetJob = scope.launch {
                delay(periodMs)
                isWaiting = false
            }
        }
    }

    fun cancel() {
        resetJob?.cancel()
        isWaiting = false
    }
}
