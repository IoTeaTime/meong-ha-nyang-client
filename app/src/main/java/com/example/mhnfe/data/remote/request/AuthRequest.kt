package com.example.mhnfe.data.remote.request

import kotlinx.serialization.Serializable

data class SignUpRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val nickname: String
)

data class EmailRequest(
    val email: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshFcmTokenRequest (
    val token: String
)

data class SendPasswordRequest (
    val email: String
)

data class SendEmailVerificationRequest (
    val email: String
)

data class CheckEmailVerificationRequest (
    val email: String,
    val code: String
)