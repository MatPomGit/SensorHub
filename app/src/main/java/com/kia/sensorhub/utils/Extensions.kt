package com.kia.sensorhub.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Extension functions for Context
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Extension functions for Float
 */
fun Float.format(decimals: Int = 2): String {
    return String.format("%.${decimals}f", this)
}

fun Float.toPercentage(): String {
    return "${(this * 100).format(0)}%"
}

/**
 * Extension functions for Long (timestamp)
 */
fun Long.toFormattedDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < 1000 -> "Just now"
        diff < 60000 -> "${diff / 1000}s ago"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}

/**
 * Math utilities for sensor data
 */
object SensorMath {
    
    /**
     * Calculate magnitude from 3D vector
     */
    fun calculateMagnitude(x: Float, y: Float, z: Float): Float {
        return sqrt(x * x + y * y + z * z)
    }
    
    /**
     * Calculate distance between two 3D points
     */
    fun calculateDistance3D(
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float
    ): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val dz = z2 - z1
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
    
    /**
     * Apply low-pass filter to smooth sensor data
     */
    fun lowPassFilter(current: Float, previous: Float, alpha: Float = 0.8f): Float {
        return previous + alpha * (current - previous)
    }
    
    /**
     * Apply high-pass filter to detect changes
     */
    fun highPassFilter(current: Float, previous: Float, alpha: Float = 0.8f): Float {
        return alpha * (previous + current - previous)
    }
    
    /**
     * Normalize value to 0-1 range
     */
    fun normalize(value: Float, min: Float, max: Float): Float {
        return ((value - min) / (max - min)).coerceIn(0f, 1f)
    }
    
    /**
     * Map value from one range to another
     */
    fun mapRange(
        value: Float,
        fromMin: Float, fromMax: Float,
        toMin: Float, toMax: Float
    ): Float {
        return toMin + (value - fromMin) * (toMax - toMin) / (fromMax - fromMin)
    }
    
    /**
     * Calculate average of float list
     */
    fun average(values: List<Float>): Float {
        return if (values.isEmpty()) 0f else values.sum() / values.size
    }
    
    /**
     * Calculate standard deviation
     */
    fun standardDeviation(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val mean = average(values)
        val variance = values.map { (it - mean) * (it - mean) }.sum() / values.size
        return sqrt(variance)
    }
    
    /**
     * Detect if value is anomalous (outside 2 standard deviations)
     */
    fun isAnomaly(value: Float, history: List<Float>): Boolean {
        if (history.size < 10) return false
        val mean = average(history)
        val std = standardDeviation(history)
        return abs(value - mean) > 2 * std
    }
}

/**
 * Data validation utilities
 */
object DataValidator {
    
    /**
     * Check if sensor value is valid (not NaN or Infinite)
     */
    fun isValidSensorValue(value: Float): Boolean {
        return !value.isNaN() && !value.isInfinite()
    }
    
    /**
     * Check if all values in array are valid
     */
    fun areAllValuesValid(values: FloatArray): Boolean {
        return values.all { isValidSensorValue(it) }
    }
    
    /**
     * Sanitize sensor value (replace invalid with 0)
     */
    fun sanitize(value: Float): Float {
        return if (isValidSensorValue(value)) value else 0f
    }
}

/**
 * Color utilities for data visualization
 */
object ColorUtils {
    
    /**
     * Get color based on value intensity (0-1)
     */
    fun getIntensityColor(intensity: Float): androidx.compose.ui.graphics.Color {
        val normalized = intensity.coerceIn(0f, 1f)
        return when {
            normalized < 0.33f -> androidx.compose.ui.graphics.Color.Green
            normalized < 0.66f -> androidx.compose.ui.graphics.Color.Yellow
            else -> androidx.compose.ui.graphics.Color.Red
        }
    }
    
    /**
     * Interpolate between two colors
     */
    fun interpolateColor(
        color1: androidx.compose.ui.graphics.Color,
        color2: androidx.compose.ui.graphics.Color,
        fraction: Float
    ): androidx.compose.ui.graphics.Color {
        val f = fraction.coerceIn(0f, 1f)
        return androidx.compose.ui.graphics.Color(
            red = color1.red + (color2.red - color1.red) * f,
            green = color1.green + (color2.green - color1.green) * f,
            blue = color1.blue + (color2.blue - color1.blue) * f,
            alpha = color1.alpha + (color2.alpha - color1.alpha) * f
        )
    }
}

/**
 * Performance measurement utilities
 */
class PerformanceMonitor {
    private val measurements = mutableMapOf<String, MutableList<Long>>()
    
    /**
     * Start measuring an operation
     */
    fun start(tag: String): Long {
        return System.nanoTime()
    }
    
    /**
     * End measurement and record duration
     */
    fun end(tag: String, startTime: Long) {
        val duration = System.nanoTime() - startTime
        measurements.getOrPut(tag) { mutableListOf() }.add(duration)
    }
    
    /**
     * Get average duration in milliseconds
     */
    fun getAverageDuration(tag: String): Double {
        val durations = measurements[tag] ?: return 0.0
        return durations.average() / 1_000_000.0 // Convert to ms
    }
    
    /**
     * Get statistics for a tag
     */
    fun getStats(tag: String): String {
        val durations = measurements[tag] ?: return "No measurements"
        val avg = durations.average() / 1_000_000.0
        val min = durations.minOrNull()?.div(1_000_000.0) ?: 0.0
        val max = durations.maxOrNull()?.div(1_000_000.0) ?: 0.0
        return "Avg: %.2fms, Min: %.2fms, Max: %.2fms (n=%d)".format(avg, min, max, durations.size)
    }
    
    /**
     * Clear all measurements
     */
    fun clear() {
        measurements.clear()
    }
}

/**
 * Data export utilities
 */
object DataExport {
    
    /**
     * Convert sensor readings to CSV format
     */
    fun toCSV(headers: List<String>, rows: List<List<Any>>): String {
        val sb = StringBuilder()
        
        // Headers
        sb.appendLine(headers.joinToString(","))
        
        // Rows
        rows.forEach { row ->
            sb.appendLine(row.joinToString(","))
        }
        
        return sb.toString()
    }
    
    /**
     * Convert sensor readings to JSON format
     */
    fun toJSON(data: Map<String, Any>): String {
        // Simple JSON serialization (for complex objects, use Gson or kotlinx.serialization)
        val sb = StringBuilder()
        sb.append("{\n")
        
        data.entries.forEachIndexed { index, (key, value) ->
            sb.append("  \"$key\": ")
            when (value) {
                is String -> sb.append("\"$value\"")
                is Number -> sb.append(value)
                is Boolean -> sb.append(value)
                is List<*> -> sb.append("[${value.joinToString(", ")}]")
                else -> sb.append("\"$value\"")
            }
            if (index < data.size - 1) sb.append(",")
            sb.append("\n")
        }
        
        sb.append("}")
        return sb.toString()
    }
}

/**
 * Constants used throughout the app
 */
object Constants {
    // Sensor sampling rates (in microseconds)
    const val SENSOR_DELAY_FASTEST = 0
    const val SENSOR_DELAY_GAME = 20_000
    const val SENSOR_DELAY_UI = 66_667
    const val SENSOR_DELAY_NORMAL = 200_000
    
    // Database limits
    const val MAX_READINGS_PER_SENSOR = 1000
    const val DATA_RETENTION_DAYS = 7
    
    // UI constants
    const val ANIMATION_DURATION_SHORT = 150
    const val ANIMATION_DURATION_MEDIUM = 300
    const val ANIMATION_DURATION_LONG = 500
    
    // Sensor thresholds
    const val SHAKE_THRESHOLD = 15f // m/s²
    const val ROTATION_THRESHOLD = 0.5f // rad/s
    const val PROXIMITY_NEAR_THRESHOLD = 5f // cm
    const val LIGHT_DIM_THRESHOLD = 50f // lux
    const val LIGHT_BRIGHT_THRESHOLD = 1000f // lux
}
