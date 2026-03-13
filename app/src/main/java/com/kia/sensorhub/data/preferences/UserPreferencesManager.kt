package com.kia.sensorhub.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension to create DataStore instance
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sensor_hub_preferences"
)

/**
 * User Preferences Manager using DataStore
 */
@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    // Preference keys
    private object PreferenceKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val SAMPLING_RATE = intPreferencesKey("sampling_rate")
        val BATTERY_OPTIMIZATION = booleanPreferencesKey("battery_optimization")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DAILY_INSIGHTS = booleanPreferencesKey("daily_insights")
        val ACHIEVEMENT_ALERTS = booleanPreferencesKey("achievement_alerts")
        val ANALYTICS = booleanPreferencesKey("analytics")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_LEVEL = intPreferencesKey("user_level")
        val TOTAL_XP = intPreferencesKey("total_xp")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LAST_ACTIVE_DATE = longPreferencesKey("last_active_date")
    }
    
    /**
     * User preferences data class
     */
    data class UserPreferences(
        val isDarkMode: Boolean = false,
        val useDynamicColors: Boolean = true,
        val autoSave: Boolean = true,
        val samplingRate: Int = 2,
        val batteryOptimization: Boolean = false,
        val notificationsEnabled: Boolean = true,
        val dailyInsights: Boolean = true,
        val achievementAlerts: Boolean = true,
        val analytics: Boolean = false,
        val onboardingCompleted: Boolean = false,
        val userLevel: Int = 1,
        val totalXp: Int = 0,
        val currentStreak: Int = 0,
        val lastActiveDate: Long = 0L
    )
    
    /**
     * Get user preferences as Flow
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                isDarkMode = preferences[PreferenceKeys.DARK_MODE] ?: false,
                useDynamicColors = preferences[PreferenceKeys.DYNAMIC_COLORS] ?: true,
                autoSave = preferences[PreferenceKeys.AUTO_SAVE] ?: true,
                samplingRate = preferences[PreferenceKeys.SAMPLING_RATE] ?: 2,
                batteryOptimization = preferences[PreferenceKeys.BATTERY_OPTIMIZATION] ?: false,
                notificationsEnabled = preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true,
                dailyInsights = preferences[PreferenceKeys.DAILY_INSIGHTS] ?: true,
                achievementAlerts = preferences[PreferenceKeys.ACHIEVEMENT_ALERTS] ?: true,
                analytics = preferences[PreferenceKeys.ANALYTICS] ?: false,
                onboardingCompleted = preferences[PreferenceKeys.ONBOARDING_COMPLETED] ?: false,
                userLevel = preferences[PreferenceKeys.USER_LEVEL] ?: 1,
                totalXp = preferences[PreferenceKeys.TOTAL_XP] ?: 0,
                currentStreak = preferences[PreferenceKeys.CURRENT_STREAK] ?: 0,
                lastActiveDate = preferences[PreferenceKeys.LAST_ACTIVE_DATE] ?: 0L
            )
        }
    
    // Individual preference update methods
    
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.DARK_MODE] = enabled
        }
    }
    
    suspend fun setDynamicColors(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.DYNAMIC_COLORS] = enabled
        }
    }
    
    suspend fun setAutoSave(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.AUTO_SAVE] = enabled
        }
    }
    
    suspend fun setSamplingRate(rate: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.SAMPLING_RATE] = rate
        }
    }
    
    suspend fun setBatteryOptimization(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.BATTERY_OPTIMIZATION] = enabled
        }
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    suspend fun setDailyInsights(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.DAILY_INSIGHTS] = enabled
        }
    }
    
    suspend fun setAchievementAlerts(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.ACHIEVEMENT_ALERTS] = enabled
        }
    }
    
    suspend fun setAnalytics(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.ANALYTICS] = enabled
        }
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETED] = completed
        }
    }
    
    suspend fun setUserLevel(level: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.USER_LEVEL] = level
        }
    }
    
    suspend fun setTotalXp(xp: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.TOTAL_XP] = xp
        }
    }
    
    suspend fun addXp(xp: Int) {
        dataStore.edit { preferences ->
            val currentXp = preferences[PreferenceKeys.TOTAL_XP] ?: 0
            preferences[PreferenceKeys.TOTAL_XP] = currentXp + xp
        }
    }
    
    suspend fun setCurrentStreak(streak: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.CURRENT_STREAK] = streak
        }
    }
    
    suspend fun updateStreak() {
        dataStore.edit { preferences ->
            val lastActiveDate = preferences[PreferenceKeys.LAST_ACTIVE_DATE] ?: 0L
            val currentDate = System.currentTimeMillis()
            
            // Check if it's a new day
            val lastDay = lastActiveDate / (24 * 60 * 60 * 1000)
            val currentDay = currentDate / (24 * 60 * 60 * 1000)
            
            if (currentDay > lastDay) {
                // New day
                if (currentDay == lastDay + 1) {
                    // Consecutive day - increase streak
                    val currentStreak = preferences[PreferenceKeys.CURRENT_STREAK] ?: 0
                    preferences[PreferenceKeys.CURRENT_STREAK] = currentStreak + 1
                } else {
                    // Streak broken - reset to 1
                    preferences[PreferenceKeys.CURRENT_STREAK] = 1
                }
                preferences[PreferenceKeys.LAST_ACTIVE_DATE] = currentDate
            }
        }
    }
    
    /**
     * Reset all preferences to defaults
     */
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Clear specific preference
     */
    suspend fun <T> clearPreference(key: Preferences.Key<T>) {
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}

/**
 * Achievement data storage
 */
@Singleton
class AchievementDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    /**
     * Save unlocked achievement
     */
    suspend fun unlockAchievement(achievementId: String) {
        val key = stringPreferencesKey("achievement_$achievementId")
        dataStore.edit { preferences ->
            preferences[key] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Check if achievement is unlocked
     */
    fun isAchievementUnlocked(achievementId: String): Flow<Boolean> {
        val key = stringPreferencesKey("achievement_$achievementId")
        return dataStore.data.map { preferences ->
            preferences[key] != null
        }
    }
    
    /**
     * Get all unlocked achievements
     */
    fun getAllUnlockedAchievements(): Flow<Set<String>> {
        return dataStore.data.map { preferences ->
            preferences.asMap().keys
                .filter { it.name.startsWith("achievement_") }
                .map { it.name.removePrefix("achievement_") }
                .toSet()
        }
    }
    
    /**
     * Clear all achievements (for testing)
     */
    suspend fun clearAllAchievements() {
        dataStore.edit { preferences ->
            val achievementKeys = preferences.asMap().keys
                .filter { it.name.startsWith("achievement_") }
            
            achievementKeys.forEach { key ->
                preferences.remove(key)
            }
        }
    }
}

/**
 * Sensor configuration storage
 */
@Singleton
class SensorConfigStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    /**
     * Save sensor enabled state
     */
    suspend fun setSensorEnabled(sensorType: String, enabled: Boolean) {
        val key = booleanPreferencesKey("sensor_enabled_$sensorType")
        dataStore.edit { preferences ->
            preferences[key] = enabled
        }
    }
    
    /**
     * Get sensor enabled state
     */
    fun isSensorEnabled(sensorType: String): Flow<Boolean> {
        val key = booleanPreferencesKey("sensor_enabled_$sensorType")
        return dataStore.data.map { preferences ->
            preferences[key] ?: true // Default: enabled
        }
    }
    
    /**
     * Save sensor sampling rate
     */
    suspend fun setSensorSamplingRate(sensorType: String, rate: Int) {
        val key = intPreferencesKey("sensor_rate_$sensorType")
        dataStore.edit { preferences ->
            preferences[key] = rate
        }
    }
    
    /**
     * Get sensor sampling rate
     */
    fun getSensorSamplingRate(sensorType: String): Flow<Int> {
        val key = intPreferencesKey("sensor_rate_$sensorType")
        return dataStore.data.map { preferences ->
            preferences[key] ?: 2 // Default: NORMAL
        }
    }
}
