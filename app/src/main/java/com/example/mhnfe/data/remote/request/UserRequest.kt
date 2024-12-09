package com.example.mhnfe.data.remote.request

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class ChangeNicknameOrGroupNameRequest(
    val nickname: String? = null,
    val groupName: String? = null
)