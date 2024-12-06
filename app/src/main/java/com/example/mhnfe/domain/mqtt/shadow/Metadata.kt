package com.example.mhnfe.domain.mqtt.shadow

import kotlinx.serialization.Serializable

@Serializable
data class Metadata(
    val reported: Map<String, MetadataField>? = null
)
