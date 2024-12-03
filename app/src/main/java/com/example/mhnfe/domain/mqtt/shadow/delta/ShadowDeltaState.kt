package com.example.mhnfe.domain.mqtt.shadow.delta

import kotlinx.serialization.Serializable

@Serializable
data class ShadowDeltaState(
    val delta: ShadowContent? = null,
    val reported: ShadowContent? = null
)