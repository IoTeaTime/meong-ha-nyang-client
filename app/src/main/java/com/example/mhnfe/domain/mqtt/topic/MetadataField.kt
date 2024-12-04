package com.example.mhnfe.domain.mqtt.topic

import kotlinx.serialization.Serializable

@Serializable
data class MetadataField(
    val timestamp: Long? = null
)