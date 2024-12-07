package com.example.mhnfe.domain.mqtt.shadow

import kotlinx.serialization.Serializable

@Serializable
data class ReportedData(
    val batteryLevel: Int? = null,
    val networkStatus: Int? = null
)