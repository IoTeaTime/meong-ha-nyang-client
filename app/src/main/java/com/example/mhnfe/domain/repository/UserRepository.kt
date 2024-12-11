package com.example.mhnfe.domain.repository

import com.example.mhnfe.data.remote.response.ChangeNicknameOrGroupNameResponse
import com.example.mhnfe.data.remote.response.ChangePasswordResponse
import com.example.mhnfe.data.remote.response.ProfileResponse

interface UserRepository {
    suspend fun changePassword(accessToken: String, currentPassword: String, newPassword: String):
            ChangePasswordResponse
    suspend fun changeNicknameOrGroupName(accessToken: String, nickname: String?, groupName: String?):
            ChangeNicknameOrGroupNameResponse
    suspend fun getMemberDetails(accessToken: String, memberId: Int): ProfileResponse
}