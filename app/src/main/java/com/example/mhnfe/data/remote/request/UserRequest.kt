package com.example.mhnfe.data.remote.request

import com.example.mhnfe.data.remote.response.ChangeNicknameOrGroupNameBody
import com.example.mhnfe.data.remote.response.Result

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class ChangeNicknameOrGroupNameRequest(
    val nickname: String? = null,
    val groupName: String? = null
)