package com.example.mhnfe.domain.mqtt.shadow

import kotlinx.serialization.Serializable

@Serializable
data class DeviceNetworkStatus(
    val SSID: String,
    val SignalStrength: Int
)
