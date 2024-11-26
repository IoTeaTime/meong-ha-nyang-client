package com.example.mhnfe.data.remote.response

// 리프래시 토큰
data class RefreshAccessTokenResponse(
    val result: Result,
    val body: RefreshedAccessToken
)

data class RefreshedAccessToken(
    val newAccessToken: String
)