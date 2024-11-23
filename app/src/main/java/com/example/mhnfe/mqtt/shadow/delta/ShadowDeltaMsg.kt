package com.example.mhnfe.mqtt.shadow.delta

import kotlinx.serialization.Serializable

@Serializable
data class ShadowDeltaMsg(
    val state: ShadowDeltaState)
