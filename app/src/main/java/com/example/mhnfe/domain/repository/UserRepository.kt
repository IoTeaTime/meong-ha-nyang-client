package com.example.mhnfe.domain.repository

import com.example.mhnfe.data.remote.response.ChangeNicknameOrGroupNameResponse
import com.example.mhnfe.data.remote.response.ChangePasswordResponse
import com.example.mhnfe.data.remote.response.RefreshAccessTokenResponse
import com.example.mhnfe.data.remote.response.RefreshToken
import com.example.mhnfe.data.remote.response.RefreshedAccessToken
import retrofit2.Response

interface UserRepository {
    suspend fun getNewAccessToken(refreshToken: String): RefreshAccessTokenResponse
    suspend fun changePassword(accessToken: String, currentPassword: String, newPassword: String):
            ChangePasswordResponse
    suspend fun changeNicknameOrGroupName(accessToken: String, nickname: String?, groupName: String?):
            ChangeNicknameOrGroupNameResponse
}