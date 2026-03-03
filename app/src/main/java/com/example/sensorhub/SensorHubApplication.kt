package com.kia.sensorhub

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kia.sensorhub.notifications.NotificationHelper
import com.kia.sensorhub.utils.ErrorHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * SensorHub Application Class
 * Main application entry point with Hilt integration
 */
@HiltAndroidApp
class SensorHubApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize application components
        initializeNotifications()
        initializeErrorHandling()
        
        // Log app start
        ErrorHandler.logInfo(
            tag = "Application",
            message = "SensorHub started - Version 3.0.0-alpha Build 3"
        )
    }
    
    /**
     * Initialize notification system
     */
    private fun initializeNotifications() {
        try {
            NotificationHelper.initialize(this)
            createNotificationChannels()
            ErrorHandler.logInfo(
                tag = "Application",
                message = "Notification system initialized"
            )
        } catch (e: Exception) {
            ErrorHandler.logError(
                tag = "Application",
                message = "Failed to initialize notifications",
                throwable = e
            )
        }
    }
    
    /**
     * Create notification channels for Android O+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Create channels
            val channels = listOf(
                NotificationChannel(
                    "sensor_alerts",
                    "Sensor Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Critical sensor alerts and warnings"
                    enableVibration(true)
                    enableLights(true)
                },
                
                NotificationChannel(
                    "sensor_insights",
                    "Sensor Insights",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily insights and analysis"
                },
                
                NotificationChannel(
                    "achievements",
                    "Achievements",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Achievement unlocks and progress"
                },
                
                NotificationChannel(
                    "monitoring",
                    "Background Monitoring",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Ongoing sensor monitoring status"
                    setShowBadge(false)
                }
            )
            
            notificationManager.createNotificationChannels(channels)
        }
    }
    
    /**
     * Initialize error handling
     */
    private fun initializeErrorHandling() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            ErrorHandler.logError(
                tag = "UncaughtException",
                message = "Uncaught exception in thread: ${thread.name}",
                throwable = throwable
            )
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * Provide WorkManager configuration with Hilt
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    
    override fun onTerminate() {
        super.onTerminate()
        ErrorHandler.logInfo(
            tag = "Application",
            message = "SensorHub terminated"
        )
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        ErrorHandler.logWarning(
            tag = "Application",
            message = "Low memory warning"
        )
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                ErrorHandler.logWarning(
                    tag = "Application",
                    message = "Memory critically low - running"
                )
            }
            TRIM_MEMORY_COMPLETE -> {
                ErrorHandler.logWarning(
                    tag = "Application",
                    message = "Memory critically low - complete"
                )
            }
        }
    }
}
