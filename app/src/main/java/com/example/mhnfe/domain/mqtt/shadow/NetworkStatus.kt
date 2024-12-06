package com.example.mhnfe.domain.mqtt.shadow

import kotlinx.serialization.Serializable

@Serializable
data class NetworkStatus(
    val SignalStrength: Int? = null
)