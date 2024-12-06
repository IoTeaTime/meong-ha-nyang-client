package com.example.mhnfe.domain.mqtt.topic

import kotlinx.serialization.Serializable


@Serializable
data class ReportedThingInfo(
    val batteryLevel: Int,
    val availableMemory: Int,
    val deviceModel: String,
    val osVersion: String,
    val appVersion: String,
    val networkStatus: Int
)