package com.kia.sensorhub.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kia.sensorhub.MainActivity
import com.kia.sensorhub.R

/**
 * Notification Manager for SensorHub
 * Handles all app notifications including sensor alerts and insights
 */
class SensorNotificationManager(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID_ALERTS = "sensor_alerts"
        private const val CHANNEL_ID_INSIGHTS = "sensor_insights"
        private const val CHANNEL_ID_ACHIEVEMENTS = "achievements"
        private const val CHANNEL_ID_MONITORING = "monitoring"
        
        private const val NOTIFICATION_ID_SENSOR_ALERT = 100
        private const val NOTIFICATION_ID_INSIGHT = 200
        private const val NOTIFICATION_ID_ACHIEVEMENT = 300
        private const val NOTIFICATION_ID_MONITORING = 400
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Create notification channels for Android O+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    "Sensor Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Critical sensor alerts and warnings"
                    enableVibration(true)
                    enableLights(true)
                },
                
                NotificationChannel(
                    CHANNEL_ID_INSIGHTS,
                    "Sensor Insights",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily insights and analysis"
                },
                
                NotificationChannel(
                    CHANNEL_ID_ACHIEVEMENTS,
                    "Achievements",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Achievement unlocks and progress"
                },
                
                NotificationChannel(
                    CHANNEL_ID_MONITORING,
                    "Background Monitoring",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Ongoing sensor monitoring status"
                    setShowBadge(false)
                }
            )
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
    
    /**
     * Show sensor alert notification
     */
    fun showSensorAlert(
        title: String,
        message: String,
        sensorType: String,
        priority: AlertPriority = AlertPriority.NORMAL
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("sensor_type", sensorType)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(getAlertIcon(priority))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority.toPriority())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_SENSOR_ALERT, notification)
    }
    
    /**
     * Show daily insight notification
     */
    fun showDailyInsight(
        title: String,
        message: String,
        insights: List<String>
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "dashboard")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
        
        insights.forEach { inboxStyle.addLine("• $it") }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_INSIGHTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(inboxStyle)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_INSIGHT, notification)
    }
    
    /**
     * Show achievement unlocked notification
     */
    fun showAchievementUnlocked(
        achievementName: String,
        description: String,
        xpReward: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "achievements")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ACHIEVEMENTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🏆 Achievement Unlocked!")
            .setContentText(achievementName)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$achievementName\n\n$description\n\n+$xpReward XP")
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_ACHIEVEMENT, notification)
    }
    
    /**
     * Show monitoring status notification (foreground service)
     */
    fun showMonitoringNotification(
        sensorsActive: Int,
        duration: String
    ): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, CHANNEL_ID_MONITORING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SensorHub Monitoring")
            .setContentText("$sensorsActive sensor(s) active • $duration")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    /**
     * Show emotion analysis notification
     */
    fun showEmotionAnalysis(
        emotion: String,
        confidence: Int,
        recommendation: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "affective")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            4,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_INSIGHTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Emotional State: $emotion")
            .setContentText("$confidence% confidence")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Current Emotion: $emotion\nConfidence: $confidence%\n\n💡 $recommendation")
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_INSIGHT + 1, notification)
    }
    
    /**
     * Show daily challenge notification
     */
    fun showDailyChallengeReminder(
        challengesCompleted: Int,
        challengesTotal: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "challenges")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            5,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_INSIGHTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Daily Challenges")
            .setContentText("$challengesCompleted of $challengesTotal completed")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        if (challengesCompleted < challengesTotal) {
            notificationManager.notify(NOTIFICATION_ID_INSIGHT + 2, notification)
        }
    }
    
    /**
     * Show data export complete notification
     */
    fun showExportComplete(
        fileName: String,
        fileSize: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_INSIGHTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Export Complete")
            .setContentText("$fileName ($fileSize)")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_INSIGHT + 3, notification)
    }
    
    /**
     * Cancel specific notification
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAll() {
        notificationManager.cancelAll()
    }
    
    /**
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
    
    private fun getAlertIcon(priority: AlertPriority): Int {
        return when (priority) {
            AlertPriority.CRITICAL -> android.R.drawable.ic_dialog_alert
            AlertPriority.HIGH -> android.R.drawable.stat_notify_error
            else -> R.drawable.ic_launcher_foreground
        }
    }
}

/**
 * Alert priority levels
 */
enum class AlertPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL;
    
    fun toPriority(): Int {
        return when (this) {
            LOW -> NotificationCompat.PRIORITY_LOW
            NORMAL -> NotificationCompat.PRIORITY_DEFAULT
            HIGH -> NotificationCompat.PRIORITY_HIGH
            CRITICAL -> NotificationCompat.PRIORITY_MAX
        }
    }
}

/**
 * Notification scheduler for periodic notifications
 */
class NotificationScheduler(private val context: Context) {
    
    /**
     * Schedule daily insight notification
     */
    fun scheduleDailyInsight(hourOfDay: Int, minute: Int) {
        // TODO: Implement using AlarmManager or WorkManager
        // This would schedule a daily notification at specified time
    }
    
    /**
     * Schedule challenge reminder
     */
    fun scheduleChallengeReminder(delayMinutes: Long) {
        // TODO: Implement using WorkManager
        // Schedule one-time reminder after delay
    }
    
    /**
     * Cancel all scheduled notifications
     */
    fun cancelAllScheduled() {
        // TODO: Cancel all pending alarms/work
    }
}

/**
 * Notification helper for easy access
 */
object NotificationHelper {
    
    private var notificationManager: SensorNotificationManager? = null
    
    fun initialize(context: Context) {
        notificationManager = SensorNotificationManager(context.applicationContext)
    }
    
    fun getInstance(): SensorNotificationManager {
        return notificationManager ?: throw IllegalStateException(
            "NotificationHelper not initialized. Call initialize() first."
        )
    }
}
