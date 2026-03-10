package com.kia.sensorhub.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kia.sensorhub.data.model.SensorReading
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for sensor readings
 */
@Dao
interface SensorDao {
    
    /**
     * Insert a sensor reading
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: SensorReading): Long
    
    /**
     * Insert multiple sensor readings
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<SensorReading>)
    
    /**
     * Get all sensor readings as Flow
     */
    @Query("SELECT * FROM sensor_readings ORDER BY timestamp DESC")
    fun getAllReadingsFlow(): Flow<List<SensorReading>>
    
    /**
     * Get readings for a specific sensor type
     */
    @Query("SELECT * FROM sensor_readings WHERE sensorType = :sensorType ORDER BY timestamp DESC LIMIT :limit")
    fun getReadingsBySensorType(sensorType: String, limit: Int = 100): Flow<List<SensorReading>>
    
    /**
     * Get readings within a time range
     */
    @Query("SELECT * FROM sensor_readings WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getReadingsByTimeRange(startTime: Long, endTime: Long): Flow<List<SensorReading>>
    
    /**
     * Get latest reading for a sensor type
     */
    @Query("SELECT * FROM sensor_readings WHERE sensorType = :sensorType ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestReading(sensorType: String): SensorReading?
    
    /**
     * Delete all readings
     */
    @Query("DELETE FROM sensor_readings")
    suspend fun deleteAllReadings()
    
    /**
     * Delete readings older than timestamp
     */
    @Query("DELETE FROM sensor_readings WHERE timestamp < :timestamp")
    suspend fun deleteOldReadings(timestamp: Long)
    
    /**
     * Delete readings for a specific sensor type
     */
    @Query("DELETE FROM sensor_readings WHERE sensorType = :sensorType")
    suspend fun deleteReadingsBySensorType(sensorType: String)
    
    /**
     * Get count of all readings
     */
    @Query("SELECT COUNT(*) FROM sensor_readings")
    suspend fun getReadingsCount(): Int
    
    /**
     * Get count of readings for a sensor type
     */
    @Query("SELECT COUNT(*) FROM sensor_readings WHERE sensorType = :sensorType")
    suspend fun getReadingsCountBySensorType(sensorType: String): Int
}
