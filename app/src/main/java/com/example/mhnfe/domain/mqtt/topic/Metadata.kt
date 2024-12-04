package com.example.mhnfe.domain.mqtt.topic

import kotlinx.serialization.Serializable

@Serializable
data class Metadata(
    val reported: Map<String, MetadataField>? = null
)
