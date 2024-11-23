package com.example.mhnfe.mqtt.shadow.delta

import kotlinx.serialization.Serializable

@Serializable
data class ShadowDeltaState(
    val delta: ShadowContent)
