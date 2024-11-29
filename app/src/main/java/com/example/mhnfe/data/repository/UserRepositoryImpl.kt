package com.example.mhnfe.data.repository

import com.example.mhnfe.data.remote.api.UserApi
import com.example.mhnfe.data.remote.request.ChangePasswordRequest
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
}