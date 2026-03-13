package com.kia.sensorhub.sensors

/**
 * Sensor information data class
 * Contains metadata about a hardware sensor
 */
data class SensorInfo(
    val name: String,
    val vendor: String,
    val version: Int,
    val type: Int,
    val maxRange: Float,
    val resolution: Float,
    val power: Float,
    val minDelay: Int,
    val maxDelay: Int = 0,
    val fifoMaxEventCount: Int = 0,
    val fifoReservedEventCount: Int = 0,
    val stringType: String = "",
    val isWakeUpSensor: Boolean = false,
    val isDynamicSensor: Boolean = false,
    val isAdditionalInfoSupported: Boolean = false
) {
    /**
     * Get human-readable sensor type name
     */
    fun getTypeName(): String {
        return when (type) {
            1 -> "Accelerometer"
            2 -> "Magnetic Field"
            3 -> "Orientation"
            4 -> "Gyroscope"
            5 -> "Light"
            6 -> "Pressure"
            7 -> "Temperature"
            8 -> "Proximity"
            9 -> "Gravity"
            10 -> "Linear Acceleration"
            11 -> "Rotation Vector"
            12 -> "Relative Humidity"
            13 -> "Ambient Temperature"
            else -> "Unknown ($type)"
        }
    }
    
    /**
     * Get maximum sampling frequency in Hz
     */
    fun getMaxFrequencyHz(): Float {
        return if (minDelay > 0) {
            1_000_000f / minDelay
        } else {
            0f
        }
    }
    
    /**
     * Get minimum sampling frequency in Hz
     */
    fun getMinFrequencyHz(): Float {
        return if (maxDelay > 0) {
            1_000_000f / maxDelay
        } else {
            0f
        }
    }
    
    /**
     * Format power consumption
     */
    fun getPowerConsumption(): String {
        return String.format("%.2f mA", power)
    }
    
    /**
     * Format resolution
     */
    fun getResolutionString(): String {
        return String.format("%.4f", resolution)
    }
    
    /**
     * Format max range
     */
    fun getMaxRangeString(): String {
        return String.format("%.2f", maxRange)
    }
    
    /**
     * Check if sensor supports high frequency sampling
     */
    fun supportsHighFrequency(): Boolean {
        return getMaxFrequencyHz() >= 100f
    }
    
    /**
     * Get sensor capabilities summary
     */
    fun getCapabilitiesSummary(): List<Pair<String, String>> {
        return listOf(
            "Type" to getTypeName(),
            "Vendor" to vendor,
            "Version" to version.toString(),
            "Max Range" to getMaxRangeString(),
            "Resolution" to getResolutionString(),
            "Power" to getPowerConsumption(),
            "Max Frequency" to "${String.format("%.1f", getMaxFrequencyHz())} Hz",
            "Min Frequency" to "${String.format("%.1f", getMinFrequencyHz())} Hz",
            "FIFO Size" to fifoMaxEventCount.toString(),
            "Wake-up" to if (isWakeUpSensor) "Yes" else "No",
            "Dynamic" to if (isDynamicSensor) "Yes" else "No"
        )
    }
}

/**
 * Extension function to create SensorInfo from Android Sensor
 */
fun android.hardware.Sensor.toSensorInfo(): SensorInfo {
    return SensorInfo(
        name = this.name,
        vendor = this.vendor,
        version = this.version,
        type = this.type,
        maxRange = this.maximumRange,
        resolution = this.resolution,
        power = this.power,
        minDelay = this.minDelay,
        maxDelay = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            this.maxDelay
        } else {
            0
        },
        fifoMaxEventCount = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            this.fifoMaxEventCount
        } else {
            0
        },
        fifoReservedEventCount = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            this.fifoReservedEventCount
        } else {
            0
        },
        stringType = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            this.stringType
        } else {
            ""
        },
        isWakeUpSensor = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            this.isWakeUpSensor
        } else {
            false
        },
        isDynamicSensor = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            this.isDynamicSensor
        } else {
            false
        },
        isAdditionalInfoSupported = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            this.isAdditionalInfoSupported
        } else {
            false
        }
    )
}
