package com.example.mhnfe.data.repository

import com.example.mhnfe.data.api.ApiService
import com.example.mhnfe.data.api.UserApi
import com.example.mhnfe.data.model.RefreshResponse

class UserRepository {
    private val api: UserApi

    init {
        api = ApiService.createApiService(UserApi::class.java)
    }

    suspend fun refreshAccessToken(
        token: String
    ): RefreshResponse{
        return api.refreshToken(token)
    }
}