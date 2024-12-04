package com.example.mhnfe.domain.mqtt.topic

import kotlinx.serialization.Serializable

@Serializable
data class ReportedData(
    val batteryLevel: Int? = null,
    val networkStatus: NetworkStatus? = null
)