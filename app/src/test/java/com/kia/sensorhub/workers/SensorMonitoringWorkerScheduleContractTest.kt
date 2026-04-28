package com.kia.sensorhub.workers

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test kontraktowy walidacji interwału harmonogramu dla SensorMonitoringWorker.
 */
class SensorMonitoringWorkerScheduleContractTest {

    @Test
    fun `sanitizeIntervalMinutes podbija wartosci ponizej minimum do 15 minut`() {
        // Sprawdzenie kluczowego kontraktu: wartości mniejsze od 15 muszą zostać podbite do 15.
        val safeInterval = SensorMonitoringWorker.sanitizeIntervalMinutes(intervalMinutes = 5L)

        assertEquals(SensorMonitoringWorker.MIN_PERIODIC_INTERVAL_MINUTES, safeInterval)
    }
}
