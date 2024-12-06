package com.example.mhnfe.domain.mqtt.shadow

import kotlinx.serialization.Serializable

@Serializable
data class State(
    val reported: ReportedData? = null
)
