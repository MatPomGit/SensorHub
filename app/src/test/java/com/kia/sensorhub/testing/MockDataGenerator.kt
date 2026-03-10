package com.kia.sensorhub.testing

import com.kia.sensorhub.data.model.*
import com.kia.sensorhub.data.database.SensorReading
import kotlin.random.Random

/**
 * Mock Data Generator for Testing
 */
object MockDataGenerator {
    
    /**
     * Generate mock accelerometer data
     */
    fun generateAccelerometerData(count: Int = 10): List<AccelerometerData> {
        return List(count) {
            AccelerometerData(
                x = Random.nextFloat() * 20 - 10, // -10 to 10
                y = Random.nextFloat() * 20 - 10,
                z = Random.nextFloat() * 20 - 10,
                timestamp = System.currentTimeMillis() - (count - it) * 100L
            )
        }
    }
    
    /**
     * Generate mock gyroscope data
     */
    fun generateGyroscopeData(count: Int = 10): List<GyroscopeData> {
        return List(count) {
            GyroscopeData(
                x = Random.nextFloat() * 6 - 3, // -3 to 3
                y = Random.nextFloat() * 6 - 3,
                z = Random.nextFloat() * 6 - 3,
                timestamp = System.currentTimeMillis() - (count - it) * 100L
            )
        }
    }
    
    /**
     * Generate mock magnetometer data
     */
    fun generateMagnetometerData(count: Int = 10): List<MagnetometerData> {
        return List(count) {
            MagnetometerData(
                x = Random.nextFloat() * 100 - 50,
                y = Random.nextFloat() * 100 - 50,
                z = Random.nextFloat() * 100 - 50,
                timestamp = System.currentTimeMillis() - (count - it) * 100L
            )
        }
    }
    
    /**
     * Generate mock light sensor data
     */
    fun generateLightData(count: Int = 10): List<LightData> {
        return List(count) {
            LightData(
                illuminance = Random.nextFloat() * 10000, // 0 to 10000 lux
                timestamp = System.currentTimeMillis() - (count - it) * 1000L
            )
        }
    }
    
    /**
     * Generate mock GPS data
     */
    fun generateGpsData(count: Int = 10): List<GpsData> {
        val baseLat = 52.2297 // Warsaw
        val baseLon = 21.0122
        
        return List(count) { index ->
            GpsData(
                latitude = baseLat + (Random.nextFloat() - 0.5) * 0.01,
                longitude = baseLon + (Random.nextFloat() - 0.5) * 0.01,
                altitude = 100.0 + Random.nextDouble() * 50,
                speed = Random.nextFloat() * 10,
                accuracy = Random.nextFloat() * 20 + 5,
                bearing = Random.nextFloat() * 360,
                timestamp = System.currentTimeMillis() - (count - index) * 5000L
            )
        }
    }
    
    /**
     * Generate mock proximity data
     */
    fun generateProximityData(count: Int = 10): List<ProximityData> {
        return List(count) {
            val distance = Random.nextFloat() * 5
            ProximityData(
                distance = distance,
                isNear = distance < 3f,
                maxRange = 5f,
                timestamp = System.currentTimeMillis() - (count - it) * 500L
            )
        }
    }
    
    /**
     * Generate mock barometer data
     */
    fun generateBarometerData(count: Int = 10): List<BarometerData> {
        return List(count) {
            val pressure = 1013f + Random.nextFloat() * 20 - 10
            BarometerData(
                pressure = pressure,
                altitude = 44330f * (1f - Math.pow((pressure / 1013.25).toDouble(), (1.0 / 5.255))).toFloat(),
                timestamp = System.currentTimeMillis() - (count - it) * 1000L
            )
        }
    }
    
    /**
     * Generate mock sensor readings for database
     */
    fun generateSensorReadings(
        sensorType: String,
        count: Int = 100
    ): List<SensorReading> {
        return List(count) { index ->
            SensorReading(
                id = index.toLong(),
                sensorType = sensorType,
                x = Random.nextFloat() * 20 - 10,
                y = Random.nextFloat() * 20 - 10,
                z = Random.nextFloat() * 20 - 10,
                timestamp = System.currentTimeMillis() - (count - index) * 1000L
            )
        }
    }
    
    /**
     * Generate mixed sensor readings
     */
    fun generateMixedSensorReadings(count: Int = 100): List<SensorReading> {
        val sensorTypes = listOf(
            "ACCELEROMETER",
            "GYROSCOPE",
            "MAGNETOMETER"
        )
        
        return List(count) { index ->
            SensorReading(
                id = index.toLong(),
                sensorType = sensorTypes.random(),
                x = Random.nextFloat() * 20 - 10,
                y = Random.nextFloat() * 20 - 10,
                z = Random.nextFloat() * 20 - 10,
                timestamp = System.currentTimeMillis() - (count - index) * 500L
            )
        }
    }
    
    /**
     * Generate emotion data for testing
     */
    fun generateEmotionData(): List<Pair<String, Float>> {
        val emotions = listOf("CALM", "STRESSED", "ACTIVE", "FOCUSED", "ANXIOUS")
        return emotions.map { it to Random.nextFloat() }
    }
    
    /**
     * Generate achievement data
     */
    fun generateAchievements(count: Int = 5): List<MockAchievement> {
        val names = listOf(
            "First Steps", "Data Collector", "Sensor Master",
            "Emotion Expert", "Early Bird", "Night Owl",
            "Marathon Runner", "Consistent User", "Explorer"
        )
        
        return List(count) {
            MockAchievement(
                id = "achievement_$it",
                name = names.getOrElse(it) { "Achievement $it" },
                description = "Complete the challenge to unlock",
                xpReward = Random.nextInt(50, 500),
                isUnlocked = Random.nextBoolean(),
                progress = Random.nextInt(0, 100),
                target = 100
            )
        }
    }
    
    /**
     * Generate challenges
     */
    fun generateChallenges(count: Int = 3): List<MockChallenge> {
        val titles = listOf(
            "Morning Movement", "Compass Navigator", "Data Export",
            "Sensor Sweep", "Marathon Session"
        )
        
        return List(count) {
            MockChallenge(
                id = "challenge_$it",
                title = titles.getOrElse(it) { "Challenge $it" },
                description = "Complete the daily challenge",
                progress = Random.nextInt(0, 10),
                target = 10,
                xpReward = Random.nextInt(25, 100),
                isCompleted = Random.nextBoolean()
            )
        }
    }
}

/**
 * Mock Achievement data class
 */
data class MockAchievement(
    val id: String,
    val name: String,
    val description: String,
    val xpReward: Int,
    val isUnlocked: Boolean,
    val progress: Int,
    val target: Int
)

/**
 * Mock Challenge data class
 */
data class MockChallenge(
    val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val target: Int,
    val xpReward: Int,
    val isCompleted: Boolean
)

/**
 * Test Data Provider
 */
object TestDataProvider {
    
    /**
     * Get sample sensor reading
     */
    fun getSampleReading(sensorType: String): SensorReading {
        return SensorReading(
            id = 1,
            sensorType = sensorType,
            x = 1.5f,
            y = 2.3f,
            z = -0.8f,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Get sample readings list
     */
    fun getSampleReadings(count: Int = 10): List<SensorReading> {
        return MockDataGenerator.generateMixedSensorReadings(count)
    }
    
    /**
     * Get time-series data
     */
    fun getTimeSeriesData(
        startTime: Long,
        endTime: Long,
        intervalMs: Long
    ): List<Float> {
        val points = ((endTime - startTime) / intervalMs).toInt()
        return List(points) {
            kotlin.math.sin(it * 0.1) + Random.nextFloat() * 0.2 - 0.1
        }.map { it.toFloat() }
    }
}

/**
 * Testing Utilities
 */
object TestingUtils {
    
    /**
     * Wait for condition with timeout
     */
    suspend fun waitForCondition(
        timeoutMs: Long = 5000,
        intervalMs: Long = 100,
        condition: () -> Boolean
    ): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (condition()) return true
            kotlinx.coroutines.delay(intervalMs)
        }
        return false
    }
    
    /**
     * Measure execution time
     */
    inline fun <T> measureTime(block: () -> T): Pair<T, Long> {
        val start = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - start
        return result to duration
    }
    
    /**
     * Repeat action n times
     */
    inline fun repeat(times: Int, action: (Int) -> Unit) {
        for (i in 0 until times) {
            action(i)
        }
    }
}

/**
 * Assertion helpers
 */
object AssertionHelpers {
    
    fun assertBetween(value: Float, min: Float, max: Float, message: String = "") {
        if (value !in min..max) {
            throw AssertionError("$message Expected value between $min and $max, got $value")
        }
    }
    
    fun assertPositive(value: Float, message: String = "") {
        if (value <= 0) {
            throw AssertionError("$message Expected positive value, got $value")
        }
    }
    
    fun assertNonNull(value: Any?, message: String = "") {
        if (value == null) {
            throw AssertionError("$message Expected non-null value")
        }
    }
}
