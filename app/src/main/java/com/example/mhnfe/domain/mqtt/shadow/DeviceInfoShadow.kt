package com.example.mhnfe.domain.mqtt.shadow

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfoShadow(
    val state: State? = null,
    val metadata: Metadata? = null,
    val version: Int? = null,
    val timestamp: Long? = null
)
