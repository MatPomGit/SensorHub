package com.kia.sensorhub.viewmodel

import app.cash.turbine.test
import com.kia.sensorhub.data.model.AccelerometerData
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.sensors.SensorInfo
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
 * Unit tests for AccelerometerViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AccelerometerViewModelTest {
    
    private lateinit var repository: SensorRepository
    private lateinit var viewModel: AccelerometerViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        
        // Mock sensor availability
        every { repository.isAccelerometerAvailable() } returns true
        every { repository.getAccelerometerInfo() } returns SensorInfo(
            name = "Test Accelerometer",
            vendor = "Test Vendor",
            version = 1,
            type = 1,
            maxRange = 20f,
            resolution = 0.01f,
            power = 0.5f,
            minDelay = 10000
        )
        
        viewModel = AccelerometerViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state has sensor available`() = runTest {
        // Given
        val state = viewModel.uiState.value
        
        // Then
        assertTrue(state.isAvailable)
        assertFalse(state.isMonitoring)
        assertFalse(state.isSavingEnabled)
    }
    
    @Test
    fun `startMonitoring updates state correctly`() = runTest {
        // Given
        val testData = AccelerometerData(
            timestamp = System.currentTimeMillis(),
            x = 1.0f,
            y = 2.0f,
            z = 3.0f,
            magnitude = 3.74f
        )
        every { repository.getAccelerometerFlow() } returns flowOf(testData)
        
        // When
        viewModel.startMonitoring()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isMonitoring)
        assertEquals(testData, state.currentData)
    }
    
    @Test
    fun `stopMonitoring updates state correctly`() = runTest {
        // Given
        val testData = AccelerometerData(x = 1.0f, y = 2.0f, z = 3.0f)
        every { repository.getAccelerometerFlow() } returns flowOf(testData)
        
        viewModel.startMonitoring()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.stopMonitoring()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isMonitoring)
    }
    
    @Test
    fun `toggleSaving changes saving state`() = runTest {
        // Given
        val initialState = viewModel.uiState.value.isSavingEnabled
        
        // When
        viewModel.toggleSaving()
        
        // Then
        val newState = viewModel.uiState.value.isSavingEnabled
        assertEquals(!initialState, newState)
    }
    
    @Test
    fun `clearHistory removes data history`() = runTest {
        // Given
        val testData = AccelerometerData(x = 1.0f, y = 2.0f, z = 3.0f)
        every { repository.getAccelerometerFlow() } returns flowOf(testData)
        
        viewModel.startMonitoring()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearHistory()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.dataHistory.isEmpty())
    }
    
    @Test
    fun `sensor unavailable shows error`() = runTest {
        // Given
        every { repository.isAccelerometerAvailable() } returns false
        val viewModelUnavailable = AccelerometerViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModelUnavailable.startMonitoring()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModelUnavailable.uiState.value
        assertTrue(state.error != null)
        assertFalse(state.isMonitoring)
    }
}
