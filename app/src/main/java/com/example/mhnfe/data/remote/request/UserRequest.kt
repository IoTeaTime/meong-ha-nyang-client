package com.example.mhnfe.data.remote.request

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)