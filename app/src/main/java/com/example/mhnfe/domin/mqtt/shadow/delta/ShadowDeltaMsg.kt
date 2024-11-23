package com.example.mhnfe.domin.mqtt.shadow.delta

import kotlinx.serialization.Serializable

@Serializable
data class ShadowDeltaMsg(
    val state: ShadowDeltaState
)
