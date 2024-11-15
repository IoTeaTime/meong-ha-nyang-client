package com.example.mhnfe.data.model

data class SignUpRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val nickname: String
)