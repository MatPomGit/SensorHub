package com.kia.sensorhub

import com.kia.sensorhub.data.model.AccelerometerData
import com.kia.sensorhub.testing.MockDataGenerator
import com.kia.sensorhub.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Extended Unit Tests for SensorHub
 */
class ExtendedUnitTests {
    
    @Before
    fun setup() {
        // Setup before each test
    }
    
    // ========== Data Validator Tests ==========
    
    @Test
    fun `validateAccelerometerData with valid values returns true`() {
        val result = SensorDataValidator.validateAccelerometerData(5f, -3f, 9.8f)
        assertTrue(result)
    }
    
    @Test
    fun `validateAccelerometerData with out of range values returns false`() {
        val result = SensorDataValidator.validateAccelerometerData(25f, 0f, 0f)
        assertFalse(result)
    }
    
    @Test
    fun `validateAccelerometerData with NaN returns false`() {
        val result = SensorDataValidator.validateAccelerometerData(Float.NaN, 0f, 0f)
        assertFalse(result)
    }
    
    @Test
    fun `validateGyroscopeData with valid values returns true`() {
        val result = SensorDataValidator.validateGyroscopeData(2f, -1.5f, 0.8f)
        assertTrue(result)
    }
    
    @Test
    fun `validateLightData with valid value returns true`() {
        val result = SensorDataValidator.validateLightData(1000f)
        assertTrue(result)
    }
    
    @Test
    fun `validateLightData with negative value returns false`() {
        val result = SensorDataValidator.validateLightData(-10f)
        assertFalse(result)
    }
    
    @Test
    fun `validateGpsData with valid coordinates returns true`() {
        val result = SensorDataValidator.validateGpsData(52.2297, 21.0122)
        assertTrue(result)
    }
    
    @Test
    fun `validateGpsData with invalid latitude returns false`() {
        val result = SensorDataValidator.validateGpsData(95.0, 21.0)
        assertFalse(result)
    }
    
    // ========== Data Sanitizer Tests ==========
    
    @Test
    fun `sanitizeFloat removes NaN`() {
        val result = DataSanitizer.sanitizeFloat(Float.NaN)
        assertEquals(0f, result)
    }
    
    @Test
    fun `sanitizeFloat clamps to max`() {
        val result = DataSanitizer.sanitizeFloat(2000f, max = 100f)
        assertEquals(100f, result)
    }
    
    @Test
    fun `sanitizeFloat clamps to min`() {
        val result = DataSanitizer.sanitizeFloat(-2000f, min = -100f)
        assertEquals(-100f, result)
    }
    
    @Test
    fun `sanitizeFloat handles infinity`() {
        val result = DataSanitizer.sanitizeFloat(Float.POSITIVE_INFINITY, max = 1000f)
        assertEquals(1000f, result)
    }
    
    @Test
    fun `removeOutliers filters extreme values`() {
        val data = listOf(1f, 2f, 3f, 100f, 4f, 5f, -100f, 6f)
        val result = DataSanitizer.removeOutliers(data)
        
        assertFalse(100f in result)
        assertFalse(-100f in result)
        assertTrue(3f in result)
    }
    
    // ========== Mock Data Generator Tests ==========
    
    @Test
    fun `generateAccelerometerData creates correct count`() {
        val data = MockDataGenerator.generateAccelerometerData(50)
        assertEquals(50, data.size)
    }
    
    @Test
    fun `generateAccelerometerData values in valid range`() {
        val data = MockDataGenerator.generateAccelerometerData(100)
        
        data.forEach { reading ->
            assertTrue(reading.x in -20f..20f)
            assertTrue(reading.y in -20f..20f)
            assertTrue(reading.z in -20f..20f)
        }
    }
    
    @Test
    fun `generateLightData creates valid lux values`() {
        val data = MockDataGenerator.generateLightData(50)
        
        data.forEach { reading ->
            assertTrue(reading.illuminance >= 0f)
            assertTrue(reading.illuminance <= 10000f)
        }
    }
    
    @Test
    fun `generateGpsData creates valid coordinates`() {
        val data = MockDataGenerator.generateGpsData(20)
        
        data.forEach { reading ->
            assertTrue(reading.latitude in -90.0..90.0)
            assertTrue(reading.longitude in -180.0..180.0)
            assertTrue(reading.accuracy > 0)
        }
    }
    
    @Test
    fun `generateSensorReadings has correct sensor type`() {
        val data = MockDataGenerator.generateSensorReadings("ACCELEROMETER", 30)
        
        assertEquals(30, data.size)
        data.forEach { reading ->
            assertEquals("ACCELEROMETER", reading.sensorType)
        }
    }
    
    // ========== Permission Helper Tests ==========
    
    @Test
    fun `isPermissionRequired returns true for GPS`() {
        val result = PermissionHelper.isPermissionRequired("GPS")
        assertTrue(result)
    }
    
    @Test
    fun `isPermissionRequired returns false for accelerometer`() {
        val result = PermissionHelper.isPermissionRequired("accelerometer")
        assertFalse(result)
    }
    
    @Test
    fun `getRequiredPermissions for GPS returns location permissions`() {
        val permissions = PermissionHelper.getRequiredPermissions("GPS")
        
        assertTrue(permissions.isNotEmpty())
        assertTrue(permissions.any { it.contains("LOCATION") })
    }
    
    // ========== Error Handler Tests ==========
    
    @Test
    fun `getUserFriendlyMessage for SecurityException`() {
        val exception = SecurityException("Permission denied")
        val message = ErrorHandler.getUserFriendlyMessage(exception)
        
        assertTrue(message.contains("Permission"))
    }
    
    @Test
    fun `getUserFriendlyMessage for IllegalStateException`() {
        val exception = IllegalStateException("Sensor not available")
        val message = ErrorHandler.getUserFriendlyMessage(exception)
        
        assertTrue(message.contains("sensor") || message.contains("available"))
    }
    
    // ========== Utility Function Tests ==========
    
    @Test
    fun `calculateMagnitude returns correct value`() {
        val magnitude = DataValidator.calculateMagnitude(3f, 4f, 0f)
        assertEquals(5f, magnitude, 0.01f)
    }
    
    @Test
    fun `isValidSensorValue detects NaN`() {
        assertFalse(DataValidator.isValidSensorValue(Float.NaN))
    }
    
    @Test
    fun `isValidSensorValue detects Infinity`() {
        assertFalse(DataValidator.isValidSensorValue(Float.POSITIVE_INFINITY))
        assertFalse(DataValidator.isValidSensorValue(Float.NEGATIVE_INFINITY))
    }
    
    @Test
    fun `isValidSensorValue accepts normal values`() {
        assertTrue(DataValidator.isValidSensorValue(0f))
        assertTrue(DataValidator.isValidSensorValue(100f))
        assertTrue(DataValidator.isValidSensorValue(-50f))
    }
    
    // ========== Coroutine Tests ==========
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `tryCatch handles exceptions`() = runTest {
        var errorCaught = false
        
        val result = tryCatch(
            onError = { errorCaught = true }
        ) {
            throw IllegalStateException("Test error")
        }
        
        assertNull(result)
        assertTrue(errorCaught)
    }
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `tryCatch returns success value`() = runTest {
        val result = tryCatch {
            "Success"
        }
        
        assertEquals("Success", result)
    }
    
    // ========== Debouncer Tests ==========
    
    @Test
    fun `Debouncer allows first action`() {
        val debouncer = Debouncer(100)
        var actionCount = 0
        
        debouncer.debounce { actionCount++ }
        
        assertEquals(1, actionCount)
    }
    
    @Test
    fun `Debouncer blocks rapid actions`() {
        val debouncer = Debouncer(100)
        var actionCount = 0
        
        repeat(10) {
            debouncer.debounce { actionCount++ }
        }
        
        assertEquals(1, actionCount)
    }
    
    // ========== Data Model Tests ==========
    
    @Test
    fun `AccelerometerData has correct default values`() {
        val data = AccelerometerData()
        
        assertEquals(0f, data.x)
        assertEquals(0f, data.y)
        assertEquals(0f, data.z)
        assertTrue(data.timestamp > 0)
    }
    
    @Test
    fun `SensorType enum fromString works`() {
        val type = SensorType.fromString("ACCELEROMETER")
        assertEquals(SensorType.ACCELEROMETER, type)
    }
    
    @Test
    fun `SensorType enum fromString handles unknown`() {
        val type = SensorType.fromString("INVALID_SENSOR")
        assertEquals(SensorType.UNKNOWN, type)
    }
    
    // ========== Statistical Tests ==========
    
    @Test
    fun `average calculation is correct`() {
        val values = listOf(1f, 2f, 3f, 4f, 5f)
        val avg = values.average()
        
        assertEquals(3.0, avg, 0.001)
    }
    
    @Test
    fun `standard deviation calculation`() {
        val values = listOf(2f, 4f, 4f, 4f, 5f, 5f, 7f, 9f)
        val avg = values.average().toFloat()
        val variance = values.map { (it - avg) * (it - avg) }.average().toFloat()
        val stdDev = kotlin.math.sqrt(variance)
        
        assertTrue(stdDev > 1.5f)
        assertTrue(stdDev < 2.5f)
    }
    
    // ========== Edge Case Tests ==========
    
    @Test
    fun `empty list handling`() {
        val result = DataSanitizer.removeOutliers(emptyList())
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `single element list handling`() {
        val result = DataSanitizer.removeOutliers(listOf(5f))
        assertEquals(1, result.size)
        assertEquals(5f, result[0])
    }
    
    @Test
    fun `null handling in tryOrNull`() {
        val result = tryOrNull {
            null as String?
        }
        
        assertNull(result)
    }

    @Test
    fun `isRecent uses injected time provider with boundary values`() {
        ValidationUtils.setCurrentTimeProvider { 10_000L }

        assertTrue(ValidationUtils.isRecent(timestamp = 10_000L, withinMs = 0L))
        assertTrue(ValidationUtils.isRecent(timestamp = 9_900L, withinMs = 100L))
        assertFalse(ValidationUtils.isRecent(timestamp = 9_899L, withinMs = 100L))

        ValidationUtils.resetCurrentTimeProvider()
    }

    @Test
    fun `isRecent returns false for negative window`() {
        ValidationUtils.setCurrentTimeProvider { 5_000L }

        assertFalse(ValidationUtils.isRecent(timestamp = 5_000L, withinMs = -1L))

        ValidationUtils.resetCurrentTimeProvider()
    }

    @Test
    fun `hasValidExtension normalizes case and dot-prefixed allowed extensions`() {
        assertTrue(ValidationUtils.hasValidExtension("report.CSV", listOf(".csv")))
        assertFalse(ValidationUtils.hasValidExtension("report", listOf("csv")))
    }

    @Test
    fun `isWithinRange rejects reversed boundaries`() {
        assertFalse(ValidationUtils.isWithinRange(timestamp = 5L, startTime = 10L, endTime = 0L))
    }

}
