package com.kia.sensorhub.utils

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

/**
 * Accessibility Helper
 * Utilities for improving app accessibility
 */
object AccessibilityHelper {
    
    /**
     * Check if TalkBack or other screen readers are enabled
     */
    fun isScreenReaderEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.isEnabled && am.isTouchExplorationEnabled
    }
    
    /**
     * Check if any accessibility service is enabled
     */
    fun isAccessibilityEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.isEnabled
    }
    
    /**
     * Get recommended timeout for accessibility
     * Returns longer timeout if accessibility services are enabled
     */
    fun getAccessibilityTimeout(context: Context, defaultMs: Long = 5000): Long {
        return if (isScreenReaderEnabled(context)) {
            defaultMs * 2 // Double timeout for screen readers
        } else {
            defaultMs
        }
    }
    
    /**
     * Format sensor value for screen readers
     */
    fun formatSensorValueForScreenReader(
        label: String,
        value: Float,
        unit: String
    ): String {
        val roundedValue = String.format("%.2f", value)
        return "$label: $roundedValue $unit"
    }
    
    /**
     * Format coordinate for screen readers
     */
    fun formatCoordinateForScreenReader(
        type: String,
        value: Double,
        direction: String
    ): String {
        val degrees = kotlin.math.abs(value)
        return "$type: ${String.format("%.6f", degrees)} degrees $direction"
    }
    
    /**
     * Announce message to screen reader
     */
    fun announceForAccessibility(context: Context, message: String) {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (am.isEnabled) {
            // TODO: Implement actual announcement
            // This would typically use AccessibilityEvent
        }
    }
}

/**
 * Accessibility content descriptions
 */
object ContentDescriptions {
    
    // Sensor descriptions
    const val ACCELEROMETER = "Accelerometer sensor measuring device motion in meters per second squared"
    const val GYROSCOPE = "Gyroscope sensor measuring device rotation in radians per second"
    const val MAGNETOMETER = "Magnetometer sensor measuring magnetic field strength in microteslas"
    const val LIGHT_SENSOR = "Light sensor measuring ambient light in lux"
    const val GPS_SENSOR = "GPS sensor providing location coordinates"
    const val PROXIMITY_SENSOR = "Proximity sensor detecting nearby objects"
    const val BAROMETER = "Barometer sensor measuring atmospheric pressure in hectopascals"
    
    // Button descriptions
    const val START_MONITORING = "Start sensor monitoring"
    const val STOP_MONITORING = "Stop sensor monitoring"
    const val SAVE_READING = "Save current sensor reading"
    const val EXPORT_DATA = "Export sensor data"
    const val OPEN_SETTINGS = "Open settings"
    const val OPEN_INFO = "View sensor information"
    
    // Status descriptions
    const val SENSOR_ACTIVE = "Sensor is currently active and collecting data"
    const val SENSOR_INACTIVE = "Sensor is not active"
    const val SENSOR_UNAVAILABLE = "This sensor is not available on your device"
    const val PERMISSION_REQUIRED = "Permission required to use this sensor"
    
    // Value descriptions
    fun sensorValue(label: String, value: Float, unit: String): String {
        return "$label is ${String.format("%.2f", value)} $unit"
    }
    
    fun achievement(name: String, xp: Int): String {
        return "Achievement unlocked: $name, earned $xp experience points"
    }
    
    fun progress(current: Int, total: Int, label: String): String {
        val percentage = (current.toFloat() / total * 100).toInt()
        return "$label: $current out of $total, $percentage percent complete"
    }
}

/**
 * Compose accessibility modifiers
 */
object AccessibilityModifiers {
    
    /**
     * Custom semantics for sensor values
     */
    val SensorValueKey = SemanticsPropertyKey<String>("SensorValue")
    
    fun SemanticsPropertyReceiver.sensorValue(value: String) {
        this[SensorValueKey] = value
    }
}

/**
 * Accessibility announcements
 */
sealed class AccessibilityAnnouncement {
    data class SensorStarted(val sensorType: String) : AccessibilityAnnouncement() {
        override fun getMessage() = "$sensorType monitoring started"
    }
    
    data class SensorStopped(val sensorType: String) : AccessibilityAnnouncement() {
        override fun getMessage() = "$sensorType monitoring stopped"
    }
    
    data class DataSaved(val count: Int) : AccessibilityAnnouncement() {
        override fun getMessage() = "$count readings saved"
    }
    
    data class AchievementUnlocked(val name: String, val xp: Int) : AccessibilityAnnouncement() {
        override fun getMessage() = "Achievement unlocked: $name, earned $xp XP"
    }
    
    data class ErrorOccurred(val message: String) : AccessibilityAnnouncement() {
        override fun getMessage() = "Error: $message"
    }
    
    abstract fun getMessage(): String
}

/**
 * Screen reader text builder
 */
class ScreenReaderTextBuilder {
    private val parts = mutableListOf<String>()
    
    fun addLabel(label: String): ScreenReaderTextBuilder {
        parts.add(label)
        return this
    }
    
    fun addValue(value: String): ScreenReaderTextBuilder {
        parts.add(value)
        return this
    }
    
    fun addUnit(unit: String): ScreenReaderTextBuilder {
        parts.add(unit)
        return this
    }
    
    fun addStatus(status: String): ScreenReaderTextBuilder {
        parts.add(status)
        return this
    }
    
    fun build(): String {
        return parts.joinToString(separator = ", ")
    }
    
    companion object {
        fun create() = ScreenReaderTextBuilder()
    }
}

/**
 * Haptic feedback helper
 */
object HapticFeedbackHelper {
    
    /**
     * Provide haptic feedback for accessibility
     */
    fun provideHapticFeedback(
        context: Context,
        feedbackType: HapticFeedbackType
    ) {
        // Only provide haptic if accessibility enabled
        if (AccessibilityHelper.isAccessibilityEnabled(context)) {
            // TODO: Implement actual haptic feedback
            // Would use Vibrator service
        }
    }
    
    enum class HapticFeedbackType {
        CLICK,
        LONG_PRESS,
        SUCCESS,
        ERROR,
        WARNING
    }
}

/**
 * Font scale helper
 */
object FontScaleHelper {
    
    /**
     * Get current font scale
     */
    fun getFontScale(context: Context): Float {
        return context.resources.configuration.fontScale
    }
    
    /**
     * Check if large text is enabled
     */
    fun isLargeTextEnabled(context: Context): Boolean {
        return getFontScale(context) >= 1.3f
    }
    
    /**
     * Get scaled text size
     */
    fun getScaledTextSize(baseSize: Float, context: Context): Float {
        return baseSize * getFontScale(context)
    }
}

/**
 * Color contrast helper
 */
object ColorContrastHelper {
    
    /**
     * Calculate relative luminance
     */
    private fun relativeLuminance(color: Int): Double {
        val r = android.graphics.Color.red(color) / 255.0
        val g = android.graphics.Color.green(color) / 255.0
        val b = android.graphics.Color.blue(color) / 255.0
        
        val rsRGB = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
        val gsRGB = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
        val bsRGB = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)
        
        return 0.2126 * rsRGB + 0.7152 * gsRGB + 0.0722 * bsRGB
    }
    
    /**
     * Calculate contrast ratio between two colors
     */
    fun contrastRatio(foreground: Int, background: Int): Double {
        val l1 = relativeLuminance(foreground)
        val l2 = relativeLuminance(background)
        
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        
        return (lighter + 0.05) / (darker + 0.05)
    }
    
    /**
     * Check if contrast meets WCAG AA standard (4.5:1 for normal text)
     */
    fun meetsWCAGAA(foreground: Int, background: Int): Boolean {
        return contrastRatio(foreground, background) >= 4.5
    }
    
    /**
     * Check if contrast meets WCAG AAA standard (7:1 for normal text)
     */
    fun meetsWCAGAAA(foreground: Int, background: Int): Boolean {
        return contrastRatio(foreground, background) >= 7.0
    }
}

/**
 * Touch target helper
 */
object TouchTargetHelper {
    
    const val MIN_TOUCH_TARGET_DP = 48
    const val RECOMMENDED_TOUCH_TARGET_DP = 56
    
    /**
     * Convert DP to pixels
     */
    fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
    
    /**
     * Check if touch target is large enough
     */
    fun isLargeEnough(sizePx: Int, context: Context): Boolean {
        val minSizePx = dpToPx(MIN_TOUCH_TARGET_DP, context)
        return sizePx >= minSizePx
    }
    
    /**
     * Get recommended touch target size in pixels
     */
    fun getRecommendedSizePx(context: Context): Int {
        return dpToPx(RECOMMENDED_TOUCH_TARGET_DP, context)
    }
}
