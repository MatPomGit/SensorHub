package com.kia.sensorhub.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kia.sensorhub.data.model.SensorReading

/**
 * Room database for SensorHub
 * Stores sensor readings for historical analysis
 */
@Database(
    entities = [SensorReading::class],
    version = 1,
    exportSchema = false
)
abstract class SensorDatabase : RoomDatabase() {
    
    abstract fun sensorDao(): SensorDao
    
    companion object {
        const val DATABASE_NAME = "sensor_hub_db"
    }
}
