package com.example.mhnfe.domain.mqtt.shadow.delta

import kotlinx.serialization.Serializable

@Serializable
data class ShadowContent(
    val batteryLevel: Int? = null,
    val kvsChannelActive: Boolean? = null,
    val kvsChannelDeleteRequested: Boolean? = null,
    val networkStatus: Int? = null,
    val isBackCamera: Boolean? = null
 )
