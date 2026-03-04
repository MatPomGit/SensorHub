package com.kia.sensorhub.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kia.sensorhub.MainActivity
import com.kia.sensorhub.R
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.data.model.toSensorReading
import com.kia.sensorhub.sensors.AccelerometerManager
import com.kia.sensorhub.sensors.GyroscopeManager
import com.kia.sensorhub.sensors.MagnetometerManager
import com.kia.sensorhub.utils.ErrorHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

/**
 * Foreground Service for continuous sensor monitoring
 */
@AndroidEntryPoint
class SensorMonitoringService : Service() {
    
    @Inject
    lateinit var repository: SensorRepository
    
    @Inject
    lateinit var accelerometerManager: AccelerometerManager
    
    @Inject
    lateinit var gyroscopeManager: GyroscopeManager
    
    @Inject
    lateinit var magnetometerManager: MagnetometerManager
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitoringJob: Job? = null
    
    private var sensorsActive = 0
    private var startTime = 0L
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "monitoring"
        
        const val ACTION_START = "com.kia.sensorhub.START_MONITORING"
        const val ACTION_STOP = "com.kia.sensorhub.STOP_MONITORING"
        const val EXTRA_SENSORS = "extra_sensors"
        
        /**
         * Start the monitoring service
         */
        fun start(context: Context, sensors: Array<String>) {
            val intent = Intent(context, SensorMonitoringService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SENSORS, sensors)
            }
            context.startForegroundService(intent)
        }
        
        /**
         * Stop the monitoring service
         */
        fun stop(context: Context) {
            val intent = Intent(context, SensorMonitoringService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        ErrorHandler.logInfo(
            tag = "SensorMonitoringService",
            message = "Service created"
        )
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sensors = intent.getStringArrayExtra(EXTRA_SENSORS) ?: emptyArray()
                startMonitoring(sensors)
            }
            ACTION_STOP -> {
                stopMonitoring()
            }
        }
        
        return START_STICKY
    }
    
    private fun startMonitoring(sensors: Array<String>) {
        startTime = System.currentTimeMillis()
        sensorsActive = sensors.size
        
        // Start foreground
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Start monitoring
        monitoringJob = serviceScope.launch {
            ErrorHandler.logInfo(
                tag = "SensorMonitoringService",
                message = "Monitoring started for ${sensors.size} sensors"
            )
            
            // Monitor accelerometer
            if ("accelerometer" in sensors) {
                launch {
                    accelerometerManager.getAccelerometerFlow()
                        .catch { e ->
                            ErrorHandler.logError(
                                tag = "SensorMonitoringService",
                                message = "Accelerometer error",
                                throwable = e
                            )
                        }
                        .collect { data ->
                            repository.saveSensorReading(data)
                        }
                }
            }
            
            // Monitor gyroscope
            if ("gyroscope" in sensors) {
                launch {
                    gyroscopeManager.getGyroscopeFlow()
                        .catch { e ->
                            ErrorHandler.logError(
                                tag = "SensorMonitoringService",
                                message = "Gyroscope error",
                                throwable = e
                            )
                        }
                        .collect { data ->
                            repository.saveSensorReading(data)
                        }
                }
            }
            
            // Monitor magnetometer
            if ("magnetometer" in sensors) {
                launch {
                    magnetometerManager.getMagnetometerFlow()
                        .catch { e ->
                            ErrorHandler.logError(
                                tag = "SensorMonitoringService",
                                message = "Magnetometer error",
                                throwable = e
                            )
                        }
                        .collect { data ->
                            repository.saveSensorReading(data)
                        }
                }
            }
            
            // Update notification periodically
            launch {
                while (isActive) {
                    delay(5000) // Update every 5 seconds
                    updateNotification()
                }
            }
        }
    }
    
    private fun stopMonitoring() {
        ErrorHandler.logInfo(
            tag = "SensorMonitoringService",
            message = "Monitoring stopped"
        )
        
        monitoringJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, SensorMonitoringService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SensorHub Monitoring")
            .setContentText("$sensorsActive sensor(s) active • ${getDuration()}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )
            .build()
    }
    
    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun getDuration(): String {
        val durationMs = System.currentTimeMillis() - startTime
        val minutes = (durationMs / 1000 / 60).toInt()
        val hours = minutes / 60
        val mins = minutes % 60
        
        return if (hours > 0) {
            "${hours}h ${mins}m"
        } else {
            "${mins}m"
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
        serviceScope.cancel()
        
        ErrorHandler.logInfo(
            tag = "SensorMonitoringService",
            message = "Service destroyed"
        )
    }
}
