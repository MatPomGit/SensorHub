package com.kia.sensorhub.affective

import com.kia.sensorhub.data.model.*
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Affective Computing Module
 * Analyzes user emotional state based on sensor data
 */

/**
 * Emotion types detected by the system
 */
enum class EmotionType {
    CALM,
    STRESSED,
    ACTIVE,
    RESTING,
    ANXIOUS,
    FOCUSED,
    DISTRACTED,
    UNKNOWN
}

/**
 * Detected emotion with confidence level
 */
data class DetectedEmotion(
    val emotion: EmotionType,
    val confidence: Float, // 0.0 to 1.0
    val timestamp: Long = System.currentTimeMillis(),
    val factors: Map<String, Float> = emptyMap() // Contributing factors
)

/**
 * Affective state representing overall emotional condition
 */
data class AffectiveState(
    val arousal: Float, // 0.0 (calm) to 1.0 (excited)
    val valence: Float, // 0.0 (negative) to 1.0 (positive)
    val stress: Float, // 0.0 (relaxed) to 1.0 (stressed)
    val focus: Float, // 0.0 (distracted) to 1.0 (focused)
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Main Affective Analyzer
 */
class AffectiveAnalyzer {
    
    private val accelerometerHistory = ArrayDeque<AccelerometerData>(HISTORY_SIZE)
    private val gyroscopeHistory = ArrayDeque<GyroscopeData>(HISTORY_SIZE)
    private val touchEventHistory = ArrayDeque<TouchEvent>(HISTORY_SIZE)
    
    companion object {
        private const val HISTORY_SIZE = 100
        private const val MOVEMENT_THRESHOLD_CALM = 2.0f
        private const val MOVEMENT_THRESHOLD_ACTIVE = 8.0f
        private const val ROTATION_THRESHOLD_STABLE = 0.1f
    }
    
    /**
     * Analyze emotion from accelerometer data
     */
    fun analyzeFromAccelerometer(data: AccelerometerData): DetectedEmotion {
        addWithBound(accelerometerHistory, data, HISTORY_SIZE)
        
        if (accelerometerHistory.size < 20) {
            return DetectedEmotion(EmotionType.UNKNOWN, 0.0f)
        }
        
        val movementIntensity = calculateMovementIntensity(accelerometerHistory)
        val movementVariability = calculateVariability(
            accelerometerHistory.asSequence().map { it.magnitude }
        )
        
        // Determine emotion based on movement patterns
        return when {
            movementIntensity < MOVEMENT_THRESHOLD_CALM && movementVariability < 0.5f -> {
                DetectedEmotion(
                    emotion = EmotionType.CALM,
                    confidence = 0.8f,
                    factors = mapOf(
                        "movement_intensity" to movementIntensity,
                        "variability" to movementVariability
                    )
                )
            }
            movementIntensity > MOVEMENT_THRESHOLD_ACTIVE && movementVariability > 2.0f -> {
                DetectedEmotion(
                    emotion = EmotionType.STRESSED,
                    confidence = 0.7f,
                    factors = mapOf(
                        "movement_intensity" to movementIntensity,
                        "variability" to movementVariability
                    )
                )
            }
            movementIntensity > MOVEMENT_THRESHOLD_ACTIVE && movementVariability < 1.0f -> {
                DetectedEmotion(
                    emotion = EmotionType.ACTIVE,
                    confidence = 0.75f,
                    factors = mapOf(
                        "movement_intensity" to movementIntensity,
                        "variability" to movementVariability
                    )
                )
            }
            else -> {
                DetectedEmotion(
                    emotion = EmotionType.RESTING,
                    confidence = 0.6f,
                    factors = mapOf(
                        "movement_intensity" to movementIntensity,
                        "variability" to movementVariability
                    )
                )
            }
        }
    }
    
    /**
     * Analyze emotion from gyroscope data
     */
    fun analyzeFromGyroscope(data: GyroscopeData): DetectedEmotion {
        addWithBound(gyroscopeHistory, data, HISTORY_SIZE)
        
        if (gyroscopeHistory.size < 20) {
            return DetectedEmotion(EmotionType.UNKNOWN, 0.0f)
        }
        
        val rotationStability = calculateRotationStability(gyroscopeHistory)
        val lastSamples = gyroscopeHistory.takeLast(10)
        val rotationIntensity = if (lastSamples.isNotEmpty()) {
            lastSamples.asSequence().map { it.rotationRate }.average().toFloat()
        } else {
            0f
        }
        
        return when {
            rotationStability > 0.8f && rotationIntensity < ROTATION_THRESHOLD_STABLE -> {
                DetectedEmotion(
                    emotion = EmotionType.FOCUSED,
                    confidence = 0.75f,
                    factors = mapOf(
                        "stability" to rotationStability,
                        "intensity" to rotationIntensity
                    )
                )
            }
            rotationStability < 0.4f -> {
                DetectedEmotion(
                    emotion = EmotionType.DISTRACTED,
                    confidence = 0.7f,
                    factors = mapOf(
                        "stability" to rotationStability,
                        "intensity" to rotationIntensity
                    )
                )
            }
            else -> {
                DetectedEmotion(
                    emotion = EmotionType.RESTING,
                    confidence = 0.5f,
                    factors = mapOf(
                        "stability" to rotationStability,
                        "intensity" to rotationIntensity
                    )
                )
            }
        }
    }
    
    /**
     * Analyze touch interaction patterns
     */
    fun analyzeFromTouchEvents(events: List<TouchEvent>): DetectedEmotion {
        events.forEach { addWithBound(touchEventHistory, it, HISTORY_SIZE) }

        if (touchEventHistory.size < 10) {
            return DetectedEmotion(EmotionType.UNKNOWN, 0.0f)
        }

        var totalDuration = 0f
        var totalPressure = 0f
        touchEventHistory.forEach {
            totalDuration += it.duration
            totalPressure += it.pressure
        }

        val avgDuration = totalDuration / touchEventHistory.size
        val avgPressure = totalPressure / touchEventHistory.size
        val frequency = touchEventHistory.size.toFloat() / 60f // per minute
        
        return when {
            avgPressure > 0.7f && frequency > 30f -> {
                DetectedEmotion(
                    emotion = EmotionType.STRESSED,
                    confidence = 0.7f,
                    factors = mapOf(
                        "pressure" to avgPressure,
                        "frequency" to frequency,
                        "duration" to avgDuration
                    )
                )
            }
            avgPressure < 0.3f && avgDuration > 500f -> {
                DetectedEmotion(
                    emotion = EmotionType.CALM,
                    confidence = 0.65f,
                    factors = mapOf(
                        "pressure" to avgPressure,
                        "frequency" to frequency,
                        "duration" to avgDuration
                    )
                )
            }
            frequency > 40f && avgDuration < 200f -> {
                DetectedEmotion(
                    emotion = EmotionType.ANXIOUS,
                    confidence = 0.6f,
                    factors = mapOf(
                        "pressure" to avgPressure,
                        "frequency" to frequency,
                        "duration" to avgDuration
                    )
                )
            }
            else -> {
                DetectedEmotion(
                    emotion = EmotionType.RESTING,
                    confidence = 0.5f,
                    factors = mapOf(
                        "pressure" to avgPressure,
                        "frequency" to frequency,
                        "duration" to avgDuration
                    )
                )
            }
        }
    }
    
    /**
     * Combine multiple emotions into an affective state
     */
    fun computeAffectiveState(emotions: List<DetectedEmotion>): AffectiveState {
        if (emotions.isEmpty()) {
            return AffectiveState(
                arousal = 0.5f,
                valence = 0.5f,
                stress = 0.5f,
                focus = 0.5f
            )
        }
        
        // Map emotions to dimensional values
        var totalArousal = 0f
        var totalValence = 0f
        var totalStress = 0f
        var totalFocus = 0f
        var totalWeight = 0f
        
        emotions.forEach { emotion ->
            val weight = emotion.confidence
            totalWeight += weight
            
            when (emotion.emotion) {
                EmotionType.CALM -> {
                    totalArousal += 0.2f * weight
                    totalValence += 0.7f * weight
                    totalStress += 0.1f * weight
                    totalFocus += 0.6f * weight
                }
                EmotionType.STRESSED -> {
                    totalArousal += 0.8f * weight
                    totalValence += 0.3f * weight
                    totalStress += 0.9f * weight
                    totalFocus += 0.4f * weight
                }
                EmotionType.ACTIVE -> {
                    totalArousal += 0.9f * weight
                    totalValence += 0.6f * weight
                    totalStress += 0.5f * weight
                    totalFocus += 0.7f * weight
                }
                EmotionType.RESTING -> {
                    totalArousal += 0.3f * weight
                    totalValence += 0.5f * weight
                    totalStress += 0.2f * weight
                    totalFocus += 0.5f * weight
                }
                EmotionType.ANXIOUS -> {
                    totalArousal += 0.7f * weight
                    totalValence += 0.2f * weight
                    totalStress += 0.8f * weight
                    totalFocus += 0.3f * weight
                }
                EmotionType.FOCUSED -> {
                    totalArousal += 0.6f * weight
                    totalValence += 0.7f * weight
                    totalStress += 0.3f * weight
                    totalFocus += 0.9f * weight
                }
                EmotionType.DISTRACTED -> {
                    totalArousal += 0.5f * weight
                    totalValence += 0.4f * weight
                    totalStress += 0.6f * weight
                    totalFocus += 0.2f * weight
                }
                EmotionType.UNKNOWN -> {
                    // Skip unknown emotions
                }
            }
        }
        
        return if (totalWeight > 0) {
            AffectiveState(
                arousal = (totalArousal / totalWeight).coerceIn(0f, 1f),
                valence = (totalValence / totalWeight).coerceIn(0f, 1f),
                stress = (totalStress / totalWeight).coerceIn(0f, 1f),
                focus = (totalFocus / totalWeight).coerceIn(0f, 1f)
            )
        } else {
            AffectiveState(
                arousal = 0.5f,
                valence = 0.5f,
                stress = 0.5f,
                focus = 0.5f
            )
        }
    }
    
    /**
     * Calculate movement intensity from accelerometer history
     */
    private fun calculateMovementIntensity(history: Collection<AccelerometerData>): Float {
        if (history.size < 2) return 0f
        
        val iterator = history.iterator()
        var prev = iterator.next()
        var totalChange = 0f
        while (iterator.hasNext()) {
            val curr = iterator.next()
            
            val dx = curr.x - prev.x
            val dy = curr.y - prev.y
            val dz = curr.z - prev.z
            
            totalChange += sqrt(dx * dx + dy * dy + dz * dz)
            prev = curr
        }
        
        return totalChange / history.size
    }
    
    /**
     * Calculate variability (standard deviation) of values
     */
    private fun calculateVariability(values: Sequence<Float>): Float {
        var count = 0
        var mean = 0.0
        var m2 = 0.0

        values.forEach { value ->
            count++
            val delta = value - mean
            mean += delta / count
            val delta2 = value - mean
            m2 += delta * delta2
        }

        if (count == 0) return 0f
        val variance = (m2 / count).toFloat()
        return sqrt(variance)
    }
    
    /**
     * Calculate rotation stability from gyroscope history
     */
    private fun calculateRotationStability(history: Collection<GyroscopeData>): Float {
        if (history.size < 2) return 0f
        
        val variability = calculateVariability(history.asSequence().map { it.rotationRate })
        val maxVariability = 2.0f // rad/s
        
        // Higher stability = lower variability
        return (1f - (variability / maxVariability).coerceIn(0f, 1f))
    }
    
    /**
     * Clear all history
     */
    fun clearHistory() {
        accelerometerHistory.clear()
        gyroscopeHistory.clear()
        touchEventHistory.clear()
    }

    private fun <T> addWithBound(buffer: ArrayDeque<T>, item: T, maxSize: Int) {
        if (buffer.size >= maxSize) {
            buffer.removeFirst()
        }
        buffer.addLast(item)
    }
}

/**
 * Touch event data for analysis
 */
data class TouchEvent(
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val pressure: Float, // 0.0 to 1.0
    val duration: Float // milliseconds
)

/**
 * Emotion history tracker
 */
class EmotionTracker {
    private val emotionHistory = ArrayDeque<DetectedEmotion>(MAX_HISTORY)
    private val stateHistory = ArrayDeque<AffectiveState>(MAX_HISTORY)
    
    companion object {
        private const val MAX_HISTORY = 1000
    }
    
    fun addEmotion(emotion: DetectedEmotion) {
        addWithBound(emotionHistory, emotion, MAX_HISTORY)
    }
    
    fun addState(state: AffectiveState) {
        addWithBound(stateHistory, state, MAX_HISTORY)
    }
    
    fun getEmotionHistory(): List<DetectedEmotion> = emotionHistory.toList()
    
    fun getStateHistory(): List<AffectiveState> = stateHistory.toList()
    
    fun getEmotionDistribution(): Map<EmotionType, Int> {
        return emotionHistory
            .groupingBy { it.emotion }
            .eachCount()
    }
    
    fun getAverageArousal(): Float {
        return if (stateHistory.isEmpty()) 0.5f
        else stateHistory.map { it.arousal }.average().toFloat()
    }
    
    fun getAverageValence(): Float {
        return if (stateHistory.isEmpty()) 0.5f
        else stateHistory.map { it.valence }.average().toFloat()
    }
    
    fun getAverageStress(): Float {
        return if (stateHistory.isEmpty()) 0.5f
        else stateHistory.map { it.stress }.average().toFloat()
    }
    
    fun getAverageFocus(): Float {
        return if (stateHistory.isEmpty()) 0.5f
        else stateHistory.map { it.focus }.average().toFloat()
    }
    
    fun clear() {
        emotionHistory.clear()
        stateHistory.clear()
    }

    private fun <T> addWithBound(buffer: ArrayDeque<T>, item: T, maxSize: Int) {
        if (buffer.size >= maxSize) {
            buffer.removeFirst()
        }
        buffer.addLast(item)
    }
}
