package com.kia.sensorhub.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.kia.sensorhub.data.repository.SensorRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import java.util.concurrent.TimeUnit

/**
 * Background worker for periodic sensor data collection
 * Collects sensor data in the background at specified intervals
 */
@HiltWorker
class SensorMonitoringWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SensorRepository
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        const val WORK_NAME = "sensor_monitoring_work"
        const val KEY_SENSOR_TYPE = "sensor_type"
        const val KEY_SAMPLE_COUNT = "sample_count"
        
        /**
         * Schedule periodic sensor monitoring
         */
        fun schedule(
            context: Context,
            sensorType: String,
            intervalMinutes: Long = 15,
            sampleCount: Int = 10
        ) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            
            val inputData = workDataOf(
                KEY_SENSOR_TYPE to sensorType,
                KEY_SAMPLE_COUNT to sampleCount
            )
            
            val request = PeriodicWorkRequestBuilder<SensorMonitoringWorker>(
                intervalMinutes, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag(WORK_NAME)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "$WORK_NAME-$sensorType",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
        
        /**
         * Cancel all sensor monitoring work
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_NAME)
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            val sensorType = inputData.getString(KEY_SENSOR_TYPE) ?: return Result.failure()
            val sampleCount = inputData.getInt(KEY_SAMPLE_COUNT, 10)
            
            // Collect sensor data based on type
            when (sensorType) {
                "ACCELEROMETER" -> collectAccelerometerData(sampleCount)
                "GYROSCOPE" -> collectGyroscopeData(sampleCount)
                "MAGNETOMETER" -> collectMagnetometerData(sampleCount)
                else -> return Result.failure()
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private suspend fun collectAccelerometerData(sampleCount: Int) {
        val samples = repository.getAccelerometerFlow()
            .take(sampleCount)
            .toList()

        repository.saveSensorReadings(samples)
    }
    
    private suspend fun collectGyroscopeData(sampleCount: Int) {
        val samples = repository.getGyroscopeFlow()
            .take(sampleCount)
            .toList()

        repository.saveSensorReadings(samples)
    }
    
    private suspend fun collectMagnetometerData(sampleCount: Int) {
        val samples = repository.getMagnetometerFlow()
            .take(sampleCount)
            .toList()

        repository.saveSensorReadings(samples)
    }
}

/**
 * Worker for cleaning old sensor data
 */
@HiltWorker
class DataCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SensorRepository
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        const val WORK_NAME = "data_cleanup_work"
        private const val RETENTION_DAYS = 7L
        
        /**
         * Schedule periodic data cleanup
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(true)
                .build()
            
            val request = PeriodicWorkRequestBuilder<DataCleanupWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .addTag(WORK_NAME)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            val cutoffTime = System.currentTimeMillis() - (RETENTION_DAYS * 24 * 60 * 60 * 1000)
            repository.deleteOldReadings(cutoffTime)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

/**
 * Worker for generating daily statistics
 */
@HiltWorker
class StatisticsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SensorRepository
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        const val WORK_NAME = "statistics_work"
        
        /**
         * Schedule one-time statistics generation
         */
        fun scheduleOneTime(context: Context) {
            val request = OneTimeWorkRequestBuilder<StatisticsWorker>()
                .addTag(WORK_NAME)
                .build()
            
            WorkManager.getInstance(context).enqueue(request)
        }
        
        /**
         * Schedule periodic statistics generation
         */
        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            
            val request = PeriodicWorkRequestBuilder<StatisticsWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .addTag(WORK_NAME)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            // Generate and store daily statistics
            val totalReadings = repository.getReadingsCount()
            
            // TODO: Store statistics in database or preferences
            
            Result.success(
                workDataOf(
                    "total_readings" to totalReadings,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

/**
 * Helper object for managing background work
 */
object WorkManagerHelper {
    
    /**
     * Initialize all periodic workers
     */
    fun initializePeriodicWork(context: Context) {
        // Schedule data cleanup
        DataCleanupWorker.schedule(context)
        
        // Schedule daily statistics
        StatisticsWorker.schedulePeriodic(context)
        
        // Optional: Schedule sensor monitoring if needed
        // SensorMonitoringWorker.schedule(context, "ACCELEROMETER")
    }
    
    /**
     * Cancel all background work
     */
    fun cancelAllWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }
    
    /**
     * Get work info for monitoring
     */
    fun getWorkInfo(context: Context, tag: String): androidx.lifecycle.LiveData<List<WorkInfo>> {
        return WorkManager.getInstance(context).getWorkInfosByTagLiveData(tag)
    }
}
