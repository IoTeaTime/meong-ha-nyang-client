package com.example.mhnfe.data.remote.request

data class SignUpRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val nickname: String
)

data class EmailRequest(
    val email: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshFcmTokenRequest (
    val token: String
)