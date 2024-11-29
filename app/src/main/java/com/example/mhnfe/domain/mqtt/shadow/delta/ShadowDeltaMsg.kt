package com.example.mhnfe.domain.mqtt.shadow.delta

import kotlinx.serialization.Serializable

@Serializable
data class ShadowDeltaMsg(
    val state: ShadowDeltaState,
    val previous: ShadowDeltaState? = null
)