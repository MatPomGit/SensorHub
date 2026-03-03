package com.kia.sensorhub.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.kia.sensorhub.data.model.LightData
import com.kia.sensorhub.data.model.ProximityData
import com.kia.sensorhub.data.model.BarometerData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.pow

// This file is now empty to avoid redeclarations. Managers are moved to AdditionalSensorManagers.kt
