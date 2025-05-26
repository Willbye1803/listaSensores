package com.example.listasensores.model

data class SensorData(
    val name: String,
    val type: Int? = null,
    val vendor: String? = null,
    val version: Int? = null,
    val maxRange: Float? = null,
    val power: Float? = null,
    val resolution: Float? = null,
    val isDynamic: Boolean = false,
    val dynamicValue: Any? = null
)