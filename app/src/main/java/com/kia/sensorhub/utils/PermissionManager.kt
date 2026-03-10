package com.kia.sensorhub.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Permission Manager for handling runtime permissions
 */
class PermissionManager(private val activity: ComponentActivity) {

    private var singlePermissionCallbacks: Pair<() -> Unit, () -> Unit>? = null
    private var multiPermissionCallbacks: Pair<() -> Unit, (List<String>) -> Unit>? = null

    private val singlePermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val callbacks = singlePermissionCallbacks ?: return@registerForActivityResult
        singlePermissionCallbacks = null
        if (isGranted) callbacks.first() else callbacks.second()
    }

    private val multiPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val callbacks = multiPermissionCallbacks ?: return@registerForActivityResult
        multiPermissionCallbacks = null
        val denied = results.filterValues { granted -> !granted }.keys.toList()
        if (denied.isEmpty()) {
            callbacks.first()
        } else {
            callbacks.second(denied)
        }
    }
    
    /**
     * Location permissions
     */
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /**
     * Background location permission (request separately on API 29+)
     */
    val backgroundLocationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        emptyArray()
    }
    
    /**
     * Audio permissions
     */
    val audioPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )
    
    /**
     * Storage permissions by API level
     */
    val storagePermissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> emptyArray()
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    
    /**
     * Notification permissions (API 33+)
     */
    val notificationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }
    
    /**
     * Check if all permissions in array are granted
     */
    fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return hasForegroundLocationPermission()
    }

    /**
     * Check if foreground location permission is granted (fine OR coarse)
     */
    fun hasForegroundLocationPermission(): Boolean {
        return activity.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                activity.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    /**
     * Check if background location permission is granted
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasPermissions(backgroundLocationPermissions)
        } else {
            true
        }
    }
    
    /**
     * Check if audio permission is granted
     */
    fun hasAudioPermission(): Boolean {
        return hasPermissions(audioPermissions)
    }
    
    /**
     * Check if storage permissions are granted
     */
    fun hasStoragePermission(): Boolean {
        return storagePermissions.isEmpty() || hasPermissions(storagePermissions)
    }
    
    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermissions(notificationPermissions)
        } else {
            true // Not required before Android 13
        }
    }
    
    /**
     * Request single permission
     */
    fun requestPermission(
        permission: String,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (hasPermissions(arrayOf(permission))) {
            onGranted()
            return
        }

        singlePermissionCallbacks = onGranted to onDenied
        singlePermissionLauncher.launch(permission)
    }
    
    /**
     * Request multiple permissions
     */
    fun requestPermissions(
        permissions: Array<String>,
        onAllGranted: () -> Unit,
        onDenied: (List<String>) -> Unit
    ) {
        val normalizedPermissions = permissions
            .filter { it.isNotBlank() }
            .distinct()
            .toTypedArray()

        if (normalizedPermissions.isEmpty()) {
            onAllGranted()
            return
        }

        val denied = normalizedPermissions.filterNot { hasPermissions(arrayOf(it)) }
        if (denied.isEmpty()) {
            onAllGranted()
            return
        }

        multiPermissionCallbacks = onAllGranted to onDenied
        multiPermissionLauncher.launch(denied.toTypedArray())
    }
    
    /**
     * Check if permission should show rationale
     */
    fun shouldShowRationale(permission: String): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }
    
    /**
     * Get permission status description
     */
    fun getPermissionStatus(permission: String): PermissionStatus {
        return when {
            hasPermissions(arrayOf(permission)) -> PermissionStatus.GRANTED
            shouldShowRationale(permission) -> PermissionStatus.DENIED_SHOW_RATIONALE
            else -> PermissionStatus.DENIED_DONT_ASK_AGAIN
        }
    }
}

/**
 * Permission status enum
 */
enum class PermissionStatus {
    GRANTED,
    DENIED_SHOW_RATIONALE,
    DENIED_DONT_ASK_AGAIN
}

/**
 * Permission request result
 */
sealed class PermissionResult {
    object Granted : PermissionResult()
    data class Denied(val permissions: List<String>) : PermissionResult()
    data class PermanentlyDenied(val permissions: List<String>) : PermissionResult()
}

/**
 * Extension function to check permission
 */
fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Extension function to check multiple permissions
 */
fun Context.hasPermissions(vararg permissions: String): Boolean {
    return permissions.all { hasPermission(it) }
}

/**
 * Get required permissions for feature
 */
object RequiredPermissions {
    
    fun forGPS(): Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    fun forVoiceRecognition(): Array<String> = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )
    
    fun forNotifications(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    }
    
    fun forBackgroundLocation(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            emptyArray()
        }
    }
}

/**
 * Permission utility functions
 */
object PermissionUtils {
    
    /**
     * Get user-friendly permission name
     */
    fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION -> "Precise Location"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Approximate Location"
            Manifest.permission.ACCESS_BACKGROUND_LOCATION -> "Background Location"
            Manifest.permission.RECORD_AUDIO -> "Microphone"
            Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Storage (Read)"
            Manifest.permission.READ_MEDIA_IMAGES -> "Photos and Images"
            Manifest.permission.READ_MEDIA_VIDEO -> "Videos"
            Manifest.permission.READ_MEDIA_AUDIO -> "Music and Audio"
            Manifest.permission.CAMERA -> "Camera"
            else -> permission.substringAfterLast('.')
        }
    }
    
    /**
     * Get permission description
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION -> 
                "Required for GPS sensor functionality and location tracking"
            
            Manifest.permission.ACCESS_BACKGROUND_LOCATION -> 
                "Allows location tracking even when app is in background"
            
            Manifest.permission.RECORD_AUDIO -> 
                "Required for voice recognition feature"
            
            Manifest.permission.POST_NOTIFICATIONS -> 
                "Allows app to show sensor alerts and insights"
            
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO ->
                "Required for reading exported sensor files"
            
            else -> "Required for app functionality"
        }
    }
    
    /**
     * Check if permission is dangerous (requires runtime request)
     */
    fun isDangerousPermission(permission: String): Boolean {
        val dangerousPermissions = setOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.CALL_PHONE
        )
        
        return permission in dangerousPermissions ||
               (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                permission == Manifest.permission.POST_NOTIFICATIONS)
    }
}
