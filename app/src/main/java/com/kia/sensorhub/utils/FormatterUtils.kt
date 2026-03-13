package com.kia.sensorhub.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Formatter Utilities
 * Comprehensive formatting functions for app data
 */
object FormatterUtils {
    
    // ========== Number Formatting ==========
    
    /**
     * Format float to specified decimal places
     */
    fun formatFloat(value: Float, decimalPlaces: Int = 2): String {
        val pattern = "0.${"0".repeat(decimalPlaces)}"
        return DecimalFormat(pattern).format(value)
    }
    
    /**
     * Format sensor value with unit
     */
    fun formatSensorValue(value: Float, unit: String, decimalPlaces: Int = 2): String {
        return "${formatFloat(value, decimalPlaces)} $unit"
    }
    
    /**
     * Format large numbers with K/M/B suffixes
     */
    fun formatLargeNumber(value: Long): String {
        return when {
            value >= 1_000_000_000 -> "${value / 1_000_000_000}B"
            value >= 1_000_000 -> "${value / 1_000_000}M"
            value >= 1_000 -> "${value / 1_000}K"
            else -> value.toString()
        }
    }
    
    /**
     * Format percentage
     */
    fun formatPercentage(value: Float, decimalPlaces: Int = 1): String {
        return "${formatFloat(value, decimalPlaces)}%"
    }
    
    // ========== Time Formatting ==========
    
    /**
     * Format timestamp to readable string
     */
    fun formatTimestamp(
        timestamp: Long,
        pattern: String = "yyyy-MM-dd HH:mm:ss"
    ): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Format duration in milliseconds
     */
    fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%d:%02d", minutes, seconds)
            else -> String.format("0:%02d", seconds)
        }
    }
    
    /**
     * Format time ago (e.g., "2 hours ago")
     */
    fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            else -> formatTimestamp(timestamp, "MMM dd, yyyy")
        }
    }
    
    // ========== Size Formatting ==========
    
    /**
     * Format file size in bytes to human readable
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "${bytes / 1_073_741_824} GB"
            bytes >= 1_048_576 -> "${bytes / 1_048_576} MB"
            bytes >= 1_024 -> "${bytes / 1_024} KB"
            else -> "$bytes B"
        }
    }
    
    /**
     * Format file size with decimal precision
     */
    fun formatFileSizePrecise(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> String.format("%.2f GB", bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> String.format("%.2f MB", bytes / 1_048_576.0)
            bytes >= 1_024 -> String.format("%.2f KB", bytes / 1_024.0)
            else -> "$bytes B"
        }
    }
    
    // ========== Coordinate Formatting ==========
    
    /**
     * Format GPS coordinate with direction
     */
    fun formatCoordinate(coordinate: Double, isLatitude: Boolean): String {
        val absValue = abs(coordinate)
        val degrees = absValue.toInt()
        val minutesFloat = (absValue - degrees) * 60
        val minutes = minutesFloat.toInt()
        val seconds = (minutesFloat - minutes) * 60
        
        val direction = if (isLatitude) {
            if (coordinate >= 0) "N" else "S"
        } else {
            if (coordinate >= 0) "E" else "W"
        }
        
        return String.format("%d°%d'%.1f\"%s", degrees, minutes, seconds, direction)
    }
    
    /**
     * Format coordinate as decimal degrees
     */
    fun formatCoordinateDecimal(coordinate: Double, isLatitude: Boolean): String {
        val direction = if (isLatitude) {
            if (coordinate >= 0) "N" else "S"
        } else {
            if (coordinate >= 0) "E" else "W"
        }
        return String.format("%.6f° %s", abs(coordinate), direction)
    }
    
    // ========== Speed Formatting ==========
    
    /**
     * Format speed from m/s to km/h
     */
    fun formatSpeed(metersPerSecond: Float): String {
        val kmh = metersPerSecond * 3.6f
        return String.format("%.1f km/h", kmh)
    }
    
    /**
     * Format speed with unit selection
     */
    fun formatSpeed(metersPerSecond: Float, unit: SpeedUnit): String {
        return when (unit) {
            SpeedUnit.METERS_PER_SECOND -> String.format("%.2f m/s", metersPerSecond)
            SpeedUnit.KILOMETERS_PER_HOUR -> String.format("%.1f km/h", metersPerSecond * 3.6f)
            SpeedUnit.MILES_PER_HOUR -> String.format("%.1f mph", metersPerSecond * 2.237f)
        }
    }
    
    enum class SpeedUnit {
        METERS_PER_SECOND,
        KILOMETERS_PER_HOUR,
        MILES_PER_HOUR
    }
    
    // ========== Distance Formatting ==========
    
    /**
     * Format distance in meters
     */
    fun formatDistance(meters: Float): String {
        return when {
            meters >= 1000 -> String.format("%.2f km", meters / 1000)
            else -> String.format("%.0f m", meters)
        }
    }
    
    /**
     * Format altitude
     */
    fun formatAltitude(meters: Double): String {
        return when {
            abs(meters) >= 1000 -> String.format("%.1f km", meters / 1000)
            else -> String.format("%.0f m", meters)
        }
    }
    
    // ========== Angle Formatting ==========
    
    /**
     * Format bearing/heading
     */
    fun formatBearing(degrees: Float): String {
        return String.format("%.0f°", degrees)
    }
    
    /**
     * Get cardinal direction from bearing
     */
    fun getCardinalDirection(bearing: Float): String {
        return when {
            bearing < 22.5 || bearing >= 337.5 -> "N"
            bearing < 67.5 -> "NE"
            bearing < 112.5 -> "E"
            bearing < 157.5 -> "SE"
            bearing < 202.5 -> "S"
            bearing < 247.5 -> "SW"
            bearing < 292.5 -> "W"
            else -> "NW"
        }
    }
    
    // ========== Sensor Specific Formatting ==========
    
    /**
     * Format accelerometer value
     */
    fun formatAcceleration(value: Float): String {
        return formatSensorValue(value, "m/s²", 2)
    }
    
    /**
     * Format gyroscope value
     */
    fun formatRotationRate(value: Float): String {
        return formatSensorValue(value, "rad/s", 3)
    }
    
    /**
     * Format magnetic field
     */
    fun formatMagneticField(value: Float): String {
        return formatSensorValue(value, "μT", 2)
    }
    
    /**
     * Format illuminance
     */
    fun formatIlluminance(value: Float): String {
        return formatSensorValue(value, "lux", 1)
    }
    
    /**
     * Format pressure
     */
    fun formatPressure(value: Float): String {
        return formatSensorValue(value, "hPa", 2)
    }
    
    /**
     * Format proximity
     */
    fun formatProximity(value: Float): String {
        return formatSensorValue(value, "cm", 1)
    }
    
    // ========== Statistical Formatting ==========
    
    /**
     * Format range
     */
    fun formatRange(min: Float, max: Float, unit: String): String {
        return "${formatFloat(min)} - ${formatFloat(max)} $unit"
    }
    
    /**
     * Format average with count
     */
    fun formatAverage(average: Float, count: Int, unit: String): String {
        return "${formatFloat(average)} $unit (n=$count)"
    }
    
    // ========== List Formatting ==========
    
    /**
     * Format list with "and" conjunction
     */
    fun formatList(items: List<String>): String {
        return when (items.size) {
            0 -> ""
            1 -> items[0]
            2 -> "${items[0]} and ${items[1]}"
            else -> {
                val allButLast = items.dropLast(1).joinToString(", ")
                "$allButLast, and ${items.last()}"
            }
        }
    }
    
    /**
     * Format count with noun
     */
    fun formatCount(count: Int, singular: String, plural: String? = null): String {
        val pluralForm = plural ?: "${singular}s"
        return if (count == 1) {
            "$count $singular"
        } else {
            "$count $pluralForm"
        }
    }
}

/**
 * Text formatter for UI display
 */
object TextFormatter {
    
    /**
     * Capitalize first letter
     */
    fun capitalize(text: String): String {
        return text.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
    
    /**
     * Convert to title case
     */
    fun toTitleCase(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            capitalize(word)
        }
    }
    
    /**
     * Truncate text with ellipsis
     */
    fun truncate(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) {
            text
        } else {
            "${text.substring(0, maxLength - 3)}..."
        }
    }
    
    /**
     * Format camelCase to Title Case
     */
    fun camelToTitle(camelCase: String): String {
        return camelCase
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { it.uppercase() }
    }
}
