package com.kia.sensorhub.viewmodel

import app.cash.turbine.test
import com.kia.sensorhub.affective.*
import com.kia.sensorhub.data.model.GyroscopeData
import com.kia.sensorhub.data.model.MagnetometerData
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.sensors.SensorInfo
import com.kia.sensorhub.ui.screens.AffectiveViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for GyroscopeViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GyroscopeViewModelTest {
    
    private lateinit var repository: SensorRepository
    private lateinit var viewModel: GyroscopeViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        
        every { repository.isGyroscopeAvailable() } returns true
        every { repository.getGyroscopeInfo() } returns SensorInfo(
            name = "Test Gyroscope",
            vendor = "Test Vendor",
            version = 1,
            type = 4,
            maxRange = 10f,
            resolution = 0.01f,
            power = 0.5f,
            minDelay = 10000
        )
        
        viewModel = GyroscopeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state has sensor available`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.isAvailable)
        assertFalse(state.isMonitoring)
    }
    
    @Test
    fun `startMonitoring updates state with gyroscope data`() = runTest {
        val testData = GyroscopeData(
            x = 0.5f,
            y = 0.3f,
            z = 0.2f,
            rotationRate = 0.6f
        )
        every { repository.getGyroscopeFlow() } returns flowOf(testData)
        
        viewModel.startMonitoring()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.isMonitoring)
        assertEquals(testData, state.currentData)
    }
    
    @Test
    fun `stopMonitoring stops data collection`() = runTest {
        val testData = GyroscopeData(x = 0.5f, y = 0.3f, z = 0.2f)
        every { repository.getGyroscopeFlow() } returns flowOf(testData)
        
        viewModel.startMonitoring()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.stopMonitoring()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isMonitoring)
    }
}

/**
 * Unit tests for MagnetometerViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MagnetometerViewModelTest {
    
    private lateinit var repository: SensorRepository
    private lateinit var viewModel: MagnetometerViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        
        every { repository.isMagnetometerAvailable() } returns true
        every { repository.getMagnetometerInfo() } returns SensorInfo(
            name = "Test Magnetometer",
            vendor = "Test Vendor",
            version = 1,
            type = 2,
            maxRange = 100f,
            resolution = 0.1f,
            power = 0.5f,
            minDelay = 10000
        )
        
        viewModel = MagnetometerViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `getCompassDirection returns correct direction for North`() {
        assertEquals("North", viewModel.getCompassDirection(0f))
        assertEquals("North", viewModel.getCompassDirection(360f))
    }
    
    @Test
    fun `getCompassDirection returns correct direction for East`() {
        assertEquals("East", viewModel.getCompassDirection(90f))
    }
    
    @Test
    fun `getCompassDirection returns correct direction for South`() {
        assertEquals("South", viewModel.getCompassDirection(180f))
    }
    
    @Test
    fun `getCompassDirection returns correct direction for West`() {
        assertEquals("West", viewModel.getCompassDirection(270f))
    }
    
    @Test
    fun `startMonitoring updates state with magnetometer data`() = runTest {
        val testData = MagnetometerData(
            x = 10f,
            y = 20f,
            z = 30f,
            magnitude = 37.4f,
            azimuth = 45f
        )
        every { repository.getMagnetometerFlow() } returns flowOf(testData)
        
        viewModel.startMonitoring()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.isMonitoring)
        assertEquals(testData, state.currentData)
    }
}

/**
 * Unit tests for AffectiveAnalyzer
 */
class AffectiveAnalyzerTest {
    
    private lateinit var analyzer: AffectiveAnalyzer
    
    @Before
    fun setup() {
        analyzer = AffectiveAnalyzer()
    }
    
    @Test
    fun `analyzeFromAccelerometer detects calm state with low movement`() {
        // Generate calm movement data
        repeat(30) {
            val data = com.kia.sensorhub.data.model.AccelerometerData(
                x = 0.1f,
                y = 0.1f,
                z = 9.8f,
                magnitude = 9.8f
            )
            val emotion = analyzer.analyzeFromAccelerometer(data)
            
            if (it >= 25) { // After sufficient history
                assertEquals(EmotionType.CALM, emotion.emotion)
                assertTrue(emotion.confidence > 0.5f)
            }
        }
    }
    
    @Test
    fun `analyzeFromAccelerometer detects active state with high movement`() {
        // Generate active movement data
        repeat(30) {
            val data = com.kia.sensorhub.data.model.AccelerometerData(
                x = (Math.random() * 10).toFloat(),
                y = (Math.random() * 10).toFloat(),
                z = (Math.random() * 10).toFloat(),
                magnitude = 15f
            )
            val emotion = analyzer.analyzeFromAccelerometer(data)
            
            if (it >= 25) {
                assertTrue(emotion.emotion == EmotionType.ACTIVE || emotion.emotion == EmotionType.STRESSED)
            }
        }
    }
    
    @Test
    fun `computeAffectiveState combines multiple emotions correctly`() {
        val emotions = listOf(
            DetectedEmotion(EmotionType.CALM, 0.8f),
            DetectedEmotion(EmotionType.CALM, 0.7f),
            DetectedEmotion(EmotionType.RESTING, 0.6f)
        )
        
        val state = analyzer.computeAffectiveState(emotions)
        
        // Calm emotions should result in low arousal and stress
        assertTrue(state.arousal < 0.5f)
        assertTrue(state.stress < 0.5f)
    }
    
    @Test
    fun `computeAffectiveState handles empty emotion list`() {
        val state = analyzer.computeAffectiveState(emptyList())
        
        // Should return neutral state
        assertEquals(0.5f, state.arousal)
        assertEquals(0.5f, state.valence)
        assertEquals(0.5f, state.stress)
        assertEquals(0.5f, state.focus)
    }
}

/**
 * Unit tests for EmotionTracker
 */
class EmotionTrackerTest {
    
    private lateinit var tracker: EmotionTracker
    
    @Before
    fun setup() {
        tracker = EmotionTracker()
    }
    
    @Test
    fun `addEmotion stores emotion in history`() {
        val emotion = DetectedEmotion(EmotionType.CALM, 0.8f)
        tracker.addEmotion(emotion)
        
        val history = tracker.getEmotionHistory()
        assertEquals(1, history.size)
        assertEquals(emotion, history.first())
    }
    
    @Test
    fun `getEmotionDistribution counts emotions correctly`() {
        tracker.addEmotion(DetectedEmotion(EmotionType.CALM, 0.8f))
        tracker.addEmotion(DetectedEmotion(EmotionType.CALM, 0.7f))
        tracker.addEmotion(DetectedEmotion(EmotionType.STRESSED, 0.6f))
        
        val distribution = tracker.getEmotionDistribution()
        assertEquals(2, distribution[EmotionType.CALM])
        assertEquals(1, distribution[EmotionType.STRESSED])
    }
    
    @Test
    fun `getAverageArousal calculates correct average`() {
        tracker.addState(AffectiveState(arousal = 0.2f, valence = 0.5f, stress = 0.3f, focus = 0.4f))
        tracker.addState(AffectiveState(arousal = 0.8f, valence = 0.5f, stress = 0.7f, focus = 0.6f))
        
        val avgArousal = tracker.getAverageArousal()
        assertEquals(0.5f, avgArousal, 0.01f)
    }
    
    @Test
    fun `clear removes all history`() {
        tracker.addEmotion(DetectedEmotion(EmotionType.CALM, 0.8f))
        tracker.addState(AffectiveState(arousal = 0.5f, valence = 0.5f, stress = 0.5f, focus = 0.5f))
        
        tracker.clear()
        
        assertTrue(tracker.getEmotionHistory().isEmpty())
        assertTrue(tracker.getStateHistory().isEmpty())
    }
}

/**
 * Integration test for AffectiveViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AffectiveViewModelTest {
    
    private lateinit var repository: SensorRepository
    private lateinit var viewModel: AffectiveViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = AffectiveViewModel(repository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `toggleAnalysis starts and stops analysis`() = runTest {
        assertFalse(viewModel.uiState.value.isAnalyzing)
        
        val testData = com.kia.sensorhub.data.model.AccelerometerData()
        every { repository.getAccelerometerFlow() } returns flowOf(testData)
        
        viewModel.toggleAnalysis()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isAnalyzing)
        
        viewModel.toggleAnalysis()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isAnalyzing)
    }
    
    @Test
    fun `clearHistory clears emotion history`() = runTest {
        // Add some emotions first
        val testData = com.kia.sensorhub.data.model.AccelerometerData()
        every { repository.getAccelerometerFlow() } returns flowOf(testData)
        
        viewModel.toggleAnalysis()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.clearHistory()
        
        assertTrue(viewModel.uiState.value.emotionHistory.isEmpty())
        assertTrue(viewModel.uiState.value.emotionDistribution.isEmpty())
    }
}
