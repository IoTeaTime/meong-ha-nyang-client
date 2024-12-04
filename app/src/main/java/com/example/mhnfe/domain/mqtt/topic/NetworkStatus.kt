package com.example.mhnfe.domain.mqtt.topic

import kotlinx.serialization.Serializable

@Serializable
data class NetworkStatus(
    val SignalStrength: Int? = null
)