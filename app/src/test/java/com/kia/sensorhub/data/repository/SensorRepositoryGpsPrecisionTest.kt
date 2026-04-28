package com.kia.sensorhub.data.repository

import com.kia.sensorhub.data.database.SensorDao
import com.kia.sensorhub.data.model.GpsData
import com.kia.sensorhub.data.model.SensorReading
import com.kia.sensorhub.sensors.AccelerometerManager
import com.kia.sensorhub.sensors.GyroscopeManager
import com.kia.sensorhub.sensors.MagnetometerManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testy repozytorium potwierdzające zachowanie precyzji GPS po zapisie i odczycie.
 */
class SensorRepositoryGpsPrecisionTest {

    private val sensorDao: SensorDao = mockk()
    private val accelerometerManager: AccelerometerManager = mockk(relaxed = true)
    private val gyroscopeManager: GyroscopeManager = mockk(relaxed = true)
    private val magnetometerManager: MagnetometerManager = mockk(relaxed = true)

    private val repository = SensorRepository(
        sensorDao = sensorDao,
        accelerometerManager = accelerometerManager,
        gyroscopeManager = gyroscopeManager,
        magnetometerManager = magnetometerManager
    )

    @Test
    fun `save and read GPS keeps double precision in dedicated columns`() = runTest {
        // Przygotowuje dane GPS z wysoką precyzją, które wcześniej były obcinane do Float.
        val gpsData = GpsData(
            timestamp = 1_713_141_592_653L,
            latitude = 52.229_675_612_345,
            longitude = 21.012_228_987_654,
            altitude = 123.456_789_123,
            speed = 10.5f,
            accuracy = 2.3f
        )

        var capturedReading: SensorReading? = null
        coEvery { sensorDao.insertReading(any()) } answers {
            capturedReading = firstArg()
            1L
        }
        // Zwraca ten sam rekord z DAO, aby zasymulować odczyt po zapisie.
        coEvery { sensorDao.getLatestReading("GPS") } answers { capturedReading }

        repository.saveSensorReading(gpsData)
        val persisted = repository.getLatestReading("GPS")

        coVerify(exactly = 1) { sensorDao.insertReading(any()) }
        coVerify(exactly = 1) { sensorDao.getLatestReading("GPS") }

        requireNotNull(persisted)
        assertEquals(gpsData.latitude, persisted.latitude!!, 0.0)
        assertEquals(gpsData.longitude, persisted.longitude!!, 0.0)
        assertEquals(gpsData.altitude, persisted.altitude!!, 0.0)
    }
}
