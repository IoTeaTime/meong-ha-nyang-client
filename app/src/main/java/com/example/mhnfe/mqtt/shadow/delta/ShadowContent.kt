package com.example.mhnfe.mqtt.shadow.delta

import kotlinx.serialization.Serializable

@Serializable
data class ShadowContent(
    val batteryLevel: Long,
    val webRTCActive: Boolean,
    val kvsChannelDeleteRequested: Boolean,
    val isBackCamera: Boolean,
    val flashActivate: Boolean)
