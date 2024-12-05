package com.example.mhnfe.domain.mqtt.topic

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfoTopic(
    val state: State? = null,
    val metadata: Metadata? = null,
    val version: Int? = null,
    val timestamp: Long? = null
)
