package com.example.supabasedemo.data.network

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import kotlin.math.absoluteValue

object SensorManagerSingleton {
    private var sensorManager: SensorManager? = null

    private var isStarted: Boolean = false
    private val _isStartedFlow = MutableStateFlow(false)

    private val _linearAccelerometerReadingsFlow = MutableStateFlow(listOf(Reading(0F, 0F, 0F)))
    val linearAccelerometerReadingsFlow: StateFlow<List<Reading>> get() = _linearAccelerometerReadingsFlow

    private val _accelerometerReadingsFlow = MutableStateFlow(listOf(Reading(0F, 0F, 0F)))
    val accelerometerReadingsFlow: StateFlow<List<Reading>> get() = _accelerometerReadingsFlow

    private val _gyroscopeReadingsFlow = MutableStateFlow(listOf(Reading(0F, 0F, 0F)))
    val gyroscopeReadingsFlow: StateFlow<List<Reading>> get() = _gyroscopeReadingsFlow

    private val _magnetometerReadingsFlow = MutableStateFlow(listOf(Reading(0F, 0F, 0F)))
    val magnetometerReadingsFlow: StateFlow<List<Reading>> get() = _magnetometerReadingsFlow

    private val _gravityReadingsFlow = MutableStateFlow(listOf(Reading(0F, 0F, 0F)))
    val gravityReadingsFlow: StateFlow<List<Reading>> get() = _gravityReadingsFlow

    private val _compassReadingsFlow = MutableStateFlow(listOf(0F))
    val compassReadingsFlow: StateFlow<List<Float>> get() = _compassReadingsFlow

    private var initializationDeferred: CompletableDeferred<Unit>? = null

    fun initialize(context: Context) {
        if (isStarted || _isStartedFlow.value) return
        sensorManager = getSystemService(context, SensorManager::class.java) as SensorManager
        initializationDeferred = CompletableDeferred()

        try {
            sensorManager!!.registerGravity()
            sensorManager!!.registerLinearAccelerometer()
            sensorManager!!.registerAccelerometer()
            sensorManager!!.registerGyroscope()
            sensorManager!!.registerMagnetometer()
            sensorManager!!.registerOrientation()


            initializationDeferred?.complete(Unit)
            isStarted = true
            _isStartedFlow.value = true
        } catch (e: Exception) {
            initializationDeferred?.completeExceptionally(e)
        }
    }

    private suspend fun waitForInitialization() {
        initializationDeferred?.await()
    }

    private fun SensorManager.registerLinearAccelerometer() {
        if (sensorManager == null) throw Exception()
        val linearAccelerometer: Sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) as Sensor

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val reading = Reading(event.values[0], event.values[1], event.values[2])
                _linearAccelerometerReadingsFlow.value += reading
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        this.registerListener(
            sensorEventListener,
            linearAccelerometer,
            50000
        )
    }
    private fun SensorManager.registerAccelerometer() {
        if (sensorManager == null) throw Exception()
        val accelerometer: Sensor? = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val reading = Reading(event.values[0], event.values[1], event.values[2])
                _accelerometerReadingsFlow.value += reading
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        this.registerListener(
            sensorEventListener,
            accelerometer,
            50000
        )
    }
    private fun SensorManager.registerGyroscope() {
        if (sensorManager == null) throw Exception()
        val gyroscope: Sensor? = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val reading = Reading(event.values[0], event.values[1], event.values[2])
                _gyroscopeReadingsFlow.value += reading
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        this.registerListener(
            sensorEventListener,
            gyroscope,
            50000
        )
    }
    private fun SensorManager.registerMagnetometer() {
        if (sensorManager == null) throw Exception()
        val magnetometer: Sensor? = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val reading = Reading(event.values[0], event.values[1], event.values[2])
                _magnetometerReadingsFlow.value += reading
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        this.registerListener(
            sensorEventListener,
            magnetometer,
            50000
        )
    }
    private fun SensorManager.registerOrientation() {
        if (sensorManager == null) throw Exception()
        @Suppress("DEPRECATION")
        val orientation: Sensor? = sensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                _compassReadingsFlow.value += event.values[0]
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        this.registerListener(
            sensorEventListener,
            orientation,
            50000
        )
    }
    private fun SensorManager.registerGravity() {
        if (sensorManager == null) throw Exception()
        val gravity: Sensor? = sensorManager!!.getDefaultSensor(Sensor.TYPE_GRAVITY)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val reading = Reading(event.values[0], event.values[1], event.values[2])
                _gravityReadingsFlow.value += reading
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        this.registerListener(
            sensorEventListener,
            gravity,
            50000
        )
    }
}

class Reading(
    var x: Float,
    var y: Float,
    var z: Float,
)

fun Float.fixForScreen(): String {
    return if (this < 0) {
        String.format(Locale.getDefault(), "%.3f", this)
    }
    else {
        " " + String.format(Locale.getDefault(), "%.3f", this)
    }
}

fun List<Reading>.avg(): Reading {
    var value: Reading = Reading(0F, 0F, 0F)
    for (aa in this) {
        value.x += aa.x
        value.y += aa.y
        value.z += aa.z
    }
    value = Reading(
        value.x / this.size,
        value.y / this.size,
        value.z / this.size
    )
    return value
}

fun percentageBetween(a: Float, b: Float, x: Float): Float {
    return (x - a) / (b - a)
}

fun getForwardAcceleration(): Float {
    var gravity: Reading = SensorManagerSingleton.gravityReadingsFlow.value.last()
    var acceleration: Reading = SensorManagerSingleton.accelerometerReadingsFlow.value.takeLast(20).avg()

    var movement: Reading = Reading(
        acceleration.x - gravity.x,
        acceleration.y - gravity.y,
        acceleration.z - gravity.z
    )

    var percentX = percentageBetween(0F, 9.81F, gravity.x.absoluteValue)
    var percentZ = percentageBetween(0F, 9.81F, gravity.z.absoluteValue)

    var valueX = movement.x * (1 - percentX)
    var valueZ = movement.z * percentZ

    return valueZ + valueX
}