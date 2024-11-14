package com.example.mhnfe.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestFcmToken(
    @SerialName("token")
    val token: String
)
