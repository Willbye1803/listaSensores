package com.example.listasensores.viewmodel

import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listasensores.model.SensorData
import kotlinx.coroutines.launch

class SensorViewModel : ViewModel() {
    private val _sensors = mutableStateListOf<SensorData>()
    val sensors: List<SensorData> = _sensors

    private var cameraManager: CameraManager? = null
    private var flashlightCallback: CameraManager.TorchCallback? = null

    fun loadSensors(context: Context) {
        viewModelScope.launch {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
            _sensors.clear()

            // Agregar sensores tradicionales
            _sensors.addAll(deviceSensors.map {
                SensorData(
                    name = it.name,
                    type = it.type,
                    vendor = it.vendor,
                    version = it.version,
                    maxRange = it.maximumRange,
                    power = it.power,
                    resolution = it.resolution
                )
            })

            // Agregar linterna como sensor dinámico
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            _sensors.add(
                SensorData(
                    name = "Linterna (Flash)",
                    isDynamic = true,
                    dynamicValue = false // Estado inicial apagado
                )
            )

            // Agregar nivel de batería como sensor dinámico
            val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            _sensors.add(
                SensorData(
                    name = "Nivel de Batería",
                    isDynamic = true,
                    dynamicValue = "$batteryLevel%"
                )
            )

            // Iniciar monitoreo de linterna
            startFlashlightMonitoring()
        }
    }

    private fun startFlashlightMonitoring() {
        flashlightCallback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                super.onTorchModeChanged(cameraId, enabled)
                viewModelScope.launch {
                    val index = _sensors.indexOfFirst { it.name == "Linterna (Flash)" }
                    if (index != -1) {
                        val oldSensor = _sensors[index]
                        _sensors[index] = oldSensor.copy(dynamicValue = enabled)
                    }
                }
            }
        }
        cameraManager?.registerTorchCallback(flashlightCallback!!, null)
    }

    fun toggleFlashlight() {
        viewModelScope.launch {
            cameraManager?.cameraIdList?.firstOrNull()?.let { cameraId ->
                try {
                    val flashlightSensor = _sensors.find { it.name == "Linterna (Flash)" }
                    val isEnabled = flashlightSensor?.dynamicValue == true
                    cameraManager?.setTorchMode(cameraId, !isEnabled)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateBatteryLevel(context: Context) {
        viewModelScope.launch {
            val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val index = _sensors.indexOfFirst { it.name == "Nivel de Batería" }
            if (index != -1) {
                val oldSensor = _sensors[index]
                _sensors[index] = oldSensor.copy(dynamicValue = "$batteryLevel%")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        flashlightCallback?.let { cameraManager?.unregisterTorchCallback(it) }
    }
}