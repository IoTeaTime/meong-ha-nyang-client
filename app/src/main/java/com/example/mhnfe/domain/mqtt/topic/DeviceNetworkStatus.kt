package com.example.mhnfe.domain.mqtt.topic

import kotlinx.serialization.Serializable

@Serializable
data class DeviceNetworkStatus(
    val SSID: String,
    val SignalStrength: Int
)
