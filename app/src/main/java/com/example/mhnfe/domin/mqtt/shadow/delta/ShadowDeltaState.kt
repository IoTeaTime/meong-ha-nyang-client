package com.example.mhnfe.domin.mqtt.shadow.delta

import kotlinx.serialization.Serializable

@Serializable
data class ShadowDeltaState(
    val delta: ShadowContent
)
