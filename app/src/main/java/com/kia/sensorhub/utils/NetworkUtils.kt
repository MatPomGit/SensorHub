package com.kia.sensorhub.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Network Utilities
 * Helper functions for network connectivity
 */
object NetworkUtils {
    
    /**
     * Check if device is connected to internet
     */
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected == true
        }
    }
    
    /**
     * Check if connected via WiFi
     */
    fun isWiFiConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
        }
    }
    
    /**
     * Check if connected via cellular
     */
    fun isCellularConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE
        }
    }
    
    /**
     * Check if metered connection (cellular or metered WiFi)
     */
    fun isMeteredConnection(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        } else {
            @Suppress("DEPRECATION")
            cm.isActiveNetworkMetered
        }
    }
    
    /**
     * Get network type as string
     */
    fun getNetworkType(context: Context): NetworkType {
        if (!isConnected(context)) return NetworkType.NONE
        
        return when {
            isWiFiConnected(context) -> NetworkType.WIFI
            isCellularConnected(context) -> NetworkType.CELLULAR
            else -> NetworkType.OTHER
        }
    }
    
    /**
     * Observe network connectivity changes
     */
    fun observeNetworkConnectivity(context: Context): Flow<NetworkState> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkState.Available(getNetworkType(context)))
            }
            
            override fun onLost(network: Network) {
                trySend(NetworkState.Lost)
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val isMetered = !networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_NOT_METERED
                )
                trySend(
                    NetworkState.CapabilitiesChanged(
                        type = getNetworkType(context),
                        isMetered = isMetered
                    )
                )
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        cm.registerNetworkCallback(request, callback)
        
        // Send initial state
        trySend(
            if (isConnected(context)) {
                NetworkState.Available(getNetworkType(context))
            } else {
                NetworkState.Lost
            }
        )
        
        awaitClose {
            cm.unregisterNetworkCallback(callback)
        }
    }
}

/**
 * Network type enumeration
 */
enum class NetworkType {
    NONE,
    WIFI,
    CELLULAR,
    OTHER
}

/**
 * Network state
 */
sealed class NetworkState {
    data class Available(val type: NetworkType) : NetworkState()
    object Lost : NetworkState()
    data class CapabilitiesChanged(
        val type: NetworkType,
        val isMetered: Boolean
    ) : NetworkState()
}

/**
 * Network Request Helper
 * Utilities for API requests (future cloud sync)
 */
object NetworkRequestHelper {
    
    /**
     * Check if should sync based on connection type
     */
    fun shouldSync(
        context: Context,
        requireWifi: Boolean = false,
        allowMetered: Boolean = false
    ): Boolean {
        if (!NetworkUtils.isConnected(context)) return false
        
        if (requireWifi && !NetworkUtils.isWiFiConnected(context)) return false
        
        if (!allowMetered && NetworkUtils.isMeteredConnection(context)) return false
        
        return true
    }
    
    /**
     * Get retry delay based on attempt count (exponential backoff)
     */
    fun getRetryDelay(attemptCount: Int, baseDelayMs: Long = 1000): Long {
        val maxDelay = 60000L // Max 1 minute
        val delay = baseDelayMs * Math.pow(2.0, attemptCount.toDouble()).toLong()
        return minOf(delay, maxDelay)
    }
    
    /**
     * Check if should retry request
     */
    fun shouldRetry(
        attemptCount: Int,
        maxAttempts: Int = 3,
        exception: Exception? = null
    ): Boolean {
        if (attemptCount >= maxAttempts) return false
        
        // Check if exception is retryable
        exception?.let {
            return when (it) {
                is java.net.SocketTimeoutException -> true
                is java.net.UnknownHostException -> false // Don't retry DNS failures
                is javax.net.ssl.SSLException -> false // Don't retry SSL failures
                else -> true
            }
        }
        
        return true
    }
}

/**
 * API Response wrapper
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
    object NetworkError : ApiResult<Nothing>()
}

/**
 * Network Monitor
 * Monitors network state and quality
 */
class NetworkMonitor(private val context: Context) {
    
    private var isMonitoring = false
    
    /**
     * Start monitoring network
     */
    fun startMonitoring(
        onConnected: (NetworkType) -> Unit,
        onDisconnected: () -> Unit
    ) {
        isMonitoring = true
        
        // TODO: Implement actual monitoring
        // Would use ConnectivityManager.NetworkCallback
    }
    
    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        isMonitoring = false
    }
    
    /**
     * Get current network quality
     */
    fun getNetworkQuality(): NetworkQuality {
        return when {
            !NetworkUtils.isConnected(context) -> NetworkQuality.NO_CONNECTION
            NetworkUtils.isWiFiConnected(context) -> NetworkQuality.EXCELLENT
            NetworkUtils.isCellularConnected(context) -> NetworkQuality.GOOD
            else -> NetworkQuality.FAIR
        }
    }
    
    enum class NetworkQuality {
        NO_CONNECTION,
        POOR,
        FAIR,
        GOOD,
        EXCELLENT
    }
}

/**
 * Download/Upload speed estimator
 */
object NetworkSpeedEstimator {
    
    /**
     * Estimate download speed (simplified)
     */
    fun estimateDownloadSpeed(context: Context): NetworkSpeed {
        return when {
            NetworkUtils.isWiFiConnected(context) -> NetworkSpeed.FAST
            NetworkUtils.isCellularConnected(context) -> NetworkSpeed.MODERATE
            else -> NetworkSpeed.SLOW
        }
    }
    
    enum class NetworkSpeed {
        SLOW,      // < 1 Mbps
        MODERATE,  // 1-5 Mbps
        FAST,      // > 5 Mbps
        UNKNOWN
    }
}

/**
 * Offline Mode Manager
 */
class OfflineModeManager(private val context: Context) {
    
    private val prefs = context.getSharedPreferences("network_prefs", Context.MODE_PRIVATE)
    
    /**
     * Enable offline mode
     */
    fun enableOfflineMode() {
        prefs.edit().putBoolean("offline_mode", true).apply()
    }
    
    /**
     * Disable offline mode
     */
    fun disableOfflineMode() {
        prefs.edit().putBoolean("offline_mode", false).apply()
    }
    
    /**
     * Check if offline mode is enabled
     */
    fun isOfflineModeEnabled(): Boolean {
        return prefs.getBoolean("offline_mode", false)
    }
    
    /**
     * Check if can perform network operation
     */
    fun canPerformNetworkOperation(): Boolean {
        return !isOfflineModeEnabled() && NetworkUtils.isConnected(context)
    }
}
