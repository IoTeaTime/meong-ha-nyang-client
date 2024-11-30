package com.example.mhnfe.data.repository

import com.example.mhnfe.data.remote.api.UserApi
import com.example.mhnfe.data.remote.request.ChangeNicknameOrGroupNameRequest
import com.example.mhnfe.data.remote.request.ChangePasswordRequest
import com.example.mhnfe.data.remote.response.ChangeNicknameOrGroupNameResponse
import com.example.mhnfe.data.remote.response.ChangePasswordResponse
import com.example.mhnfe.data.remote.response.RefreshAccessTokenResponse
import com.example.mhnfe.data.remote.response.RefreshedAccessToken
import com.example.mhnfe.domain.repository.UserRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository{

    override suspend fun getNewAccessToken(
        response: RefreshAccessTokenResponse
    ): RefreshedAccessToken {
        return response.body
    }

    override suspend fun changePassword(
        accessToken: String,
        currentPassword: String,
        newPassword: String
    ): ChangePasswordResponse {
        val request = ChangePasswordRequest(currentPassword, newPassword)
        return userApi.changePassword(accessToken, request)
    }

    override suspend fun changeNicknameOrGroupName(
        accessToken: String,
        nickname: String?,
        groupName: String?
    ): ChangeNicknameOrGroupNameResponse {
        var request = ChangeNicknameOrGroupNameRequest(nickname,groupName)
        return userApi.changeNicknameOrGroupName(accessToken, request)
    }


}