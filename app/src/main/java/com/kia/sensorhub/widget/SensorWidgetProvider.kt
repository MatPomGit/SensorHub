package com.kia.sensorhub.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.kia.sensorhub.MainActivity
import com.kia.sensorhub.R

/**
 * Home Screen Widget for SensorHub
 * Provides quick access to sensors and statistics
 */
class SensorWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all widget instances
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        // Widget first created
    }
    
    override fun onDisabled(context: Context) {
        // Last widget removed
    }
    
    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Create RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_sensor_quick_access)
            
            // Set up click listeners for sensor buttons
            setupSensorButton(context, views, R.id.widget_accelerometer, "accelerometer")
            setupSensorButton(context, views, R.id.widget_gyroscope, "gyroscope")
            setupSensorButton(context, views, R.id.widget_magnetometer, "magnetometer")
            setupSensorButton(context, views, R.id.widget_light, "light")
            
            // Set up main app button
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_open_app, pendingIntent)
            
            // Update widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        private fun setupSensorButton(
            context: Context,
            views: RemoteViews,
            buttonId: Int,
            sensorType: String
        ) {
            val intent = Intent(context, MainActivity::class.java).apply {
                action = "com.kia.sensorhub.OPEN_SENSOR"
                putExtra("sensor_type", sensorType)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                buttonId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            views.setOnClickPendingIntent(buttonId, pendingIntent)
        }
    }
}

/**
 * Widget Configuration Activity (optional)
 */
class SensorWidgetConfigActivity : android.app.Activity() {
    
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get widget ID from intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        // If invalid, finish
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        // Set result
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        
        // Update widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        SensorWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)
        
        finish()
    }
}

/**
 * Widget Update Service
 * Updates widget with real sensor data
 */
class WidgetUpdateService(context: Context) {
    
    private val sharedPrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    
    /**
     * Update widget with current sensor values
     */
    fun updateWidgetData(
        context: Context,
        accelerometerValue: Float,
        gyroscopeValue: Float,
        magnetometerValue: Float
    ) {
        // Save to preferences
        sharedPrefs.edit().apply {
            putFloat("accelerometer", accelerometerValue)
            putFloat("gyroscope", gyroscopeValue)
            putFloat("magnetometer", magnetometerValue)
            putLong("last_update", System.currentTimeMillis())
            apply()
        }
        
        // Trigger widget update for all existing widget instances
        val widgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = widgetManager.getAppWidgetIds(
            ComponentName(context, SensorWidgetProvider::class.java)
        )

        if (widgetIds.isEmpty()) {
            return
        }

        val intent = Intent(context, SensorWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        }
        context.sendBroadcast(intent)
    }
    
    /**
     * Get cached sensor values
     */
    fun getCachedValues(): WidgetData {
        return WidgetData(
            accelerometer = sharedPrefs.getFloat("accelerometer", 0f),
            gyroscope = sharedPrefs.getFloat("gyroscope", 0f),
            magnetometer = sharedPrefs.getFloat("magnetometer", 0f),
            lastUpdate = sharedPrefs.getLong("last_update", 0L)
        )
    }
}

/**
 * Widget data class
 */
data class WidgetData(
    val accelerometer: Float,
    val gyroscope: Float,
    val magnetometer: Float,
    val lastUpdate: Long
)
