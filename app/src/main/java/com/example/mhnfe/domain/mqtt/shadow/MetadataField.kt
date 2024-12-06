package com.example.mhnfe.domain.mqtt.shadow

import kotlinx.serialization.Serializable

@Serializable
data class MetadataField(
    val timestamp: Long? = null
)