package com.example.mhnfe.data.remote.response

import kotlinx.serialization.Serializable

// 리프래시 토큰
data class RefreshAccessTokenResponse(
    val result: Result,
    val body: RefreshedAccessToken
)

@Serializable
data class RefreshedAccessToken(
    val newAccessToken: String
)