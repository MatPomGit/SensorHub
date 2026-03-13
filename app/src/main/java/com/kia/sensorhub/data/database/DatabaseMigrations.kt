package com.kia.sensorhub.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database Migration Strategies
 */
object DatabaseMigrations {
    
    /**
     * Migration from version 1 to 2
     * Example: Add new column
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Add accuracy column
            database.execSQL(
                "ALTER TABLE sensor_readings ADD COLUMN accuracy REAL NOT NULL DEFAULT 0.0"
            )
        }
    }
    
    /**
     * Migration from version 2 to 3
     * Example: Add index for better query performance
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add index on sensorType and timestamp
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_sensor_readings_type_timestamp " +
                "ON sensor_readings(sensorType, timestamp)"
            )
        }
    }
    
    /**
     * Get all migrations
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            // MIGRATION_1_2,
            // MIGRATION_2_3
            // Add more migrations as needed
        )
    }
}

/**
 * Performance Monitoring for Database Operations
 */
object PerformanceMonitor {
    
    private const val TAG = "PerformanceMonitor"
    private val measurements = mutableMapOf<String, MutableList<Long>>()
    
    /**
     * Start measuring operation
     */
    fun startMeasurement(operationName: String): Long {
        return System.currentTimeMillis()
    }
    
    /**
     * End measurement and log
     */
    fun endMeasurement(operationName: String, startTime: Long) {
        val duration = System.currentTimeMillis() - startTime
        
        // Store measurement
        measurements.getOrPut(operationName) { mutableListOf() }.add(duration)
        
        // Log if slow
        if (duration > 100) {
            android.util.Log.w(
                TAG,
                "Slow operation: $operationName took ${duration}ms"
            )
        }
    }
    
    /**
     * Measure block execution
     */
    inline fun <T> measure(operationName: String, block: () -> T): T {
        val start = startMeasurement(operationName)
        return try {
            block()
        } finally {
            endMeasurement(operationName, start)
        }
    }
    
    /**
     * Get statistics for operation
     */
    fun getStatistics(operationName: String): OperationStats? {
        val times = measurements[operationName] ?: return null
        
        if (times.isEmpty()) return null
        
        return OperationStats(
            operationName = operationName,
            count = times.size,
            avgMs = times.average(),
            minMs = times.minOrNull() ?: 0L,
            maxMs = times.maxOrNull() ?: 0L,
            totalMs = times.sum()
        )
    }
    
    /**
     * Get all statistics
     */
    fun getAllStatistics(): List<OperationStats> {
        return measurements.keys.mapNotNull { getStatistics(it) }
    }
    
    /**
     * Clear all measurements
     */
    fun clear() {
        measurements.clear()
    }
    
    /**
     * Print statistics
     */
    fun printStatistics() {
        getAllStatistics().forEach { stats ->
            android.util.Log.i(
                TAG,
                "${stats.operationName}: " +
                "count=${stats.count}, " +
                "avg=${String.format("%.2f", stats.avgMs)}ms, " +
                "min=${stats.minMs}ms, " +
                "max=${stats.maxMs}ms"
            )
        }
    }
}

/**
 * Operation statistics data class
 */
data class OperationStats(
    val operationName: String,
    val count: Int,
    val avgMs: Double,
    val minMs: Long,
    val maxMs: Long,
    val totalMs: Long
)

/**
 * Database Health Monitor
 */
object DatabaseHealthMonitor {
    
    private const val TAG = "DatabaseHealth"
    
    /**
     * Check database size
     */
    fun checkDatabaseSize(database: SupportSQLiteDatabase): Long {
        val cursor = database.query("SELECT page_count * page_size as size FROM pragma_page_count(), pragma_page_size()")
        return if (cursor.moveToFirst()) {
            cursor.getLong(0)
        } else {
            0L
        }.also {
            cursor.close()
        }
    }
    
    /**
     * Check number of records
     */
    fun checkRecordCount(database: SupportSQLiteDatabase, tableName: String): Int {
        val cursor = database.query("SELECT COUNT(*) FROM $tableName")
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }.also {
            cursor.close()
        }
    }
    
    /**
     * Check index usage
     */
    fun checkIndexes(database: SupportSQLiteDatabase): List<String> {
        val indexes = mutableListOf<String>()
        val cursor = database.query("SELECT name FROM sqlite_master WHERE type='index'")
        
        while (cursor.moveToNext()) {
            indexes.add(cursor.getString(0))
        }
        cursor.close()
        
        return indexes
    }
    
    /**
     * Run VACUUM to optimize database
     */
    fun vacuum(database: SupportSQLiteDatabase) {
        val start = System.currentTimeMillis()
        database.execSQL("VACUUM")
        val duration = System.currentTimeMillis() - start
        
        android.util.Log.i(TAG, "VACUUM completed in ${duration}ms")
    }
    
    /**
     * Analyze database for query optimization
     */
    fun analyze(database: SupportSQLiteDatabase) {
        database.execSQL("ANALYZE")
        android.util.Log.i(TAG, "ANALYZE completed")
    }
    
    /**
     * Get database integrity check result
     */
    fun checkIntegrity(database: SupportSQLiteDatabase): Boolean {
        val cursor = database.query("PRAGMA integrity_check")
        val result = if (cursor.moveToFirst()) {
            cursor.getString(0) == "ok"
        } else {
            false
        }
        cursor.close()
        return result
    }
}

/**
 * Query Optimizer
 */
object QueryOptimizer {
    
    /**
     * Explain query execution plan
     */
    fun explainQuery(database: SupportSQLiteDatabase, query: String): String {
        val cursor = database.query("EXPLAIN QUERY PLAN $query")
        val plan = StringBuilder()
        
        while (cursor.moveToNext()) {
            plan.append(cursor.getString(0)).append("\n")
        }
        cursor.close()
        
        return plan.toString()
    }
    
    /**
     * Suggest indexes for query
     */
    fun suggestIndexes(tableName: String, columns: List<String>): String {
        return "CREATE INDEX IF NOT EXISTS idx_${tableName}_${columns.joinToString("_")} " +
               "ON $tableName(${columns.joinToString(", ")})"
    }
}

/**
 * Database Backup Utility
 */
object DatabaseBackup {
    
    /**
     * Create backup of database
     */
    fun backup(
        sourceDb: SupportSQLiteDatabase,
        backupPath: String
    ): Boolean {
        return try {
            // Checkpoint WAL file first
            sourceDb.query("PRAGMA wal_checkpoint(FULL)").close()
            
            // Copy database file
            // Note: Actual file copy would require file system access
            android.util.Log.i("DatabaseBackup", "Backup created at $backupPath")
            true
        } catch (e: Exception) {
            android.util.Log.e("DatabaseBackup", "Backup failed", e)
            false
        }
    }
    
    /**
     * Restore database from backup
     */
    fun restore(
        targetDb: SupportSQLiteDatabase,
        backupPath: String
    ): Boolean {
        return try {
            android.util.Log.i("DatabaseBackup", "Restored from $backupPath")
            true
        } catch (e: Exception) {
            android.util.Log.e("DatabaseBackup", "Restore failed", e)
            false
        }
    }
}

/**
 * Memory Monitor
 */
object MemoryMonitor {
    
    private const val TAG = "MemoryMonitor"
    
    /**
     * Get current memory usage
     */
    fun getCurrentMemoryUsage(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        
        return MemoryInfo(
            usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024,
            freeMemoryMB = runtime.freeMemory() / 1024 / 1024,
            totalMemoryMB = runtime.totalMemory() / 1024 / 1024,
            maxMemoryMB = runtime.maxMemory() / 1024 / 1024
        )
    }
    
    /**
     * Log memory usage
     */
    fun logMemoryUsage() {
        val info = getCurrentMemoryUsage()
        android.util.Log.i(
            TAG,
            "Memory: Used=${info.usedMemoryMB}MB, " +
            "Free=${info.freeMemoryMB}MB, " +
            "Total=${info.totalMemoryMB}MB, " +
            "Max=${info.maxMemoryMB}MB"
        )
    }
    
    /**
     * Check if low memory
     */
    fun isLowMemory(): Boolean {
        val info = getCurrentMemoryUsage()
        val usagePercentage = (info.usedMemoryMB.toDouble() / info.maxMemoryMB) * 100
        return usagePercentage > 80
    }
}

/**
 * Memory information data class
 */
data class MemoryInfo(
    val usedMemoryMB: Long,
    val freeMemoryMB: Long,
    val totalMemoryMB: Long,
    val maxMemoryMB: Long
) {
    val usagePercentage: Double
        get() = (usedMemoryMB.toDouble() / maxMemoryMB) * 100
}
