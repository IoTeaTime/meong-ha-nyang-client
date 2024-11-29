package com.example.mhnfe.domain.mqtt.shadow.delta

import kotlinx.serialization.Serializable

@Serializable
data class ShadowContent(
    val batteryLevel: Long? = null,
    val webRTCActive: Boolean? = null,
    val kvsChannelDeleteRequested: Boolean? = null,
    val isBackCamera: Boolean? = null,
    val flashActivate: Boolean? = null
 )
