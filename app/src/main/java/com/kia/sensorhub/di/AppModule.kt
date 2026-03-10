package com.kia.sensorhub.di

import android.content.Context
import androidx.room.Room
import com.kia.sensorhub.data.database.SensorDao
import com.kia.sensorhub.data.database.SensorDatabase
import com.kia.sensorhub.sensors.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing app-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    /**
     * Provide Room Database
     */
    @Provides
    @Singleton
    fun provideSensorDatabase(
        @ApplicationContext context: Context
    ): SensorDatabase {
        return Room.databaseBuilder(
            context,
            SensorDatabase::class.java,
            SensorDatabase.DATABASE_NAME
        ).build()
    }
    
    /**
     * Provide SensorDao
     */
    @Provides
    @Singleton
    fun provideSensorDao(database: SensorDatabase): SensorDao {
        return database.sensorDao()
    }
    
    /**
     * Provide AccelerometerManager
     */
    @Provides
    @Singleton
    fun provideAccelerometerManager(
        @ApplicationContext context: Context
    ): AccelerometerManager {
        return AccelerometerManager(context)
    }
    
    /**
     * Provide GyroscopeManager
     */
    @Provides
    @Singleton
    fun provideGyroscopeManager(
        @ApplicationContext context: Context
    ): GyroscopeManager {
        return GyroscopeManager(context)
    }
    
    /**
     * Provide MagnetometerManager
     */
    @Provides
    @Singleton
    fun provideMagnetometerManager(
        @ApplicationContext context: Context
    ): MagnetometerManager {
        return MagnetometerManager(context)
    }
    
    /**
     * Provide LightSensorManager
     */
    @Provides
    @Singleton
    fun provideLightSensorManager(
        @ApplicationContext context: Context
    ): LightSensorManager {
        return LightSensorManager(context)
    }
    
    /**
     * Provide ProximitySensorManager
     */
    @Provides
    @Singleton
    fun provideProximitySensorManager(
        @ApplicationContext context: Context
    ): ProximitySensorManager {
        return ProximitySensorManager(context)
    }
    
    /**
     * Provide BarometerManager
     */
    @Provides
    @Singleton
    fun provideBarometerManager(
        @ApplicationContext context: Context
    ): BarometerManager {
        return BarometerManager(context)
    }
    
    /**
     * Provide GpsManager
     */
    @Provides
    @Singleton
    fun provideGpsManager(
        @ApplicationContext context: Context
    ): GpsManager {
        return GpsManager(context)
    }
}
