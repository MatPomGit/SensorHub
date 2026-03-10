package com.kia.sensorhub.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.kia.sensorhub.data.model.GpsData
import com.google.android.gms.location.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Manager for GPS/Location services
 * Provides location data using Google Play Services
 */
class GpsManager(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if location services are available
     */
    fun isAvailable(): Boolean {
        return hasLocationPermission()
    }
    
    /**
     * Get location updates as a Flow
     */
    fun getLocationFlow(
        updateIntervalMs: Long = 1000L,
        fastestIntervalMs: Long = 500L
    ): Flow<GpsData> = callbackFlow {
        
        if (!hasLocationPermission()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            updateIntervalMs
        ).apply {
            setMinUpdateIntervalMillis(fastestIntervalMs)
            setWaitForAccurateLocation(false)
        }.build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val data = GpsData(
                        timestamp = System.currentTimeMillis(),
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitude = location.altitude,
                        speed = location.speed,
                        accuracy = location.accuracy,
                        bearing = location.bearing
                    )
                    trySend(data)
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
        }
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    /**
     * Get last known location (single reading)
     */
    suspend fun getLastLocation(): GpsData? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return try {
            val location: Location? = fusedLocationClient.lastLocation.await()
            location?.let {
                GpsData(
                    timestamp = System.currentTimeMillis(),
                    latitude = it.latitude,
                    longitude = it.longitude,
                    altitude = it.altitude,
                    speed = it.speed,
                    accuracy = it.accuracy,
                    bearing = it.bearing
                )
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Calculate distance between two GPS points (in meters)
     * Uses Android's [Location.distanceBetween] utility.
     */
    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
    
    /**
     * Format coordinates for display
     */
    fun formatCoordinates(latitude: Double, longitude: Double): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        
        return "${String.format("%.6f", kotlin.math.abs(latitude))}° $latDir, " +
               "${String.format("%.6f", kotlin.math.abs(longitude))}° $lonDir"
    }
    
    /**
     * Get accuracy description
     */
    fun getAccuracyDescription(accuracy: Float): String {
        return when {
            accuracy < 10 -> "Excellent"
            accuracy < 20 -> "Good"
            accuracy < 50 -> "Moderate"
            accuracy < 100 -> "Poor"
            else -> "Very Poor"
        }
    }
}
