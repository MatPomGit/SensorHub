package com.kia.sensorhub.utils

import android.util.Patterns
import java.util.regex.Pattern
import java.util.Locale

/**
 * Comprehensive Validation Utilities
 */
object ValidationUtils {

    @Volatile
    private var currentTimeProvider: () -> Long = { System.currentTimeMillis() }

    fun setCurrentTimeProvider(provider: (() -> Long)?) {
        currentTimeProvider = provider ?: { System.currentTimeMillis() }
    }

    fun resetCurrentTimeProvider() {
        currentTimeProvider = { System.currentTimeMillis() }
    }

    private fun now(): Long = currentTimeProvider()
    
    // ========== Sensor Data Validation ==========
    
    /**
     * Validate accelerometer data range (-20 to 20 m/s²)
     */
    fun isValidAccelerometerValue(value: Float): Boolean {
        return value.isFinite() && value in -20f..20f
    }
    
    /**
     * Validate gyroscope data range (-10 to 10 rad/s)
     */
    fun isValidGyroscopeValue(value: Float): Boolean {
        return value.isFinite() && value in -10f..10f
    }
    
    /**
     * Validate magnetometer data range (-200 to 200 μT)
     */
    fun isValidMagnetometerValue(value: Float): Boolean {
        return value.isFinite() && value in -200f..200f
    }
    
    /**
     * Validate light sensor data (0 to 100000 lux)
     */
    fun isValidLightValue(value: Float): Boolean {
        return value.isFinite() && value in 0f..100000f
    }
    
    /**
     * Validate GPS latitude (-90 to 90 degrees)
     */
    fun isValidLatitude(latitude: Double): Boolean {
        return latitude.isFinite() && latitude in -90.0..90.0
    }
    
    /**
     * Validate GPS longitude (-180 to 180 degrees)
     */
    fun isValidLongitude(longitude: Double): Boolean {
        return longitude.isFinite() && longitude in -180.0..180.0
    }
    
    /**
     * Validate GPS coordinates
     */
    fun isValidCoordinates(latitude: Double, longitude: Double): Boolean {
        return isValidLatitude(latitude) && isValidLongitude(longitude)
    }
    
    /**
     * Validate altitude (-500 to 10000 meters)
     */
    fun isValidAltitude(altitude: Double): Boolean {
        return altitude.isFinite() && altitude in -500.0..10000.0
    }
    
    /**
     * Validate speed (0 to 150 m/s ≈ 540 km/h)
     */
    fun isValidSpeed(speed: Float): Boolean {
        return speed.isFinite() && speed >= 0 && speed <= 150f
    }
    
    /**
     * Validate bearing (0 to 360 degrees)
     */
    fun isValidBearing(bearing: Float): Boolean {
        return bearing.isFinite() && bearing in 0f..360f
    }
    
    /**
     * Validate proximity distance (0 to maxRange)
     */
    fun isValidProximity(distance: Float, maxRange: Float): Boolean {
        return distance.isFinite() && maxRange.isFinite() && maxRange >= 0f && distance in 0f..maxRange
    }
    
    /**
     * Validate atmospheric pressure (800 to 1100 hPa)
     */
    fun isValidPressure(pressure: Float): Boolean {
        return pressure.isFinite() && pressure in 800f..1100f
    }
    
    // ========== Generic Validation ==========
    
    /**
     * Check if value is finite (not NaN or Infinity)
     */
    fun isFiniteValue(value: Float): Boolean {
        return value.isFinite()
    }
    
    /**
     * Check if value is in range (inclusive)
     */
    fun isInRange(value: Float, min: Float, max: Float): Boolean {
        return value.isFinite() && value in min..max
    }
    
    /**
     * Check if value is positive
     */
    fun isPositive(value: Float): Boolean {
        return value.isFinite() && value > 0
    }
    
    /**
     * Check if value is non-negative
     */
    fun isNonNegative(value: Float): Boolean {
        return value.isFinite() && value >= 0
    }
    
    // ========== String Validation ==========
    
    /**
     * Check if string is not null or blank
     */
    fun isNotBlank(text: String?): Boolean {
        return !text.isNullOrBlank()
    }
    
    /**
     * Check if string has minimum length
     */
    fun hasMinLength(text: String?, minLength: Int): Boolean {
        return text != null && minLength >= 0 && text.length >= minLength
    }
    
    /**
     * Check if string has maximum length
     */
    fun hasMaxLength(text: String?, maxLength: Int): Boolean {
        return text != null && maxLength >= 0 && text.length <= maxLength
    }
    
    /**
     * Check if string matches pattern
     */
    fun matchesPattern(text: String?, pattern: Pattern): Boolean {
        return text != null && pattern.matcher(text).matches()
    }
    
    /**
     * Validate email address
     */
    fun isValidEmail(email: String?): Boolean {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validate URL
     */
    fun isValidUrl(url: String?): Boolean {
        return url != null && Patterns.WEB_URL.matcher(url).matches()
    }
    
    // ========== Collection Validation ==========
    
    /**
     * Check if list is not null or empty
     */
    fun <T> isNotEmpty(list: List<T>?): Boolean {
        return !list.isNullOrEmpty()
    }
    
    /**
     * Check if list has minimum size
     */
    fun <T> hasMinSize(list: List<T>?, minSize: Int): Boolean {
        return list != null && minSize >= 0 && list.size >= minSize
    }
    
    /**
     * Check if list has maximum size
     */
    fun <T> hasMaxSize(list: List<T>?, maxSize: Int): Boolean {
        return list != null && maxSize >= 0 && list.size <= maxSize
    }
    
    /**
     * Check if all elements match predicate
     */
    fun <T> allMatch(list: List<T>?, predicate: (T) -> Boolean): Boolean {
        return list != null && list.all(predicate)
    }
    
    // ========== File Validation ==========
    
    /**
     * Validate file size (in bytes)
     */
    fun isValidFileSize(sizeBytes: Long, maxSizeBytes: Long): Boolean {
        return sizeBytes > 0 && sizeBytes <= maxSizeBytes
    }
    
    /**
     * Validate file extension
     */
    fun hasValidExtension(fileName: String, allowedExtensions: List<String>): Boolean {
        val extension = fileName.substringAfterLast('.', "")
        if (extension.isEmpty()) return false
        val normalizedAllowedExtensions = allowedExtensions
            .asSequence()
            .map { it.trim().lowercase(Locale.ROOT).removePrefix(".") }
            .filter { it.isNotEmpty() }
            .toSet()

        return extension.lowercase(Locale.ROOT) in normalizedAllowedExtensions
    }
    
    /**
     * Validate CSV file
     */
    fun isCsvFile(fileName: String): Boolean {
        return hasValidExtension(fileName, listOf("csv"))
    }
    
    /**
     * Validate JSON file
     */
    fun isJsonFile(fileName: String): Boolean {
        return hasValidExtension(fileName, listOf("json"))
    }
    
    // ========== Date/Time Validation ==========
    
    /**
     * Check if timestamp is in the past
     */
    fun isInPast(timestamp: Long): Boolean {
        return timestamp < now()
    }
    
    /**
     * Check if timestamp is in the future
     */
    fun isInFuture(timestamp: Long): Boolean {
        return timestamp > now()
    }
    
    /**
     * Check if timestamp is recent (within last N milliseconds)
     */
    fun isRecent(timestamp: Long, withinMs: Long): Boolean {
        if (withinMs < 0) return false
        val delta = now() - timestamp
        return delta in 0..withinMs
    }
    
    /**
     * Check if timestamp is within range
     */
    fun isWithinRange(timestamp: Long, startTime: Long, endTime: Long): Boolean {
        if (startTime > endTime) return false
        return timestamp in startTime..endTime
    }
    
    // ========== Permission Validation ==========
    
    /**
     * Check if permission is required for sensor
     */
    fun requiresPermission(sensorType: String): Boolean {
        return when (sensorType.uppercase()) {
            "GPS", "LOCATION" -> true
            "VOICE", "MICROPHONE" -> true
            else -> false
        }
    }
    
    // ========== Input Sanitization ==========
    
    /**
     * Sanitize sensor value (remove NaN, Infinity, clamp)
     */
    fun sanitizeSensorValue(value: Float, min: Float, max: Float): Float {
        return when {
            !value.isFinite() -> 0f
            value < min -> min
            value > max -> max
            else -> value
        }
    }
    
    /**
     * Sanitize string input
     */
    fun sanitizeString(text: String?): String {
        return text?.trim()?.replace(Regex("\\s+"), " ") ?: ""
    }
    
    /**
     * Remove special characters
     */
    fun removeSpecialCharacters(text: String): String {
        return text.replace(Regex("[^A-Za-z0-9\\s]"), "")
    }
    
    // ========== Business Logic Validation ==========
    
    /**
     * Validate XP value (0 to 1000000)
     */
    fun isValidXP(xp: Int): Boolean {
        return xp in 0..1000000
    }
    
    /**
     * Validate level (1 to 100)
     */
    fun isValidLevel(level: Int): Boolean {
        return level in 1..100
    }
    
    /**
     * Validate achievement progress (0 to 100%)
     */
    fun isValidProgress(progress: Int): Boolean {
        return progress in 0..100
    }
    
    /**
     * Validate sampling rate (1 to 1000 Hz)
     */
    fun isValidSamplingRate(rate: Int): Boolean {
        return rate in 1..1000
    }
}

/**
 * Validation result
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
    
    fun isValid(): Boolean = this is Valid
    fun isInvalid(): Boolean = this is Invalid
}

/**
 * Field validator
 */
class FieldValidator {
    private val errors = mutableListOf<String>()
    
    fun validate(condition: Boolean, errorMessage: String): FieldValidator {
        if (!condition) {
            errors.add(errorMessage)
        }
        return this
    }
    
    fun validateNotBlank(value: String?, fieldName: String): FieldValidator {
        return validate(
            ValidationUtils.isNotBlank(value),
            "$fieldName cannot be blank"
        )
    }
    
    fun validateEmail(email: String?): FieldValidator {
        return validate(
            ValidationUtils.isValidEmail(email),
            "Invalid email address"
        )
    }
    
    fun validateInRange(
        value: Float,
        min: Float,
        max: Float,
        fieldName: String
    ): FieldValidator {
        return validate(
            ValidationUtils.isInRange(value, min, max),
            "$fieldName must be between $min and $max"
        )
    }
    
    fun result(): ValidationResult {
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors.toList())
        }
    }
}

/**
 * Form validator
 */
class FormValidator {
    private val fields = mutableMapOf<String, FieldValidator>()
    
    fun field(name: String, validator: FieldValidator.() -> Unit): FormValidator {
        fields[name] = FieldValidator().apply(validator)
        return this
    }
    
    fun validate(): ValidationResult {
        val allErrors = fields.values
            .mapNotNull { validator ->
                val result = validator.result()
                if (result is ValidationResult.Invalid) result.errors else null
            }
            .flatten()
        
        return if (allErrors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(allErrors)
        }
    }
}

/**
 * Validator extension functions
 */
fun String?.isValidEmail(): Boolean = ValidationUtils.isValidEmail(this)
fun String?.isValidUrl(): Boolean = ValidationUtils.isValidUrl(this)
fun Float.isFiniteValue(): Boolean = ValidationUtils.isFiniteValue(this)
fun Float.isInRange(min: Float, max: Float): Boolean = ValidationUtils.isInRange(this, min, max)
